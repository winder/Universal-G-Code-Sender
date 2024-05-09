import { Macro } from "../model/Macro";

export const getMacroList = (): Promise<Macro[]> => {
  return fetch("api/v1/macros/getMacroList").then((response) =>
    response.json()
  );
};

export const runMacro = (macro: Macro): Promise<void> => {
  const url = "api/v1/macros/runMacro";
  const request = {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(macro),
  };

  return fetch(url, request).then();
};
