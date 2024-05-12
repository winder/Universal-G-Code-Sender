import { FileStatus } from "../model/FileStatus";
import { WorkspaceFileList } from "../model/WorkspaceFileList";

export const getFileStatus = () => {
  return fetch("api/v1/files/getFileStatus")
    .then((response) => response.text())
    .then((text) => {
      // Workaround for a quirk where the backend returns "NaN" if it is not a number
      let convertedText = text.replaceAll('"NaN"', "null");
      return JSON.parse(convertedText) as FileStatus;
    })
    .then((response) => {
      return response;
    });
};

export const send = () => {
  const url = "api/v1/files/send";
  const request = {
    method: "POST",
  };

  return fetch(url, request).then();
};

export const stop = () => {
  return fetch("api/v1/files/cancel").then();
};

export const pause = () => {
  return fetch("api/v1/files/pause").then();
};

export const getWorkspaceFileList = (): Promise<WorkspaceFileList> => {
  return fetch("api/v1/files/getWorkspaceFileList").then((response) =>
    response.json()
  );
};

export const openWorkspaceFile = (fileName: string): Promise<void> => {
  const request = {
    method: "POST",
  };
  return fetch(
    `api/v1/files/openWorkspaceFile?file=${fileName}`,
    request
  ).then();
};

export const uploadAndOpen = (file: File): Promise<void> => {
  let formData: FormData = new FormData();
  formData.append("file", file, file.name);

  const request = {
    method: "POST",
    body: formData,
  };
  return fetch(`api/v1/files/uploadAndOpen`, request).then();
};
