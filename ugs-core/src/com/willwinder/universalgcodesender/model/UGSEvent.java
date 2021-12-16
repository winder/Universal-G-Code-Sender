/*
    Copyright 2012-2018 Will Winder

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

import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * These objects are passed around by the GUI API to notify listeners of state
 * changes.
 *
 * @author wwinder
 */
public class UGSEvent {
    private final EventType evt;
    private ControlState controlState = null;
    private Position probePosition = null;
    private ControllerStatus controllerStatus = null;

    public enum EventType {
        STATE_EVENT,
        FILE_EVENT,
        SETTING_EVENT,
        FIRMWARE_SETTING_EVENT,
        PROBE_EVENT,
        CONTROLLER_STATUS_EVENT,
        ALARM_EVENT,
        /**
         * An event type intended to be used to get controller state changes
         */
        CONTROLLER_STATE_EVENT
    }

    public enum ControlState {
        COMM_DISCONNECTED,
        COMM_IDLE,
        COMM_SENDING,
        COMM_SENDING_PAUSED,
        COMM_CHECK
    }

    public EventType getEventType(){
        return evt;
    }

    public boolean isStateChangeEvent() {
        return EventType.STATE_EVENT.equals(evt);
    }

    public boolean isFileChangeEvent() {
        return EventType.FILE_EVENT.equals(evt);
    }

    public boolean isSettingChangeEvent() {
        return EventType.SETTING_EVENT.equals(evt);
    }

    public boolean isFirmwareSettingEvent() {
        return EventType.FIRMWARE_SETTING_EVENT.equals(evt);
    }

    public boolean isProbeEvent() {
        return EventType.PROBE_EVENT.equals(evt);
    }

    public boolean isControllerStatusEvent() {
        return EventType.CONTROLLER_STATUS_EVENT.equals(evt);
    }

    /**
     * Create a new event of given type. STATE_EVENT and FILE_EVENT have
     * required parameters, so a runtime exception will be thrown if they are
     * specified with this constructor.
     * @param type 
     */
    public UGSEvent(EventType type) {
        evt = type;
        switch (evt) {
            case STATE_EVENT:
            case PROBE_EVENT:
            case CONTROLLER_STATUS_EVENT:
                throw new RuntimeException("Missing parameters for " + type + " event.");
        }
    }

    /**
     * Create a control state event.
     * @param state the new state.
     */
    public UGSEvent(ControlState state) {
        evt = EventType.STATE_EVENT;
        controlState = state;
    }

    /**
     * Create a probe position event.
     * PROBE_POSITION: This event indicates the tool location after a probe.
     * @param probePosition 
     */
    public UGSEvent(Position probePosition) {
        evt = EventType.PROBE_EVENT;
        this.probePosition = probePosition;
    }

    /**
     * Create a controller status event.
     */
    public UGSEvent(ControllerStatus controllerStatus) {
        evt = EventType.CONTROLLER_STATUS_EVENT;
        this.controllerStatus = controllerStatus;
    }

    // Getters
    public ControlState getControlState() {
        return controlState;
    }

    public Position getProbePosition() {
        return probePosition;
    }

    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
