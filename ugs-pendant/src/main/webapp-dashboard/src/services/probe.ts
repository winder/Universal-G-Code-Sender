import { ProbeOperation, ProbeResult, ProbeSettings } from "../model/Probe";

export const getProbeSettings = (): Promise<ProbeSettings> => {
  return fetch("/api/v1/probe/getSettings").then((response) => response.json());
};

export const saveProbeSettings = (settings: ProbeSettings): Promise<void> => {
  const request = {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(settings),
  };
  return fetch("/api/v1/probe/saveSettings", request).then((response) => {
    if (!response.ok) {
      throw new Error(`Couldn't save probe settings (${response.status})`);
    }
  });
};

// Blocks until the probe operation completes (or fails) - there's no separate polling/websocket
// step for this, the REST call itself only returns once the machine has finished moving.
export const runProbe = (operation: ProbeOperation, maxTravel?: number): Promise<ProbeResult> => {
  const request = {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ operation, maxTravel }),
  };
  return fetch("/api/v1/probe/run", request).then((response) => {
    if (!response.ok) {
      throw new Error(`Couldn't run the probe (${response.status})`);
    }
    return response.json();
  });
};
