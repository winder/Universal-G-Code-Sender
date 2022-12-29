/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.events.AlarmEvent;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.model.events.ProbeEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.model.events.StreamEvent;
import com.willwinder.universalgcodesender.model.events.StreamEventType;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.SettingChangeListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that is responsible for listening to various events from the controller and backend system and
 * dispatch them as UGS events.
 *
 * @author Joacim Breiler
 */
public class UGSEventDispatcher implements ControllerListener, IFirmwareSettingsListener, SettingChangeListener {
    private static final Logger LOGGER = Logger.getLogger(UGSEventDispatcher.class.getSimpleName());

    private final List<UGSEventListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * A cached instance of the controller status for preventing duplicate status events to be dispatched
     */
    private ControllerStatus controllerStatus = new ControllerStatus();

    public void sendUGSEvent(UGSEvent event) {
        LOGGER.log(Level.FINEST, "Sending event {0}.", event.getClass().getSimpleName());
        listeners.forEach(l -> {
            try {
                l.UGSEvent(event);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not dispatch the event {0} to the listener {1}",
                        new String[]{event.getClass().getSimpleName(), l.getClass().getSimpleName()});
            }
        });
    }

    public void addListener(UGSEventListener listener) {
        if (!listeners.contains(listener)) {
            LOGGER.log(Level.INFO, "Adding UGSEvent listener: {0}", listener.getClass().getSimpleName());
            listeners.add(listener);
        }
    }

    public void removeListener(UGSEventListener listener) {
        if (listeners.contains(listener)) {
            LOGGER.log(Level.INFO, "Removing UGSEvent listener: {0}", listener.getClass().getSimpleName());
            listeners.remove(listener);
        }
    }

    @Override
    public void streamCanceled() {
        sendUGSEvent(new StreamEvent(StreamEventType.STREAM_CANCELED));
    }

    @Override
    public void streamStarted() {
        sendUGSEvent(new StreamEvent(StreamEventType.STREAM_STARTED));
    }

    @Override
    public void streamPaused() {
        sendUGSEvent(new StreamEvent(StreamEventType.STREAM_PAUSED));
    }

    @Override
    public void streamResumed() {
        sendUGSEvent(new StreamEvent(StreamEventType.STREAM_RESUMED));
    }

    @Override
    public void streamComplete(String filename) {
        sendUGSEvent(new StreamEvent(StreamEventType.STREAM_COMPLETE));
        sendUGSEvent(new FileStateEvent(FileState.FILE_STREAM_COMPLETE, filename));
    }

    @Override
    public void receivedAlarm(Alarm alarm) {
        sendUGSEvent(new AlarmEvent(alarm));
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        sendUGSEvent(new CommandEvent(CommandEventType.COMMAND_SKIPPED, command));
    }

    @Override
    public void commandSent(GcodeCommand command) {
        sendUGSEvent(new CommandEvent(CommandEventType.COMMAND_SENT, command));
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        sendUGSEvent(new CommandEvent(CommandEventType.COMMAND_COMPLETE, command));
    }

    @Override
    public void probeCoordinates(Position p) {
        sendUGSEvent(new ProbeEvent(p));
    }

    @Override
    public void statusStringListener(ControllerStatus status) {
        ControllerStatus oldStatus = this.controllerStatus;
        controllerStatus = status;

        if (oldStatus.getState() != status.getState()) {
            sendUGSEvent(new ControllerStateEvent(status.getState(), oldStatus.getState()));
        }

        if (!oldStatus.equals(status)) {
            sendUGSEvent(new ControllerStatusEvent(status, oldStatus));
        }
    }

    @Override
    public void onUpdatedFirmwareSetting(FirmwareSetting setting) {
        sendUGSEvent(new FirmwareSettingEvent(setting));
    }

    @Override
    public void settingChanged() {
        sendUGSEvent(new SettingChangedEvent());
    }
}
