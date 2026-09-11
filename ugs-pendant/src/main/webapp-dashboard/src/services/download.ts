import { Macro } from "../model/Macro";
import { macroGcodeToEditorText, editorTextToMacroGcode } from "../utils/macroGcode";

// The File System Access API (Chrome/Edge/ChromeOS) is the only way a web page can
// show a real native "save to..." dialog and write straight to the picked location.
// It isn't in TypeScript's DOM lib yet, so `window` needs a narrow escape hatch here.
type SaveFilePickerWindow = Window & {
  showSaveFilePicker?: (options: {
    suggestedName: string;
    types: { description: string; accept: Record<string, string[]> }[];
  }) => Promise<{
    createWritable: () => Promise<{
      write: (data: string) => Promise<void>;
      close: () => Promise<void>;
    }>;
  }>;
};

export const supportsSaveFilePicker = () =>
  typeof window !== "undefined" && typeof (window as SaveFilePickerWindow).showSaveFilePicker === "function";

function triggerDownload(filename: string, content: string, mimeType: string) {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

/**
 * Saves content to a location the user picks on this device, via a real native "Save
 * As" dialog where the browser supports one (Chrome/Edge/ChromeOS - see
 * supportsSaveFilePicker), falling back to a plain browser download otherwise (Firefox,
 * Safari) - which browsers other than that also honor as a "choose a location" prompt
 * depends entirely on that browser's own download settings, outside this page's control.
 * Returns false if the user cancelled the picker rather than actually saving.
 */
export const saveToDevice = async (filename: string, content: string): Promise<boolean> => {
  const picker = (window as SaveFilePickerWindow).showSaveFilePicker;
  if (picker) {
    try {
      const handle = await picker({
        suggestedName: filename,
        types: [{ description: "G-code", accept: { "text/plain": [".gcode", ".nc", ".tap", ".ngc", ".txt"] } }],
      });
      const writable = await handle.createWritable();
      await writable.write(content);
      await writable.close();
      return true;
    } catch (err) {
      if (err instanceof DOMException && err.name === "AbortError") {
        return false;
      }
      throw err;
    }
  }

  triggerDownload(filename, content, "text/plain");
  return true;
};

function safeFilename(name: string): string {
  return name.replace(/[^a-z0-9_\-]+/gi, "_").replace(/^_+|_+$/g, "") || "macro";
}

// Single-macro export as plain gcode text - just the commands, human
// readable, for use outside the dashboard (e.g. loading into the visualizer
// or another sender). Not re-importable as a macro on its own since it
// drops the name/color/icon.
export const downloadSingleMacro = (macro: Macro) => {
  const body = macroGcodeToEditorText(macro.gcode);
  triggerDownload(`${safeFilename(macro.name)}.gcode`, body + "\n", "text/plain");
};

// Whole-list export/import uses UGS's own macro JSON array shape (the same
// format the native desktop app's Settings > Macros export produces), so
// files are interchangeable between the dashboard and the desktop UI.
export const downloadMacroList = (macros: Macro[]) => {
  triggerDownload("ugs-macros.json", JSON.stringify(macros, null, 2), "application/json");
};

export function parseMacroListFile(file: File): Promise<Macro[]> {
  return file.text().then((text) => {
    const data = JSON.parse(text);
    if (!Array.isArray(data)) {
      throw new Error("Expected a JSON array of macros");
    }
    return data.map((entry: Record<string, unknown>) => {
      if (typeof entry.gcode !== "string" || typeof entry.name !== "string") {
        throw new Error("Each macro needs at least a name and gcode");
      }
      // Round-trip through the editor conversion once so any semicolon
      // quirks (comments vs. separators) are normalized the same way the
      // rest of the dashboard treats gcode, rather than trusting raw input.
      const gcode = editorTextToMacroGcode(macroGcodeToEditorText(entry.gcode as string));
      return {
        uuid: typeof entry.uuid === "string" && entry.uuid ? entry.uuid : crypto.randomUUID(),
        name: entry.name as string,
        description: typeof entry.description === "string" ? entry.description : undefined,
        gcode,
        color: typeof entry.color === "string" ? entry.color : undefined,
        icon: typeof entry.icon === "string" ? entry.icon : undefined,
      } satisfies Macro;
    });
  });
}
