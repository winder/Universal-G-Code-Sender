import { useRef, useState } from "react";
import { Button, Form } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFloppyDisk, faXmark, faFileExport } from "@fortawesome/free-solid-svg-icons";
import { Macro } from "../model/Macro";
import { macroGcodeToEditorText, editorTextToMacroGcode } from "../utils/macroGcode";
import { MACRO_COLOR_PRESETS, macroColorStyle } from "../utils/macroColors";
import { MACRO_ICON_KEYS, MACRO_ICONS } from "../utils/macroIcons";
import { downloadSingleMacro } from "../services/download";
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
  onChange: (macro: Macro) => void;
  onSave: () => void;
  onDiscard: () => void;
};

const MacroForm = ({ macro, isDirty, isNew, onChange, onSave, onDiscard }: Props) => {
  // The editor works in the human multi-line form; only converted to/from
  // UGS's semicolon-joined storage format at the field's edges (here, and
  // in MacroEditor.tsx when a macro is first selected/loaded).
  const [gcodeText, setGcodeText] = useState(() => macroGcodeToEditorText(macro.gcode));
  const gcodeRef = useRef<HTMLTextAreaElement | null>(null);

  const updateGcodeText = (text: string) => {
    setGcodeText(text);
    onChange({ ...macro, gcode: editorTextToMacroGcode(text) });
  };

  const insertPlaceholder = (token: string) => {
    const el = gcodeRef.current;
    if (!el) {
      return;
    }
    const start = el.selectionStart ?? gcodeText.length;
    const end = el.selectionEnd ?? gcodeText.length;
    const next = gcodeText.slice(0, start) + token + gcodeText.slice(end);
    updateGcodeText(next);
    // Put the cursor right after the inserted token, not at the very end -
    // otherwise inserting into the middle of existing text feels broken.
    requestAnimationFrame(() => {
      el.focus();
      el.setSelectionRange(start + token.length, start + token.length);
    });
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
        <Form.Control
          ref={gcodeRef}
          as="textarea"
          rows={8}
          className="macroFormGcodeInput"
          value={gcodeText}
          onChange={(e) => updateGcodeText(e.target.value)}
          placeholder={"G0 X0 Y0\nM3 S1000"}
        />
        <div className="macroFormPlaceholders">
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
        <div className="macroFormColors">
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
        <div className="macroFormIcons">
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
