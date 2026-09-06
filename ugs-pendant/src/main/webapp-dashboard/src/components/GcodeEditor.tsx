import { useEffect, useMemo, useRef, useState } from "react";
import { EditorState } from "@codemirror/state";
import { EditorView, keymap, lineNumbers, highlightActiveLine } from "@codemirror/view";
import { defaultKeymap, history, historyKeymap } from "@codemirror/commands";
import { searchKeymap } from "@codemirror/search";
import { Button, Spinner } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFloppyDisk } from "@fortawesome/free-solid-svg-icons";
import { useAppSelector } from "../hooks/useAppSelector";
import { getFileContent, saveFileContent } from "../services/fileContent";
import { gcodeLanguage, gcodeSyntaxHighlighting } from "./gcodeLanguage";
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
  const isEditable = currentState === "IDLE";

  const editorContainerRef = useRef<HTMLDivElement | null>(null);
  const viewRef = useRef<EditorView | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isDirty, setIsDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!editorContainerRef.current || !fileName) {
      return;
    }

    setIsLoading(true);
    setError(null);
    setIsDirty(false);

    let cancelled = false;
    getFileContent(fileName)
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
              EditorView.editable.of(isEditable),
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

  const handleSave = () => {
    if (!viewRef.current || !fileName) return;
    setIsSaving(true);
    saveFileContent(fileName, viewRef.current.state.doc.toString())
      .then(() => setIsDirty(false))
      .catch(() => setError("Couldn't save this file."))
      .finally(() => setIsSaving(false));
  };

  if (!fileName) {
    return <div className="gcodeEditorEmpty">No file loaded. Open a file from the Run tab first.</div>;
  }

  return (
    <div className="gcodeEditor">
      <div className="gcodeEditorToolbar">
        <span className="gcodeEditorFileName">{fileName}</span>
        {!isEditable && <span className="gcodeEditorLocked">Read-only while the machine isn't idle</span>}
        {error && <span className="gcodeEditorError">{error}</span>}
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
