export const getFileContent = (file: string): Promise<string> => {
  return fetch(`/api/v1/files/getFileContent?file=${encodeURIComponent(file)}`).then((response) =>
    response.text()
  );
};

export const saveFileContent = (file: string, content: string): Promise<void> => {
  const request = {
    method: "POST",
    headers: {
      "Content-Type": "text/plain",
    },
    body: content,
  };

  return fetch(`/api/v1/files/saveFileContent?file=${encodeURIComponent(file)}`, request).then();
};
