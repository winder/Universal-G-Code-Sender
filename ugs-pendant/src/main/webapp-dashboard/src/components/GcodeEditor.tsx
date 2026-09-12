import { useEffect, useMemo, useRef, useState } from "react";
import { Compartment, EditorState } from "@codemirror/state";
import { EditorView, keymap, lineNumbers, highlightActiveLine } from "@codemirror/view";
import { defaultKeymap, history, historyKeymap } from "@codemirror/commands";
import { searchKeymap } from "@codemirror/search";
import { Button, Spinner } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFloppyDisk, faFileExport } from "@fortawesome/free-solid-svg-icons";
import { useAppSelector } from "../hooks/useAppSelector";
import { getFileContent, saveFileContent, saveFileContentAs } from "../services/fileContent";
import { gcodeLanguage, gcodeSyntaxHighlighting } from "./gcodeLanguage";
import SaveAsModal from "./SaveAsModal";
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

const GcodeEditor = () => {
  const fileStatus = useAppSelector((state) => state.fileStatus);
  const currentState = useAppSelector((state) => state.status.state);
  const fileName = useMemo(() => getFileName(fileStatus.fileName), [fileStatus.fileName]);
  // Editing the file on disk doesn't touch the controller, so it doesn't need
  // a connection (or even IDLE) - only actually streaming a job makes editing
  // unsafe, since the file being sent could then no longer match what's open
  // here.
  const isEditable = currentState !== "RUN" && currentState !== "HOLD" && currentState !== "CHECK";

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

  useEffect(() => {
    if (!editorContainerRef.current || !fileName) {
      return;
    }

    setIsLoading(true);
    setError(null);
    setIsDirty(false);

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
              EditorView.updateListener.of((update) => {
                if (update.docChanged) setIsDirty(true);
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

      <div className="gcodeEditorToolbar">
        <span className="gcodeEditorFileName">{fileName}</span>
        {!isEditable && <span className="gcodeEditorLocked">Read-only while a job is running</span>}
        {error && <span className="gcodeEditorError">{error}</span>}
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
