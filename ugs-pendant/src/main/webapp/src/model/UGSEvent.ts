import { ControllerStateEvent } from "./ControllerStateEvent";
import { ControllerStatusEvent } from "./ControllerStatusEvent";
import { FileStateEvent } from "./FileStateEvent";

export type UGSEvent = {
  eventType: "ControllerStatusEvent" | "ControllerStateEvent" | "SettingChangedEvent" | "FileStateEvent";
  event: ControllerStateEvent | ControllerStatusEvent | FileStateEvent;
};
