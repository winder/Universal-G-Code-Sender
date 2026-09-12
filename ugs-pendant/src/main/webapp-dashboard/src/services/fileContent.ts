export const getFileContent = (): Promise<string> => {
  return fetch("/api/v1/files/getFileContent").then((response) => {
    if (!response.ok) {
      throw new Error(`Couldn't load file content (${response.status})`);
    }
    return response.text();
  });
};

export const saveFileContent = (content: string): Promise<void> => {
  const request = {
    method: "POST",
    headers: {
      "Content-Type": "text/plain",
    },
    body: content,
  };

  return fetch("/api/v1/files/saveFileContent", request).then((response) => {
    if (!response.ok) {
      throw new Error(`Couldn't save file content (${response.status})`);
    }
  });
};

export const saveFileContentAs = (filename: string, content: string): Promise<void> => {
  const request = {
    method: "POST",
    headers: {
      "Content-Type": "text/plain",
    },
    body: content,
  };

  return fetch(
    `/api/v1/files/saveFileContentAs?filename=${encodeURIComponent(filename)}`,
    request
  ).then((response) => {
    if (!response.ok) {
      throw new Error(`Couldn't save file as "${filename}" (${response.status})`);
    }
  });
};
