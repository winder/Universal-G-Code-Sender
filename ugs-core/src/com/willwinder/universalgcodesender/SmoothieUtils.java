package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmoothieUtils {

    public static final byte COMMAND_RESET = 0x18;
    /**
     * Parse state out of position string.
     */
    private static final String STATUS_STATE_REGEX = "(?<=\\<)[a-zA-z]*(?=[,])";
    private static final String GCODE_SET_COORDINATE_V9 = "G10 P0 L20";
    private static final Pattern STATUS_STATE_PATTERN = Pattern.compile(STATUS_STATE_REGEX);
    private static final Pattern MACHINE_PATTERN = Pattern.compile("(?<=MPos:)(-?\\d*\\..\\d*),(-?\\d*\\..\\d*),(-?\\d*\\..\\d*)");
    private static final Pattern WORK_PATTERN = Pattern.compile("(?<=WPos:)(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*)");

    private static final Pattern STATUS_PATTERN = Pattern.compile("\\<.*\\>");
    private static final Pattern PARSER_STATE_PATTERN = Pattern.compile("\\[.*\\]");

    public static final byte CANCEL_COMMAND = 0x18;



    protected static ControllerStatus getStatusFromStatusString(ControllerStatus lastStatus, final String status, UnitUtils.Units reportingUnits) {
        String stateString = "";
        Position MPos = null;
        Position WPos = null;
        Position WCO = null;

        ControllerStatus.OverridePercents overrides = null;
        ControllerStatus.EnabledPins pins = null;
        ControllerStatus.AccessoryStates accessoryStates = null;

        double feedSpeed = 0;
        double spindleSpeed = 0;
        if (lastStatus != null) {
            feedSpeed = lastStatus.getFeedSpeed();
            spindleSpeed = lastStatus.getSpindleSpeed();
        }
        boolean isOverrideReport = false;

        // Parse out the status messages.
        for (String part : status.substring(0, status.length() - 1).split("\\|")) {
            if (part.startsWith("<")) {
                int idx = part.indexOf(':');
                if (idx == -1)
                    stateString = part.substring(1);
                else
                    stateString = part.substring(1, idx);
            } else if (part.startsWith("MPos:")) {
                MPos = getPositionFromStatusString(status, MACHINE_PATTERN, reportingUnits);
            } else if (part.startsWith("WPos:")) {
                WPos = getPositionFromStatusString(status, WORK_PATTERN, reportingUnits);
            } else if (part.startsWith("Ov:")) {
                isOverrideReport = true;
                String[] overrideParts = part.substring(3).trim().split(",");
                if (overrideParts.length == 3) {
                    overrides = new ControllerStatus.OverridePercents(
                            Integer.parseInt(overrideParts[0]),
                            Integer.parseInt(overrideParts[1]),
                            Integer.parseInt(overrideParts[2]));
                }
            } else if (part.startsWith("F:")) {
                String[] feedStrings = StringUtils.split(part.substring(2), ",");
                if (feedStrings.length < 3) {
                    feedSpeed = 0;
                } else {
                    feedSpeed = Double.parseDouble(StringUtils.split(feedStrings[0], ",")[0]);
                }
            } else if (part.startsWith("FS:")) {
                String[] parts = part.substring(3).split(",");
                feedSpeed = Double.parseDouble(parts[0]);
                spindleSpeed = Double.parseDouble(parts[1]);
            } else if (part.startsWith("Pn:")) {
                String value = part.substring(part.indexOf(':') + 1);
                pins = new ControllerStatus.EnabledPins(value);
            } else if (part.startsWith("A:")) {
                String value = part.substring(part.indexOf(':') + 1);
                accessoryStates = new ControllerStatus.AccessoryStates(value);
            }
        }

        // Grab WCO from state information if necessary.
        if (WCO == null) {
            // Grab the work coordinate offset.
            if (lastStatus != null && lastStatus.getWorkCoordinateOffset() != null) {
                WCO = lastStatus.getWorkCoordinateOffset();
            } else {
                WCO = new Position(0, 0, 0, reportingUnits);
            }
        }

        // Calculate missing coordinate with WCO
        if (WPos == null) {
            WPos = new Position(MPos.x - WCO.x, MPos.y - WCO.y, MPos.z - WCO.z, reportingUnits);
        }
        if (MPos == null) {
            MPos = new Position(WPos.x + WCO.x, WPos.y + WCO.y, WPos.z + WCO.z, reportingUnits);
        }

        if (!isOverrideReport && lastStatus != null) {
            overrides = lastStatus.getOverrides();
            pins = lastStatus.getEnabledPins();
            accessoryStates = lastStatus.getAccessoryStates();
        } else if (isOverrideReport) {
            // If this is an override report and the 'Pn:' field wasn't sent
            // set all pins to a disabled state.
            if (pins == null) {
                pins = new ControllerStatus.EnabledPins("");
            }
            // Likewise for accessory states.
            if (accessoryStates == null) {
                accessoryStates = new ControllerStatus.AccessoryStates("");
            }
        }

        ControllerState state = getControllerStateFromStateString(stateString);
        return new ControllerStatus(stateString, state, MPos, WPos, feedSpeed, reportingUnits, spindleSpeed, overrides, WCO, pins, accessoryStates);

    }

    private static String getStateFromStatusString(final String status) {
        String retValue = null;

        // Search for a version.
        Matcher matcher = STATUS_STATE_PATTERN.matcher(status);
        if (matcher.find()) {
            retValue = matcher.group(0);
        }

        return retValue;
    }

    private static ControllerState getControllerStateFromStateString(String stateString) {
        switch (stateString.toLowerCase()) {
            case "jog":
                return ControllerState.JOG;
            case "run":
                return ControllerState.RUN;
            case "hold":
                return ControllerState.HOLD;
            case "door":
                return ControllerState.DOOR;
            case "home":
                return ControllerState.HOME;
            case "idle":
                return ControllerState.IDLE;
            case "alarm":
                return ControllerState.ALARM;
            case "check":
                return ControllerState.CHECK;
            case "sleep":
                return ControllerState.SLEEP;
            default:
                return ControllerState.UNKNOWN;
        }
    }

    private static Position getMachinePositionFromStatusString(final String status, UnitUtils.Units reportingUnits) {
        return getPositionFromStatusString(status, MACHINE_PATTERN, reportingUnits);
    }

    private static Position getWorkPositionFromStatusString(final String status, UnitUtils.Units reportingUnits) {
        return getPositionFromStatusString(status, WORK_PATTERN, reportingUnits);
    }

    static private Position getPositionFromStatusString(final String status, final Pattern pattern, UnitUtils.Units reportingUnits) {
        Matcher matcher = pattern.matcher(status);
        if (matcher.find()) {
            return new Position(Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)),
                    reportingUnits);
        }

        return null;
    }

    public static boolean isOkErrorAlarmResponse(String response) {
        return response.startsWith("ok") || response.startsWith("error");
    }

    public static boolean isStatusResponse(final String response) {
        return STATUS_PATTERN.matcher(response).find();
    }

    public static boolean isVersionResponse(String response) {
        return response.startsWith("Build version:");
    }

    public static boolean isParserStateResponse(final String response) {
        return PARSER_STATE_PATTERN.matcher(response).find();
    }

    public static String generateSetWorkPositionCommand(ControllerStatus controllerStatus, GcodeState gcodeState, PartialPosition positions) {
        int offsetCode = WorkCoordinateSystem.fromGCode(gcodeState.offset).getPValue();
        Position machineCoord = controllerStatus.getMachineCoord();


        PartialPosition.Builder offsets = new PartialPosition.Builder();
        for (Map.Entry<Axis, Double> position : positions.getAll().entrySet()) {
            double axisOffset = -(position.getValue() - machineCoord.get(position.getKey()));
            offsets.setValue(position.getKey(), axisOffset);

        }
        return "G10 L2 P" + offsetCode + " " + offsets.build().getFormattedGCode();    }
}
