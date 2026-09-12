import { Macro } from "../model/Macro";

export const getMacroList = (): Promise<Macro[]> => {
  return fetch("/api/v1/macros/getMacroList").then((response) =>
    response.json()
  );
};

export const runMacro = (macro: Macro): Promise<void> => {
  const url = "/api/v1/macros/runMacro";
  const request = {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(macro),
  };

  return fetch(url, request).then();
};

// Replaces the entire macro list in one atomic save (create/update/delete/
// reorder are all just "here's the new full list") - mirrors how the native
// desktop Settings > Macros panel already persists edits. Returns the saved
// list as the backend sees it (e.g. after any server-side normalization).
export const saveMacroList = (macros: Macro[]): Promise<Macro[]> => {
  const url = "/api/v1/macros/saveMacroList";
  const request = {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(macros),
  };

  return fetch(url, request).then((response) => {
    if (!response.ok) {
      throw new Error(`Couldn't save macros (${response.status})`);
    }
    return response.json();
  });
};
