/**
 * Retreives a list of available ports
 *
 * @returns a list of available ports
 */
export const getPortList = (): Promise<string[]> => {
  return fetch("api/v1/machine/getPortList")
    .then((response) => response.json())
    .then((data) => data as string[]);
};

/**
 * Retreives the currently selected port
 *
 * @returns the currently selected port
 */
export const getSelectedPort = (): Promise<string> => {
  return fetch("api/v1/machine/getSelectedPort")
    .then((response) => response.json())
    .then((data) => data?.selectedPort ?? "");
};

export const getSelectedFirmware = (): Promise<string> => {
  return fetch("api/v1/machine/getSelectedFirmware")
    .then((response) => response.json())
    .then((data) => data?.selectedFirmware ?? "");
};

export const getSelectedBaudRate = (): Promise<string> => {
  return fetch("api/v1/machine/getSelectedBaudRate")
    .then((response) => response.json())
    .then((data) => data?.selectedBaudRate ?? "");
};

export const getFirmwareList = (): Promise<string[]> => {
  return fetch("api/v1/machine/getFirmwareList")
    .then((response) => response.json())
    .then((data) => data as string[]);
};

export const getBaudRateList = (): Promise<string[]> => {
  return fetch("api/v1/machine/getBaudRateList")
    .then((response) => response.json())
    .then((data) => data as string[]);
};

export const connect = (): Promise<void> => {
  return fetch("api/v1/machine/connect").then();
};

export const disconnect = (): Promise<void> => {
  return fetch("api/v1/machine/disconnect").then();
};

export const softReset = (): Promise<void> => {
  return fetch("api/v1/machine/softReset").then();
};

export const killAlarm = (): Promise<void> => {
  return fetch("api/v1/machine/killAlarm").then();
};

export const homeMachine = (): Promise<void> => {
  return fetch("api/v1/machine/homeMachine").then();
};

export const returnToZero = (): Promise<void> => {
  return fetch("api/v1/machine/returnToZero").then();
};

export const jog = (x: number, y: number, z: number): Promise<void> => {
  return fetch(`api/v1/machine/jog?x=${x}&y=${y}&z=${z}`).then();
};

export const resetToZero = (
  axis?: "A" | "B" | "C" | "X" | "Y" | "Z"
): Promise<void> => {
  if (axis) {
    return fetch(`api/v1/machine/resetToZero?axis=${axis}`).then();
  }
  return fetch(`api/v1/machine/resetToZero`).then();
};


export const cancelSend = (): Promise<void> => {
  return fetch(`api/v1/files/cancel`).then();
};

export const pause = (): Promise<void> => {
  return fetch(`api/v1/files/pause`).then();
};

export const send = (): Promise<void> => {
  const url = "api/v1/files/send";
  const request = {
    method: "POST",
    headers: {
      'Content-Type': "application/json"
    }
  };

  return fetch(url, request).then();
};