/*
    Copyright 2013-2018 Will Winder

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
package com.willwinder.universalgcodesender;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Common utils for TinyG controllers
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class TinyGUtils {

    public static final byte COMMAND_PAUSE = '!';
    public static final byte COMMAND_RESUME = '~';
    public static final byte COMMAND_STATUS = '?';
    public static final byte COMMAND_QUEUE_FLUSH = '%';
    public static final byte COMMAND_KILL_JOB = 0x04;
    public static final byte COMMAND_ENQUIRE_STATUS = 0x05;
    public static final byte COMMAND_RESET = 0x18;

    public static final String COMMAND_STATUS_REPORT = "{sr:n}";
    public static final String COMMAND_KILL_ALARM_LOCK = "{clear:n}";

    public static final String FIELD_FIRMWARE_VERSION = "fv";
    public static final String FIELD_RESPONSE = "r";

    public static final String FIELD_STATUS_RESULT = "sr";
    public static final String FIELD_STATUS_RESULT_UNIT = "unit";
    public static final String FIELD_STATUS_RESULT_POSX = "posx";
    public static final String FIELD_STATUS_REPORT_POSY = "posy";
    public static final String FIELD_STATUS_REPORT_POSZ = "posz";
    public static final String FIELD_STATUS_REPORT_VELOCITY = "vel";
    public static final String FIELD_STATUS_REPORT_STATUS = "stat";

    private static JsonParser parser = new JsonParser();

    public static JsonObject jsonToObject(String response) {
        return parser.parse(response).getAsJsonObject();
    }

    public static boolean isTinyGVersion(JsonObject response) {
        if (response.has(FIELD_RESPONSE)) {
            JsonObject jo = response.getAsJsonObject(FIELD_RESPONSE);
            if (jo.has(FIELD_FIRMWARE_VERSION)) {
                return true;
            }
        }
        return false;
    }

    public static String getVersion(JsonObject response) {
        if (response.has(FIELD_RESPONSE)) {
            JsonObject jo = response.getAsJsonObject(FIELD_RESPONSE);
            if (jo.has(FIELD_FIRMWARE_VERSION)) {
                return jo.get(FIELD_FIRMWARE_VERSION).getAsString();
            }
        }
        return "";
    }

    public static boolean isRestartingResponse(JsonObject response) {
        if (response.has(FIELD_RESPONSE)) {
            JsonObject jo = response.getAsJsonObject(FIELD_RESPONSE);
            if (jo.has("msg")) {
                String msg = jo.get("msg").getAsString();
                return StringUtils.equals(msg, "Loading configs from EEPROM");
            }
        }
        return false;
    }

    public static boolean isReadyResponse(JsonObject response) {
        if (response.has(FIELD_RESPONSE)) {
            JsonObject jo = response.getAsJsonObject(FIELD_RESPONSE);
            if (jo.has("msg")) {
                String msg = jo.get("msg").getAsString();
                return StringUtils.equals(msg,"SYSTEM READY");
            }
        }
        return false;
    }

    public static boolean isStatusResponse(JsonObject response) {
        return response.has("sr");
    }

    /**
     * Parses the TinyG status result response and creates a new current controller status
     *
     * @param lastControllerStatus the last controller status to update
     * @param response             the response string from the controller
     * @return a new updated controller status
     */
    public static ControllerStatus updateControllerStatus(final ControllerStatus lastControllerStatus, final JsonObject response) {
        if (response.has(FIELD_STATUS_RESULT)) {
            JsonObject statusResultObject = response.getAsJsonObject(FIELD_STATUS_RESULT);

            Position machineCoord = lastControllerStatus.getMachineCoord();
            if (statusResultObject.has(FIELD_STATUS_RESULT_POSX)) {
                machineCoord.setX(statusResultObject.get(FIELD_STATUS_RESULT_POSX).getAsDouble());
            }

            if (statusResultObject.has(FIELD_STATUS_REPORT_POSY)) {
                machineCoord.setY(statusResultObject.get(FIELD_STATUS_REPORT_POSY).getAsDouble());
            }

            if (statusResultObject.has(FIELD_STATUS_REPORT_POSZ)) {
                machineCoord.setZ(statusResultObject.get(FIELD_STATUS_REPORT_POSZ).getAsDouble());
            }

            if (statusResultObject.has(FIELD_STATUS_RESULT_UNIT)) {
                UnitUtils.Units units = statusResultObject.get(FIELD_STATUS_RESULT_UNIT).getAsInt() == 0 ? UnitUtils.Units.INCH : UnitUtils.Units.MM;
                machineCoord = new Position(machineCoord.getX(), machineCoord.getY(), machineCoord.getZ(), units);
            }

            Double feedSpeed = lastControllerStatus.getFeedSpeed();
            if (statusResultObject.has(FIELD_STATUS_REPORT_VELOCITY)) {
                feedSpeed = statusResultObject.get(FIELD_STATUS_REPORT_VELOCITY).getAsDouble();
            }

            ControllerState state = lastControllerStatus.getState();
            String stateString = lastControllerStatus.getStateString();
            if (statusResultObject.has(FIELD_STATUS_REPORT_STATUS)) {
                state = getState(statusResultObject.get(FIELD_STATUS_REPORT_STATUS).getAsInt());
                stateString = getStateAsString(statusResultObject.get(FIELD_STATUS_REPORT_STATUS).getAsInt());
            }

            Double spindleSpeed = lastControllerStatus.getSpindleSpeed();
            ControllerStatus.OverridePercents overrides = lastControllerStatus.getOverrides();
            Position workCoordinateOffset = lastControllerStatus.getWorkCoordinateOffset();
            ControllerStatus.EnabledPins enabledPins = lastControllerStatus.getEnabledPins();
            ControllerStatus.AccessoryStates accessoryStates = lastControllerStatus.getAccessoryStates();

            return new ControllerStatus(stateString, state, machineCoord, machineCoord, feedSpeed, spindleSpeed, overrides, workCoordinateOffset, enabledPins, accessoryStates);
        }

        return lastControllerStatus;
    }

    /**
     * Maps between the TinyG state to a ControllerState
     *
     * @param state the state flag from a TinyGController
     * @return a corresponding ControllerState
     */
    private static ControllerState getState(int state) {
        switch (state) {
            case 0: // Machine is initializing
                return ControllerState.UNKNOWN;
            case 1: // Machine is ready for use
                return ControllerState.IDLE;
            case 2: // Machine is in alarm state
                return ControllerState.ALARM;
            case 3: // Machine has encountered program stop
                return ControllerState.IDLE;
            case 4: // Machine has encountered program end
                return ControllerState.IDLE;
            case 5: // Machine is running
                return ControllerState.RUN;
            case 6: // Machine is holding
                return ControllerState.HOLD;
            case 7: // Machine is in probing operation
                return ControllerState.UNKNOWN;
            case 8: // Reserved for canned cycles (not used)
                return ControllerState.UNKNOWN;
            case 9: // Machine is in a homing cycle
                return ControllerState.HOME;
            case 10: // Machine is in a jogging cycle
                return ControllerState.JOG;
            case 11: // Machine is in safety interlock hold
                return ControllerState.UNKNOWN;
            case 12: // Machine is in shutdown state. Will not process commands
                return ControllerState.UNKNOWN;
            case 13: // Machine is in panic state. Needs to be physically reset
                return ControllerState.ALARM;
            default:
                return ControllerState.UNKNOWN;
        }
    }

    private static String getStateAsString(int state) {
        ControllerState controllerState = getState(state);
        return controllerState.name();
    }

    /**
     * Gets a ControlState based on the ControllerState
     * @param controllerState the currently reported controller state from the controller
     * @return the control state for the current ControllerState
     */
    public static UGSEvent.ControlState getStateFromControllerStatus(ControllerState controllerState) {
        switch (controllerState) {
            case IDLE:
            case DOOR:
            case ALARM:
            case SLEEP:
                return UGSEvent.ControlState.COMM_IDLE;
            case RUN:
            case JOG:
            case HOME:
                return UGSEvent.ControlState.COMM_SENDING;
            case CHECK:
                return UGSEvent.ControlState.COMM_CHECK;
            case HOLD:
                return UGSEvent.ControlState.COMM_SENDING_PAUSED;
            default:
                return UGSEvent.ControlState.COMM_DISCONNECTED;
        }
    }
}
