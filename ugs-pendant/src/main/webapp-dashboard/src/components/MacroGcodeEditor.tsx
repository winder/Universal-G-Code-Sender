import { forwardRef, useEffect, useImperativeHandle, useRef } from "react";
import { EditorState } from "@codemirror/state";
import { EditorView, keymap, lineNumbers, highlightActiveLine } from "@codemirror/view";
import { defaultKeymap, history, historyKeymap } from "@codemirror/commands";
import { gcodeLanguage, gcodeSyntaxHighlighting } from "./gcodeLanguage";
import "./MacroGcodeEditor.scss";

const editorTheme = EditorView.theme(
  {
    "&": { height: "100%", fontSize: "0.85rem", backgroundColor: "#1c1e1f" },
    ".cm-content": { fontFamily: "monospace" },
    ".cm-gutters": { backgroundColor: "#1c1e1f", color: "#5b6062", border: "none" },
    ".cm-activeLine": { backgroundColor: "#232526" },
    ".cm-activeLineGutter": { backgroundColor: "#232526" },
    "&.cm-focused": { outline: "none" },
  },
  { dark: true }
);

export type MacroGcodeEditorHandle = {
  insertAtCursor: (text: string) => void;
};

type Props = {
  initialValue: string;
  onChange: (text: string) => void;
};

// A small CodeMirror wrapper so the macro editor's gcode field gets the same
// syntax highlighting as the main gcode editor (see gcodeLanguage.ts), rather
// than a plain textarea. Uncontrolled by design - `initialValue` only seeds
// the doc on mount; MacroEditor forces a remount (via a key) whenever the
// selected macro changes or edits are discarded, same trick MacroForm's old
// textarea state relied on, so there's no need to sync a `value` prop back in.
const MacroGcodeEditor = forwardRef<MacroGcodeEditorHandle, Props>(({ initialValue, onChange }, ref) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const viewRef = useRef<EditorView | null>(null);
  const onChangeRef = useRef(onChange);
  onChangeRef.current = onChange;

  useEffect(() => {
    if (!containerRef.current) return;

    const view = new EditorView({
      state: EditorState.create({
        doc: initialValue,
        extensions: [
          lineNumbers(),
          highlightActiveLine(),
          history(),
          keymap.of([...defaultKeymap, ...historyKeymap]),
          gcodeLanguage,
          gcodeSyntaxHighlighting,
          editorTheme,
          EditorView.updateListener.of((update) => {
            if (update.docChanged) onChangeRef.current(update.state.doc.toString());
          }),
        ],
      }),
      parent: containerRef.current,
    });
    viewRef.current = view;

    return () => {
      view.destroy();
      viewRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useImperativeHandle(ref, () => ({
    insertAtCursor(text: string) {
      const view = viewRef.current;
      if (!view) return;
      const { from, to } = view.state.selection.main;
      view.dispatch({
        changes: { from, to, insert: text },
        selection: { anchor: from + text.length },
      });
      view.focus();
    },
  }));

  return <div className="macroGcodeEditor" ref={containerRef} />;
});

export default MacroGcodeEditor;
