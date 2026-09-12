// Converts between the multi-line gcode a user actually edits and the
// single semicolon-joined string UGS stores/executes (MacroHelper.java's
// executeCustomGcode: strips newlines, then splits on ";" - so ";" is the
// ONLY command separator in storage, not an editing convenience).
//
// This deliberately treats ";" the way real gcode does - as the start of an
// end-of-line comment - rather than as a raw delimiter, so a normal-looking
// macro line like "G0 X10 ; move to start" converts safely instead of the
// comment's own ";" being mistaken for a second command separator.

// Storage -> editor: one command per line, in the order they'll execute.
export function macroGcodeToEditorText(gcode: string | undefined | null): string {
  if (!gcode) {
    return "";
  }
  return gcode
    .split(";")
    .map((command) => command.trim())
    .filter((command) => command.length > 0)
    .join("\n");
}

// Editor -> storage: strip any ";"-comment from each line (real gcode
// syntax - everything from the first ";" onward isn't sent), drop empty
// lines, then join what's left with ";" to match how MacroHelper expects to
// find it.
export function editorTextToMacroGcode(text: string): string {
  return text
    .split(/\r?\n/)
    .map((line) => {
      const commentIndex = line.indexOf(";");
      const command = commentIndex === -1 ? line : line.slice(0, commentIndex);
      return command.trim();
    })
    .filter((command) => command.length > 0)
    .join(";");
}
