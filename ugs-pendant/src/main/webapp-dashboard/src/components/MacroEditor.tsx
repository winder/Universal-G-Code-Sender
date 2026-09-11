import { useEffect, useRef, useState } from "react";
import { Alert, Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowDown,
  faArrowUp,
  faFileExport,
  faFileImport,
  faPlus,
  faTrash,
} from "@fortawesome/free-solid-svg-icons";
import { useAppSelector } from "../hooks/useAppSelector";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { fetchMacros, macrosActions } from "../store/macrosSlice";
import { saveMacroList } from "../services/macros";
import { downloadMacroList, parseMacroListFile } from "../services/download";
import { Macro } from "../model/Macro";
import { MACRO_ICONS } from "../utils/macroIcons";
import { macroColorStyle } from "../utils/macroColors";
import MacroForm from "./MacroForm";
import ConfirmDialog from "./ConfirmDialog";
import "./MacroEditor.scss";

const blankMacro = (): Macro => ({
  uuid: crypto.randomUUID(),
  name: "New macro",
  description: "",
  gcode: "",
});

type PendingConfirm = {
  title: string;
  message: string;
  confirmLabel?: string;
  confirmVariant?: string;
  run: () => void;
};

const MacroEditor = () => {
  const dispatch = useAppDispatch();
  const macros = useAppSelector((state) => state.macros.macros);
  const loaded = useAppSelector((state) => state.macros.loaded);

  const [selectedUuid, setSelectedUuid] = useState<string | null>(null);
  const [draft, setDraft] = useState<Macro | null>(null);
  const [isNewDraft, setIsNewDraft] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pendingConfirm, setPendingConfirm] = useState<PendingConfirm | null>(null);
  const importInputRef = useRef<HTMLInputElement | null>(null);
  // MacroForm keeps its own local text-area state for the gcode field,
  // seeded from macro.gcode only when it (re)mounts. Discarding edits to the
  // *currently* selected macro changes draft's content but not its uuid, so
  // there's nothing else to force a remount - bump this to make one happen.
  const [formResetToken, setFormResetToken] = useState(0);

  useEffect(() => {
    if (!loaded) {
      dispatch(fetchMacros());
    }
  }, [dispatch, loaded]);

  const original = selectedUuid ? macros.find((m) => m.uuid === selectedUuid) : undefined;
  const isDirty = draft !== null && (isNewDraft || JSON.stringify(draft) !== JSON.stringify(original));

  const persist = (newList: Macro[]): Promise<Macro[]> => {
    setIsSaving(true);
    setError(null);
    return saveMacroList(newList)
      .then((result) => {
        dispatch(macrosActions.setMacros(result));
        return result;
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "Couldn't save macros");
        throw err;
      })
      .finally(() => setIsSaving(false));
  };

  const guardDirty = (run: () => void, title: string, message: string) => {
    if (isDirty) {
      setPendingConfirm({ title, message, confirmLabel: "Discard changes", confirmVariant: "danger", run });
    } else {
      run();
    }
  };

  const selectMacro = (uuid: string) => {
    if (uuid === selectedUuid) return;
    const target = macros.find((m) => m.uuid === uuid);
    if (!target) return;
    guardDirty(
      () => {
        setSelectedUuid(uuid);
        setDraft({ ...target });
        setIsNewDraft(false);
      },
      "Discard unsaved changes?",
      `You have unsaved changes to "${draft?.name ?? "this macro"}". Switch macros and discard them?`
    );
  };

  const startNewMacro = () => {
    guardDirty(
      () => {
        const fresh = blankMacro();
        setSelectedUuid(fresh.uuid);
        setDraft(fresh);
        setIsNewDraft(true);
      },
      "Discard unsaved changes?",
      `You have unsaved changes to "${draft?.name ?? "this macro"}". Start a new macro and discard them?`
    );
  };

  const saveDraft = () => {
    if (!draft) return;
    const newList = isNewDraft ? [...macros, draft] : macros.map((m) => (m.uuid === draft.uuid ? draft : m));
    persist(newList).then((result) => {
      const saved = result.find((m) => m.uuid === draft.uuid);
      setSelectedUuid(saved?.uuid ?? null);
      setDraft(saved ?? null);
      setIsNewDraft(false);
    });
  };

  const discardDraft = () => {
    if (isNewDraft) {
      setSelectedUuid(null);
      setDraft(null);
      setIsNewDraft(false);
    } else if (original) {
      setDraft({ ...original });
      setFormResetToken((n) => n + 1);
    }
  };

  const deleteMacro = (macro: Macro) => {
    setPendingConfirm({
      title: "Delete macro?",
      message: `Delete "${macro.name}"? This can't be undone.`,
      confirmLabel: "Delete",
      confirmVariant: "danger",
      run: () => {
        persist(macros.filter((m) => m.uuid !== macro.uuid)).then(() => {
          if (selectedUuid === macro.uuid) {
            setSelectedUuid(null);
            setDraft(null);
            setIsNewDraft(false);
          }
        });
      },
    });
  };

  const moveMacro = (index: number, direction: -1 | 1) => {
    const target = index + direction;
    if (target < 0 || target >= macros.length) return;
    const reordered = [...macros];
    [reordered[index], reordered[target]] = [reordered[target], reordered[index]];
    persist(reordered);
  };

  const onImportFileChosen = (file: File | undefined) => {
    if (!file) return;
    parseMacroListFile(file)
      .then((parsed) => {
        setPendingConfirm({
          title: "Import macros?",
          message: `Import ${parsed.length} macro(s) from "${file.name}"? This replaces all ${macros.length} macro(s) currently in UGS.`,
          confirmLabel: "Import",
          confirmVariant: "danger",
          run: () => {
            persist(parsed).then(() => {
              setSelectedUuid(null);
              setDraft(null);
              setIsNewDraft(false);
            });
          },
        });
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Couldn't read that file"));
  };

  return (
    <div className="macroEditor">
      <div className="macroEditorList">
        <div className="macroEditorListHeader">
          <Button size="sm" variant="outline-secondary" onClick={startNewMacro} title="New macro">
            <FontAwesomeIcon icon={faPlus} /> New
          </Button>
          <div className="macroEditorListHeaderRight">
            <Button
              size="sm"
              variant="outline-secondary"
              onClick={() => importInputRef.current?.click()}
              title="Import macro list (replaces all macros)"
            >
              <FontAwesomeIcon icon={faFileImport} />
            </Button>
            <input
              ref={importInputRef}
              type="file"
              accept="application/json"
              hidden
              onChange={(e) => {
                onImportFileChosen(e.target.files?.[0]);
                e.target.value = "";
              }}
            />
            <Button
              size="sm"
              variant="outline-secondary"
              onClick={() => downloadMacroList(macros)}
              title="Export all macros"
              disabled={macros.length === 0}
            >
              <FontAwesomeIcon icon={faFileExport} />
            </Button>
          </div>
        </div>

        {loaded && macros.length === 0 && !isNewDraft && (
          <div className="macroEditorEmpty">No macros yet - click New to add one.</div>
        )}

        <div className="macroEditorRows">
          {macros.map((macro, index) => (
            <div
              key={macro.uuid}
              className={"macroEditorRow" + (macro.uuid === selectedUuid ? " selected" : "")}
              onClick={() => selectMacro(macro.uuid)}
            >
              <span className="macroEditorRowSwatch" style={macroColorStyle(macro.color)}>
                {macro.icon && MACRO_ICONS[macro.icon] && <FontAwesomeIcon icon={MACRO_ICONS[macro.icon]} />}
              </span>
              <span className="macroEditorRowName">{macro.name}</span>
              <div className="macroEditorRowActions">
                <button
                  type="button"
                  disabled={index === 0}
                  onClick={(e) => {
                    e.stopPropagation();
                    moveMacro(index, -1);
                  }}
                  title="Move up"
                >
                  <FontAwesomeIcon icon={faArrowUp} />
                </button>
                <button
                  type="button"
                  disabled={index === macros.length - 1}
                  onClick={(e) => {
                    e.stopPropagation();
                    moveMacro(index, 1);
                  }}
                  title="Move down"
                >
                  <FontAwesomeIcon icon={faArrowDown} />
                </button>
                <button
                  type="button"
                  className="macroEditorRowDelete"
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteMacro(macro);
                  }}
                  title="Delete"
                >
                  <FontAwesomeIcon icon={faTrash} />
                </button>
              </div>
            </div>
          ))}

          {isNewDraft && draft && (
            <div className="macroEditorRow selected">
              <span className="macroEditorRowSwatch" style={macroColorStyle(draft.color)}>
                {draft.icon && MACRO_ICONS[draft.icon] && <FontAwesomeIcon icon={MACRO_ICONS[draft.icon]} />}
              </span>
              <span className="macroEditorRowName">{draft.name || "New macro"} (unsaved)</span>
            </div>
          )}
        </div>
      </div>

      <div className="macroEditorFormPane">
        {error && (
          <Alert variant="danger" dismissible onClose={() => setError(null)}>
            {error}
          </Alert>
        )}
        {draft ? (
          <MacroForm
            // Remount when the selected macro changes - MacroForm keeps its
            // own local text-area state for the gcode field (derived from
            // macro.gcode only on mount), so without a key tied to identity
            // it would keep showing the previously selected macro's gcode.
            key={`${draft.uuid}-${formResetToken}`}
            macro={draft}
            isDirty={isDirty}
            isNew={isNewDraft}
            onChange={setDraft}
            onSave={saveDraft}
            onDiscard={discardDraft}
          />
        ) : (
          <div className="macroEditorNoSelection">Select a macro to edit, or click New.</div>
        )}
        {isSaving && <div className="macroEditorSaving">Saving...</div>}
      </div>

      {pendingConfirm && (
        <ConfirmDialog
          show={true}
          title={pendingConfirm.title}
          message={pendingConfirm.message}
          confirmLabel={pendingConfirm.confirmLabel}
          confirmVariant={pendingConfirm.confirmVariant}
          onConfirm={() => {
            pendingConfirm.run();
            setPendingConfirm(null);
          }}
          onCancel={() => setPendingConfirm(null)}
        />
      )}
    </div>
  );
};

export default MacroEditor;
