import { Status } from "../model/Status";

const getStatus = () => {
  return fetch("api/v1/status/getStatus")
    .then((response) => response.text())
    .then((text) => {
      // Workaround for a quirk where the backend returns "NaN" if it is not a number
      let convertedText = text.replaceAll('"NaN"', "null");
      return JSON.parse(convertedText) as Status;
    })
    .then((response) => {
      return response;
    });
};

export default getStatus;
