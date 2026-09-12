import {socketActions} from "./socketSlice";
import {Socket} from "../utils/Socket";
import {UGSEvent} from "../model/UGSEvent";
import {ControllerStatusEvent} from "../model/ControllerStatusEvent";
import {statusActions} from "./statusSlice";
import {
    Action,
    MiddlewareAPI,
    ThunkDispatch,
    ThunkMiddleware,
} from "@reduxjs/toolkit";
import {RootState} from "./store";
import {getSettings} from "./settingsSlice";
import {fetchStatus} from "./statusSlice";
import {fetchFileStatus} from "./fileStatusSlice";
import {consoleActions} from "./consoleSlice.ts";
import {CommandEvent} from "../model/CommandEvent.ts";
import {alarmActions} from "./alarmSlice.ts";
import {AlarmEvent} from "../model/AlarmEvent.ts";
import {ConsoleMessageEvent} from "../model/ConsoleMessageEvent.ts";
import {FileStateEvent} from "../model/FileStateEvent.ts";
import {uiActions} from "./uiSlice.ts";

// The currently-connected socket, if any - lets setVerboseEnabled (dispatched
// well after the "connect" action that created this) reach it directly,
// since the socket itself only exists inside this middleware's closure.
let activeSocket: Socket | undefined;

let fetchStatusTimer: number;
let debounceTime = 500;
const fetchSettingsDebounce = (
    store: MiddlewareAPI<ThunkDispatch<RootState, void, Action>, RootState>,
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
        // Toggling verbose output just needs to tell the already-open socket
        // about it - not something that opens/closes a connection, so it's
        // handled here instead of going through consoleSlice's own reducer
        // path unassisted.
        if (consoleActions.setVerboseEnabled.match(action)) {
            activeSocket?.send(action.payload ? "verbose:on" : "verbose:off");
            return next(action);
        }

        // Not a socket action
        if (!socketActions.connect.match(action)) {
            return next(action);
        }

        const socket = new Socket();
        activeSocket = socket;
        socket.connect("ws://" + location.host + "/ws/v1/events");

        socket.on("open", () => {
            console.log("Established connection");
            store.dispatch(socketActions.connectionEstablished());
            store.dispatch(getSettings());
            // Without this, a freshly loaded page starts from the default redux
            // state (state: "DISCONNECTED") and only corrects itself once the
            // backend happens to push a status change - so reloading while
            // already connected could get stuck showing "disconnected" until
            // something on the machine changed.
            store.dispatch(fetchStatus());
            // Verbose opt-in is per-connection on the server (EventsSocket.java
            // forgets it on close) - re-assert it after every reconnect so the
            // setting doesn't silently revert to off from the user's perspective.
            if (store.getState().console.verboseEnabled) {
                socket.send("verbose:on");
            }

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
            store.dispatch(socketActions.messageReceived());
            if (ugsEvent.eventType === "ControllerStatusEvent") {
                store.dispatch(
                    statusActions.setStatus(
                        (ugsEvent.event as ControllerStatusEvent).status,
                    ),
                );
            } else if (ugsEvent.eventType === "Pong") {
                // No-op - messageReceived() above already recorded this as a
                // live heartbeat, which is the only reason it's sent.
            } else if (ugsEvent.eventType === "ConsoleMessageEvent") {
                store.dispatch(
                    consoleActions.addMessage({
                        type: "verbose",
                        text: (ugsEvent.event as ConsoleMessageEvent).message,
                    }),
                );
            } else if (ugsEvent.eventType === "AlarmEvent") {
                store.dispatch(alarmActions.setAlarm((ugsEvent.event as AlarmEvent).alarm));
            } else if (ugsEvent.eventType === "FileStateEvent") {
                store.dispatch(fetchFileStatus());
                // FILE_LOADED specifically (not OPENING_FILE/FILE_LOADING,
                // which fire earlier) is when the backend's own doc comment
                // says the processed file is actually ready - see
                // VisualizerResource.getToolpath, which reads that file and
                // otherwise races ahead of it existing.
                if ((ugsEvent.event as FileStateEvent).fileState === "FILE_LOADED") {
                    store.dispatch(uiActions.bumpToolpathVersion());
                }
            } else if (ugsEvent.eventType === "SettingChangedEvent") {
                fetchSettingsDebounce(store);
            } else if (ugsEvent.eventType === "CommandEvent") {
                store.dispatch(fetchFileStatus());
                const commandEvent: CommandEvent = ugsEvent.event;
                if (commandEvent.commandEventType === "COMMAND_COMPLETE") {
                    store.dispatch(
                        consoleActions.addMessage({
                            type: commandEvent.command.isOk ? "ok" : "error",
                            text: ugsEvent.event.command.response,
                        }),
                    );
                    // Coolant state isn't in the WebSocket status push (see
                    // Status.floodCoolantOn on the Java side), so it's only as
                    // fresh as the last getStatus() fetch. Refresh on ANY
                    // completed M7/M8/M9 - not just ones sent from this
                    // dashboard - so a command typed into the native UGS
                    // console (or sent by any other client) still keeps the
                    // Coolant button in sync instead of going stale until the
                    // next click here.
                    if (/\bM0?[789]\b/i.test(commandEvent.command.command)) {
                        store.dispatch(fetchStatus());
                    }
                } else if (commandEvent.commandEventType === "COMMAND_SENT") {
                    store.dispatch(
                        consoleActions.addMessage({
                            type: "info",
                            text: "> " + ugsEvent.event.command.command,
                        }),
                    );
                }
            } else {
                console.info("Unknown event", messageEvent.data);
            }
        });

        return next(action);
    };
