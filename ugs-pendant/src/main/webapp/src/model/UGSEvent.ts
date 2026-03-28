import { ControllerStateEvent } from "./ControllerStateEvent";
import { ControllerStatusEvent } from "./ControllerStatusEvent";
import { FileStateEvent } from "./FileStateEvent";
import {CommandEvent} from "./CommandEvent.ts";

type UGSControllerStatusEvent = {
  eventType: "ControllerStatusEvent",
  event: ControllerStatusEvent;
}

type UGSCommandEvent = {
  eventType: "CommandEvent";
  event: CommandEvent;
};

type UGSControllerStateEvent = {
  eventType: "ControllerStateEvent";
  event: ControllerStateEvent;
};

type UGSFileStateEvent = {
  eventType: "FileStateEvent";
  event: FileStateEvent;
};

export type UGSEvent = {
  eventType:  "SettingChangedEvent";
} | UGSCommandEvent | UGSControllerStatusEvent | UGSControllerStateEvent | UGSFileStateEvent;
