/*import { Middleware, MiddlewareAPI } from "@reduxjs/toolkit";
import { RootState } from "./store";
import { Socket } from "../utils/Socket";
import { ControllerStateEvent } from "../model/ControllerStateEvent";
import { UGSEvent } from "../model/UGSEvent";
import { ControllerStatusEvent } from "../model/ControllerStatusEvent";

const createSocketMiddleware = (socket: Socket) => {
  socket.connect("ws://" + location.host + "/ws/v1/events");

  socket.on("open", (event) => {
    console.log(event);
    setInterval(() => {
      socket.send("ping");
    }, 4000);
  });

  socket.onMessage((messageEvent: MessageEvent) => {
    const ugsEvent = JSON.parse(messageEvent.data) as UGSEvent;
    if (ugsEvent.eventType === "ControllerStateEvent") {
      console.log(
        ugsEvent.eventType,
        (ugsEvent.event as ControllerStateEvent).state
      );
    } else if (ugsEvent.eventType === "ControllerStatusEvent") {
      console.log(
        ugsEvent.eventType,
        (ugsEvent.event as ControllerStatusEvent).status.state
      );
    } else {
      console.log("Unknown event", messageEvent.data);
    }
  });

  socket.on("close", (event) => {
    console.log("...and I say goodbye!");
  });

  //socket.send('A message')
  //socket.disconnect()
};

const socket = new Socket();

export const socketMiddleware: Middleware<void> =
  (api : MiddlewareAPI<void>) => 
  (next : Dispatch<void>) => <A extends Action>(action: A) => {
   
    return next(action);
  };*/

import { socketActions } from "./socketSlice";
import { Socket } from "../utils/Socket";
import { UGSEvent } from "../model/UGSEvent";
import { ControllerStatusEvent } from "../model/ControllerStatusEvent";
import { fetchStatus, statusActions } from "./statusSlice";
import { Action, ThunkMiddleware } from "@reduxjs/toolkit";
import { RootState } from "./store";
import { getSettings } from "./settingsSlice";

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
      console.log("Closing!!");
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
      } else if (ugsEvent.eventType === "SettingChangedEvent") {
        store.dispatch(fetchStatus());
      } else {
        console.info("Unknown event", messageEvent.data);
      }
    });

    return next(action);
  };
