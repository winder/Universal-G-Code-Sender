import { Settings } from "../model/Settings";

export const fetchSettings = (): Promise<Settings> => {
  return fetch("api/v1/settings/getSettings").then((response) =>
    response.json()
  );
};

export const putSettings = (settings: Settings): Promise<Settings> => {
  const url = "api/v1/settings/setSettings";
  const request = {
    method: "POST",
    headers: {
      'Content-Type': "application/json"
    },
    body: JSON.stringify(settings),
  };

  return fetch(url, request).then(() => settings);
};
