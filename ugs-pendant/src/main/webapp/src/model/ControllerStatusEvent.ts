import { Status } from "./Status";

export type ControllerStatusEvent = {
  status: Status;
  previousStatus: Status;
};
