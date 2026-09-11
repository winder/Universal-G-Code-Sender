/*
    Copyright 2023 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.willwinder.universalgcodesender.pendantui.v1.ws;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.pendantui.BackendProvider;
import com.willwinder.universalgcodesender.pendantui.v1.model.Event;
import com.willwinder.universalgcodesender.utils.Settings;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@ClientEndpoint
@ServerEndpoint(value = "/events")
public class EventsSocket implements UGSEventListener, MessageListener {

    private static final Logger LOGGER = Logger.getLogger(EventsSocket.class.getSimpleName());
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    // Sessions that opted into verbose console output (see onWebSocketMessage).
    // Off by default and per-session, not global - a client that never asks
    // for it should never receive the high-frequency VERBOSE traffic (raw
    // status polls, parser feedback) that a real connection generates.
    private final Set<String> verboseSessionIds = ConcurrentHashMap.newKeySet();
    private final Gson gson;

    public EventsSocket() {
        gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        if (BackendProvider.getBackendAPI() != null) {
            BackendProvider.getBackendAPI().addUGSEventListener(this);
            BackendProvider.getBackendAPI().addMessageListener(this);
        }
    }

    private static ControllerStatus convertToPreferredUnits(ControllerStatus controllerStatusEvent, UnitUtils.Units units) {
        return ControllerStatusBuilder.newInstance(controllerStatusEvent)
                .setMachineCoord(controllerStatusEvent.getMachineCoord().getPositionIn(units))
                .setWorkCoord(controllerStatusEvent.getWorkCoord().getPositionIn(units))
                .build();
    }

    @OnOpen
    public void onWebSocketConnect(Session session) {
        sessions.put(session.getId(), session);
        LOGGER.info("WebSocket Connected: " + session.getId());
    }

    @OnClose
    public void onWebSocketClose(Session session) {
        sessions.remove(session.getId());
        verboseSessionIds.remove(session.getId());
        LOGGER.info("WebSocket Closed: " + session.getId());
    }

    @OnError
    public void onWebSocketError(Session session, Throwable cause) {
        sessions.remove(session.getId());
        verboseSessionIds.remove(session.getId());
        LOGGER.log(Level.WARNING, cause, () -> "WebSocket Closed: " + session.getId());
    }

    // The client sends a "ping" text frame every few seconds purely to keep
    // the socket from being idle-timed-out. Answering it gives the dashboard
    // a heartbeat it can use to tell "connection is alive" apart from
    // "nothing has changed" - FluidNCController (and others) deliberately
    // skip dispatching a ControllerStatusEvent when the status hasn't
    // changed, so a genuinely idle machine can go a while with no status
    // traffic at all even on a perfectly healthy connection.
    //
    // "verbose:on"/"verbose:off" opts this specific session in or out of the
    // MessageType.VERBOSE stream forwarded from onMessage() below.
    @OnMessage
    public void onWebSocketMessage(Session session, String message) {
        if ("ping".equals(message)) {
            try {
                session.getBasicRemote().sendText("{\"eventType\":\"Pong\"}");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e, () -> "Could not send pong to session: " + session.getId());
            }
        } else if ("verbose:on".equals(message)) {
            verboseSessionIds.add(session.getId());
        } else if ("verbose:off".equals(message)) {
            verboseSessionIds.remove(session.getId());
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        try {
            String data = getEventAsJsonString(evt);
            sessions.values().forEach(session -> {
                try {
                    session.getBasicRemote().sendText(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MessageListener: the console-message stream (distinct from the
    // UGSEvent bus above) - this is where raw controller protocol traffic
    // (status polls, parser feedback, connection messages) flows. Only
    // VERBOSE is forwarded, and only to sessions that asked for it - INFO/
    // ERROR already reach the dashboard through CommandEvent/AlarmEvent.
    @Override
    public void onMessage(MessageType messageType, String message) {
        if (messageType != MessageType.VERBOSE || verboseSessionIds.isEmpty()) {
            return;
        }

        JsonObject event = new JsonObject();
        event.addProperty("message", message.stripTrailing());
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("eventType", "ConsoleMessageEvent");
        wrapper.add("event", event);
        String data = gson.toJson(wrapper);

        verboseSessionIds.forEach(sessionId -> {
            Session session = sessions.get(sessionId);
            if (session == null) {
                return;
            }
            try {
                session.getBasicRemote().sendText(data);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e, () -> "Could not send verbose message to session: " + sessionId);
            }
        });
    }

    private String getEventAsJsonString(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent controllerStatusEvent) {
            Settings settings = BackendProvider.getBackendAPI().getSettings();
            ControllerStatus currentStatus = convertToPreferredUnits(controllerStatusEvent.getStatus(), settings.getPreferredUnits());
            ControllerStatus previousStatus = convertToPreferredUnits(controllerStatusEvent.getPreviousStatus(), settings.getPreferredUnits());
            return gson.toJson(new Event(new ControllerStatusEvent(currentStatus, previousStatus))).replace(":NaN", ":null");
        }

        return gson.toJson(new Event(evt));
    }
}
