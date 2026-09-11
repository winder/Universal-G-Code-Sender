import { ControllerStateEvent } from "./ControllerStateEvent";
import { ControllerStatusEvent } from "./ControllerStatusEvent";
import { FileStateEvent } from "./FileStateEvent";
import {CommandEvent} from "./CommandEvent.ts";
import {AlarmEvent} from "./AlarmEvent.ts";

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

type UGSAlarmEvent = {
  eventType: "AlarmEvent";
  event: AlarmEvent;
};

export type UGSEvent = {
  eventType:  "SettingChangedEvent";
} | UGSCommandEvent | UGSControllerStatusEvent | UGSControllerStateEvent | UGSFileStateEvent | UGSAlarmEvent;
