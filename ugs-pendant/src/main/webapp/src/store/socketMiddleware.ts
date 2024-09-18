import { socketActions } from "./socketSlice";
import { Socket } from "../utils/Socket";
import { UGSEvent } from "../model/UGSEvent";
import { ControllerStatusEvent } from "../model/ControllerStatusEvent";
import { statusActions } from "./statusSlice";
import {
  Action,
  MiddlewareAPI,
  ThunkDispatch,
  ThunkMiddleware,
} from "@reduxjs/toolkit";
import { RootState } from "./store";
import { getSettings } from "./settingsSlice";
import { fetchFileStatus } from "./fileStatusSlice";

let fetchStatusTimer: number;
let debounceTime = 500;
const fetchSettingsDebounce = (
  store: MiddlewareAPI<ThunkDispatch<RootState, void, Action>, RootState>
) => {
  if (fetchStatusTimer) {
    clearTimeout(fetchStatusTimer);
  }

  fetchStatusTimer = window.setTimeout(() => {
    console.log("Fetching settings");
    store.dispatch(getSettings());
  }, debounceTime);
};

/**
 * This is a TypeScript example of a simple logging middleware for Redux.
 * It will log every action that passes through the middleware pipeline.
 */
export const socketMiddleware: ThunkMiddleware<RootState, Action, void> =
  (store) => (next) => (action) => {
    // Not a socket action
    if (!socketActions.connect.match(action)) {
      return next(action);
    }

    const socket = new Socket();
    socket.connect("ws://" + location.host + "/ws/v1/events");

    socket.on("open", () => {
      console.log("Established connection");
      store.dispatch(socketActions.connectionEstablished());
      store.dispatch(getSettings());

      const timer = setInterval(() => {
        if (!socket.isConnected()) {
          clearInterval(timer);
          return;
        }

        socket.send("ping");
      }, 4000);
    });

    socket.on("close", () => {
      console.log("Closing websocket");
      store.dispatch(socketActions.connectionClosed());
    });

    socket.onMessage((messageEvent: MessageEvent) => {
      const ugsEvent = JSON.parse(messageEvent.data) as UGSEvent;
      if (ugsEvent.eventType === "ControllerStatusEvent") {
        store.dispatch(
          statusActions.setStatus(
            (ugsEvent.event as ControllerStatusEvent).status
          )
        );
      } else if (ugsEvent.eventType === "FileStateEvent") {
        store.dispatch(fetchFileStatus());
      } else if (ugsEvent.eventType === "SettingChangedEvent") {
        fetchSettingsDebounce(store);
      } else {
        console.info("Unknown event", messageEvent.data);
      }
    });

    return next(action);
  };
