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

  const blob = new Blob([content], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
  return true;
};
