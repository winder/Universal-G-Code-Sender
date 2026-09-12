import { StreamLanguage } from "@codemirror/language";
import { HighlightStyle, syntaxHighlighting } from "@codemirror/language";
import { tags } from "@lezer/highlight";

// A small hand-written tokenizer for gcode - there's no off-the-shelf CodeMirror
// language for it. Recognizes comments, line numbers, G/M-codes and axis/parameter words.
export const gcodeLanguage = StreamLanguage.define<null>({
  token(stream) {
    if (stream.eatSpace()) return null;

    if (stream.match(/^;.*/)) return "comment";
    if (stream.match(/^\([^)]*\)?/)) return "comment";
    if (stream.match(/^[Nn]\d+/)) return "meta";
    if (stream.match(/^[GgMm]\d+(\.\d+)?/)) return "keyword";
    if (stream.match(/^[A-Za-z]-?\d*\.?\d+/)) return "number";

    stream.next();
    return null;
  },
});

export const gcodeHighlightStyle = HighlightStyle.define([
  { tag: tags.comment, color: "#6b7280", fontStyle: "italic" },
  { tag: tags.meta, color: "#8a8f92" },
  { tag: tags.keyword, color: "#7bdcff", fontWeight: "bold" },
  { tag: tags.number, color: "#4ade80" },
]);

export const gcodeSyntaxHighlighting = syntaxHighlighting(gcodeHighlightStyle);
