import { useEffect, useMemo, useRef, useState } from "react";
import { Compartment, EditorState, StateEffect, StateField } from "@codemirror/state";
import { Decoration, DecorationSet, EditorView, keymap, lineNumbers, highlightActiveLine } from "@codemirror/view";
import { defaultKeymap, history, historyKeymap } from "@codemirror/commands";
import { searchKeymap } from "@codemirror/search";
import { Button, Spinner } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFloppyDisk, faFileExport, faForward } from "@fortawesome/free-solid-svg-icons";
import { useAppSelector } from "../hooks/useAppSelector";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { getFileContent, saveFileContent, saveFileContentAs } from "../services/fileContent";
import { runFromLine } from "../services/files";
import { uiActions } from "../store/uiSlice";
import { gcodeLanguage, gcodeSyntaxHighlighting } from "./gcodeLanguage";
import SaveAsModal from "./SaveAsModal";
import ConfirmDialog from "./ConfirmDialog";
import "./GcodeEditor.scss";

const editorTheme = EditorView.theme(
  {
    "&": { height: "100%", fontSize: "0.9rem", backgroundColor: "#111213" },
    ".cm-content": { fontFamily: "monospace" },
    ".cm-gutters": { backgroundColor: "#111213", color: "#5b6062", border: "none" },
    ".cm-activeLine": { backgroundColor: "#1c1e1f" },
    ".cm-activeLineGutter": { backgroundColor: "#1c1e1f" },
    "&.cm-focused": { outline: "none" },
  },
  { dark: true }
);

const getFileName = (filePath: string) => filePath.replace(/^.*[\\/]/, "");

// Dims lines 1..N (1-based, inclusive) to show what won't actually run next -
// either because they've already been sent (live during a job) or because
// they're being skipped by an armed "run from" line. A StateField (rather
// than the Compartment used for the editable toggle below) is the more
// natural CM6 fit here: it reacts to a dispatched effect on its own, no
// explicit reconfigure() call needed, and every plain edit transaction just
// remaps its existing ranges via tr.changes like any other decoration.
const setDimThroughLine = StateEffect.define<number>();
const dimmedLineMark = Decoration.line({ class: "cm-dimmedLine" });

const dimThroughField = StateField.define<DecorationSet>({
  create() {
    return Decoration.none;
  },
  update(decorations, tr) {
    for (const effect of tr.effects) {
      if (!effect.is(setDimThroughLine)) continue;
      if (effect.value <= 0) return Decoration.none;

      const lastLine = Math.min(effect.value, tr.state.doc.lines);
      const ranges = [];
      for (let line = 1; line <= lastLine; line++) {
        ranges.push(dimmedLineMark.range(tr.state.doc.line(line).from));
      }
      return Decoration.set(ranges);
    }
    return decorations.map(tr.changes);
  },
  provide: (field) => EditorView.decorations.from(field),
});

const GcodeEditor = () => {
  const dispatch = useAppDispatch();
  const fileStatus = useAppSelector((state) => state.fileStatus);
  const currentState = useAppSelector((state) => state.status.state);
  const fileName = useMemo(() => getFileName(fileStatus.fileName), [fileStatus.fileName]);
  // Editing the file on disk doesn't touch the controller, so it doesn't need
  // a connection (or even IDLE) - only actually streaming a job makes editing
  // unsafe, since the file being sent could then no longer match what's open
  // here.
  const isEditable = currentState !== "RUN" && currentState !== "HOLD" && currentState !== "CHECK";
  // "Run from here" only arms a line on the backend (see runFromLine) - it
  // doesn't send anything itself, so - like opening/editing - it doesn't
  // need a connection either. Only Start (separately, in the job bar) needs
  // one. Same gate as isEditable: blocked only while a job is actually
  // streaming.
  const canRunFrom = isEditable;
  const armedRunFromLine = useAppSelector((state) => state.ui.runFromLine);
  // What to dim: while a job is actually streaming, everything already sent;
  // otherwise, whatever's armed to be skipped by "run from" - the two never
  // apply at once, since arming is itself blocked while a job is running
  // (see canRunFrom above).
  //
  // lastCompletedLineNumber, not completedRowCount: the latter just counts
  // rows from zero for whatever's currently streaming, which undercounts
  // badly once "run from" starts a stream partway through the file (it'd
  // read 1, 2, 3... instead of the actual line numbers) - lastCompletedLineNumber
  // is the original file's own line number instead, correct either way. It's
  // already inclusive (see dimThroughField's own comment), so dims exactly
  // through the line that just completed, no further adjustment needed.
  //
  // armedRunFromLine - 1, not - 2: this is purely an editor-line fact ("dim
  // everything before the line that was selected"), not a conversion to the
  // backend's command-index argument - confirmed armedRunFromLine itself
  // does become the actual resume point (see handleConfirmRunFrom), so
  // there's no offset to apply here at all. Don't "helpfully" re-apply the
  // -2 fix from there - that was already tried here and was wrong, it left
  // the line right before the resume point undimmed.
  const dimThroughLine =
    currentState === "RUN" || currentState === "HOLD" || currentState === "CHECK"
      ? fileStatus.lastCompletedLineNumber
      : armedRunFromLine > 0
        ? armedRunFromLine - 1
        : 0;

  const editorContainerRef = useRef<HTMLDivElement | null>(null);
  const viewRef = useRef<EditorView | null>(null);
  // The editable state needs to change without tearing down and recreating the
  // whole editor (that would also blow away undo history/cursor position) - a
  // Compartment lets it be reconfigured in place from the effect below.
  const editableCompartmentRef = useRef(new Compartment());
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isDirty, setIsDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showSaveAs, setShowSaveAs] = useState(false);
  // 1-based, matching what the gutter shows - defaults to the first line so
  // the button always has a sensible target even before anyone taps a line.
  const [cursorLine, setCursorLine] = useState(1);
  const [showRunFromConfirm, setShowRunFromConfirm] = useState(false);

  useEffect(() => {
    if (!editorContainerRef.current || !fileName) {
      return;
    }

    setIsLoading(true);
    setError(null);
    setIsDirty(false);
    setCursorLine(1);
    dispatch(uiActions.setEditorCursorLine(1));
    // Mirrors the backend's own auto-reset-on-open (RunFromService resets to
    // a normal full run whenever a new file is opened) so the job bar's
    // armed-line badge doesn't keep pointing at a line from a previous file.
    dispatch(uiActions.setRunFromLine(0));

    let cancelled = false;
    getFileContent()
      .then((content) => {
        if (cancelled || !editorContainerRef.current) return;

        viewRef.current?.destroy();
        viewRef.current = new EditorView({
          state: EditorState.create({
            doc: content,
            extensions: [
              lineNumbers(),
              highlightActiveLine(),
              history(),
              keymap.of([...defaultKeymap, ...historyKeymap, ...searchKeymap]),
              gcodeLanguage,
              gcodeSyntaxHighlighting,
              editorTheme,
              editableCompartmentRef.current.of(EditorView.editable.of(isEditable)),
              dimThroughField,
              EditorView.updateListener.of((update) => {
                if (update.docChanged) setIsDirty(true);
                if (update.selectionSet || update.docChanged) {
                  const line = update.state.doc.lineAt(update.state.selection.main.head).number;
                  setCursorLine(line);
                  dispatch(uiActions.setEditorCursorLine(line));
                }
              }),
            ],
          }),
          parent: editorContainerRef.current,
        });
      })
      .catch(() => !cancelled && setError("Couldn't load this file for editing."))
      .finally(() => !cancelled && setIsLoading(false));

    return () => {
      cancelled = true;
      viewRef.current?.destroy();
      viewRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fileName]);

  // Keeps the editor's editable state in sync with the machine state on its
  // own, instead of only picking it up next time a file loads - previously,
  // opening a file while the machine hadn't yet reported IDLE (e.g. right
  // after connecting) froze the editor as non-editable/unclickable forever,
  // even once the machine settled into IDLE.
  useEffect(() => {
    viewRef.current?.dispatch({
      effects: editableCompartmentRef.current.reconfigure(EditorView.editable.of(isEditable)),
    });
  }, [isEditable]);

  useEffect(() => {
    viewRef.current?.dispatch({ effects: setDimThroughLine.of(dimThroughLine) });
  }, [dimThroughLine]);

  const handleSave = () => {
    if (!viewRef.current || !fileName) return;
    setIsSaving(true);
    saveFileContent(viewRef.current.state.doc.toString())
      .then(() => setIsDirty(false))
      .catch(() => setError("Couldn't save this file."))
      .finally(() => setIsSaving(false));
  };

  const handleSaveAsToWorkspace = (newFilename: string) => {
    if (!viewRef.current) return Promise.reject();
    return saveFileContentAs(newFilename, viewRef.current.state.doc.toString()).then(() => setIsDirty(false));
  };

  // CodeMirror's line numbers are 1-based. Desktop's own "Start program
  // here" (RunFromHere.java) computes root.getElementIndex(caretPosition) -
  // 1, where getElementIndex is *already* a 0-based line index on its own -
  // so its net result is two less than the 1-based line number, not one
  // less. Confirmed empirically against the real backend: sending only
  // "- 1" resumed one command later than desktop did for the identical
  // selected line (skipped one extra command) - matching "- 2" here fixed
  // it. line <= 1 lands at 0 or below, matching runFromLine's own <= 0 =
  // disabled convention, so no special-casing needed for the first line.
  const handleConfirmRunFrom = () => {
    runFromLine(cursorLine - 2).then(() => dispatch(uiActions.setRunFromLine(cursorLine)));
    setShowRunFromConfirm(false);
  };

  // Same reset the job bar's own "Reset" link does (see JobBar.tsx) - kept
  // here too so it's reachable right next to Run from line without having
  // to look down at the job bar, which may not even be in view depending on
  // layout.
  const resetRunFromLine = () => {
    runFromLine(0).then(() => dispatch(uiActions.setRunFromLine(0)));
  };

  const cursorLineText = viewRef.current?.state.doc.line(cursorLine).text ?? "";

  if (!fileName) {
    return <div className="gcodeEditorEmpty">No file loaded. Open a file from the Run tab first.</div>;
  }

  return (
    <div className="gcodeEditor">
      {showSaveAs && (
        <SaveAsModal
          defaultFileName={fileName}
          getContent={() => viewRef.current?.state.doc.toString() ?? ""}
          onSaveToWorkspace={handleSaveAsToWorkspace}
          handleClose={() => setShowSaveAs(false)}
        />
      )}

      <ConfirmDialog
        show={showRunFromConfirm}
        title="Run from here?"
        message={
          `This only prepares line ${cursorLine} as the job's new starting point - it won't move the ` +
          `machine yet. The machine will restore position, spindle, coolant, and work offset before ` +
          `continuing from:\n\n${cursorLineText}\n\nPress Start afterward to actually begin.`
        }
        confirmLabel="Run from here"
        onConfirm={handleConfirmRunFrom}
        onCancel={() => setShowRunFromConfirm(false)}
      />

      <div className="gcodeEditorToolbar">
        <span className="gcodeEditorFileName">{fileName}</span>
        {!isEditable && <span className="gcodeEditorLocked">Read-only while a job is running</span>}
        {error && <span className="gcodeEditorError">{error}</span>}
        <Button
          className="gcodeEditorRunFrom"
          variant="outline-secondary"
          disabled={!canRunFrom}
          title={canRunFrom ? undefined : "Can't arm a starting line while a job is running"}
          onClick={() => setShowRunFromConfirm(true)}
        >
          <FontAwesomeIcon icon={faForward} /> Run from line {cursorLine}
        </Button>
        {armedRunFromLine > 0 && (
          <button type="button" className="gcodeEditorRunFromReset" onClick={resetRunFromLine}>
            Reset
          </button>
        )}
        <Button
          className="gcodeEditorSave"
          variant="outline-secondary"
          disabled={!isEditable || isSaving}
          onClick={() => setShowSaveAs(true)}
        >
          <FontAwesomeIcon icon={faFileExport} /> Save as
        </Button>
        <Button
          className="gcodeEditorSave"
          variant="primary"
          disabled={!isEditable || !isDirty || isSaving}
          onClick={handleSave}
        >
          <FontAwesomeIcon icon={faFloppyDisk} /> Save {isSaving && <Spinner size="sm" />}
        </Button>
      </div>

      <div className="gcodeEditorContent">
        {isLoading && <div className="gcodeEditorLoading">Loading...</div>}
        <div className="gcodeEditorCodeMirror" ref={editorContainerRef} />
      </div>
    </div>
  );
};

export default GcodeEditor;
