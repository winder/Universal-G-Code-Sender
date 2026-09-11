import { useRef } from "react";
import { Button, Form } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFloppyDisk, faXmark, faFileExport } from "@fortawesome/free-solid-svg-icons";
import { Macro } from "../model/Macro";
import { macroGcodeToEditorText, editorTextToMacroGcode } from "../utils/macroGcode";
import { MACRO_COLOR_PRESETS, macroColorStyle } from "../utils/macroColors";
import { MACRO_ICON_KEYS, MACRO_ICONS } from "../utils/macroIcons";
import { downloadSingleMacro } from "../services/download";
import MacroGcodeEditor, { MacroGcodeEditorHandle } from "./MacroGcodeEditor";
import "./MacroForm.scss";

const PLACEHOLDERS: { label: string; token: string }[] = [
  { label: "Machine X", token: "{machine_x}" },
  { label: "Machine Y", token: "{machine_y}" },
  { label: "Machine Z", token: "{machine_z}" },
  { label: "Work X", token: "{work_x}" },
  { label: "Work Y", token: "{work_y}" },
  { label: "Work Z", token: "{work_z}" },
  { label: "Prompt", token: "{prompt|name|default}" },
  { label: "Keypress", token: "{keypress|keys}" },
];

type Props = {
  macro: Macro;
  isDirty: boolean;
  isNew: boolean;
  compact?: boolean;
  onChange: (macro: Macro) => void;
  onSave: () => void;
  onDiscard: () => void;
};

const MacroForm = ({ macro, isDirty, isNew, compact = false, onChange, onSave, onDiscard }: Props) => {
  const groupClass = compact ? " compact" : "";
  const gcodeEditorRef = useRef<MacroGcodeEditorHandle | null>(null);

  // The editor works in the human multi-line form; only converted to/from
  // UGS's semicolon-joined storage format at the field's edges (here, and
  // in MacroEditor.tsx when a macro is first selected/loaded).
  const updateGcodeText = (text: string) => {
    onChange({ ...macro, gcode: editorTextToMacroGcode(text) });
  };

  const insertPlaceholder = (token: string) => {
    gcodeEditorRef.current?.insertAtCursor(token);
  };

  return (
    <div className="macroForm">
      <Form.Group className="macroFormField">
        <Form.Label>Name</Form.Label>
        <Form.Control
          value={macro.name ?? ""}
          onChange={(e) => onChange({ ...macro, name: e.target.value })}
        />
      </Form.Group>

      <Form.Group className="macroFormField">
        <Form.Label>Description</Form.Label>
        <Form.Control
          value={macro.description ?? ""}
          onChange={(e) => onChange({ ...macro, description: e.target.value })}
        />
      </Form.Group>

      <Form.Group className="macroFormField macroFormGcode">
        <Form.Label>Gcode</Form.Label>
        <MacroGcodeEditor
          ref={gcodeEditorRef}
          initialValue={macroGcodeToEditorText(macro.gcode)}
          onChange={updateGcodeText}
        />
        <div className={"macroFormPlaceholders" + groupClass}>
          {PLACEHOLDERS.map((p) => (
            <Button
              key={p.token}
              size="sm"
              variant="outline-secondary"
              onClick={() => insertPlaceholder(p.token)}
              title={p.token}
            >
              {p.label}
            </Button>
          ))}
        </div>
      </Form.Group>

      <Form.Group className="macroFormField">
        <Form.Label>Button color</Form.Label>
        <div className={"macroFormColors" + groupClass}>
          {MACRO_COLOR_PRESETS.map((color) => (
            <button
              key={color}
              type="button"
              className={"macroColorSwatch" + (macro.color === color ? " selected" : "")}
              style={{ backgroundColor: color }}
              title={color}
              onClick={() => onChange({ ...macro, color })}
            />
          ))}
          <input
            type="color"
            className="macroColorCustom"
            value={macro.color ?? "#4ade80"}
            onChange={(e) => onChange({ ...macro, color: e.target.value })}
            title="Custom color"
          />
          {macro.color && (
            <button
              type="button"
              className="macroColorClear"
              onClick={() => onChange({ ...macro, color: undefined })}
            >
              Clear
            </button>
          )}
        </div>
      </Form.Group>

      <Form.Group className="macroFormField">
        <Form.Label>Icon</Form.Label>
        <div className={"macroFormIcons" + groupClass}>
          {MACRO_ICON_KEYS.map((key) => (
            <button
              key={key}
              type="button"
              className={"macroIconSwatch" + (macro.icon === key ? " selected" : "")}
              style={macro.icon === key ? macroColorStyle(macro.color || "#4ade80") : undefined}
              onClick={() => onChange({ ...macro, icon: key })}
              title={key}
            >
              <FontAwesomeIcon icon={MACRO_ICONS[key]} />
            </button>
          ))}
          {macro.icon && (
            <button
              type="button"
              className="macroColorClear"
              onClick={() => onChange({ ...macro, icon: undefined })}
            >
              Clear
            </button>
          )}
        </div>
      </Form.Group>

      <div className="macroFormActions">
        <Button variant="outline-secondary" onClick={() => downloadSingleMacro(macro)}>
          <FontAwesomeIcon icon={faFileExport} /> Export this macro
        </Button>
        <div className="macroFormActionsRight">
          <Button variant="secondary" disabled={!isDirty} onClick={onDiscard}>
            <FontAwesomeIcon icon={faXmark} /> {isNew ? "Cancel" : "Discard"}
          </Button>
          <Button variant="primary" disabled={!isDirty} onClick={onSave}>
            <FontAwesomeIcon icon={faFloppyDisk} /> Save
          </Button>
        </div>
      </div>
    </div>
  );
};

export default MacroForm;
