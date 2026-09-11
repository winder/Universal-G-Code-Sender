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
} | {
  // The server's reply to the client's periodic keepalive "ping" - carries
  // no event payload, just its arrival is the point (see socketMiddleware.ts).
  eventType: "Pong";
} | UGSCommandEvent | UGSControllerStatusEvent | UGSControllerStateEvent | UGSFileStateEvent | UGSAlarmEvent;
