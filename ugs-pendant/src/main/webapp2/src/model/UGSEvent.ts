import { ControllerStateEvent } from "./ControllerStateEvent";
import { ControllerStatusEvent } from "./ControllerStatusEvent";

export type UGSEvent = {
  eventType: "ControllerStatusEvent" | "ControllerStateEvent" | "SettingChangedEvent";
  event: ControllerStateEvent | ControllerStatusEvent;
};
