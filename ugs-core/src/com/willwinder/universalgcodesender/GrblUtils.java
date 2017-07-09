/*
 * Collection of useful Grbl related utilities.
 */

/*
    Copyright 2012-2017 Will Winder

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

import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.OverridePercents;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.AccessoryStates;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.EnabledPins;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author wwinder
 */
public class GrblUtils {
// Note: 5 characters of this buffer reserved for real time commands.
    public static final int GRBL_RX_BUFFER_SIZE= 123;
    
    /**
     * Grbl commands
     */
    // Real time
    public static final byte GRBL_PAUSE_COMMAND = '!';
    public static final byte GRBL_RESUME_COMMAND = '~';
    public static final byte GRBL_STATUS_COMMAND = '?';
    public static final byte GRBL_DOOR_COMMAND = (byte)0x84;
    public static final byte GRBL_JOG_CANCEL_COMMAND = (byte)0x85;
    public static final byte GRBL_RESET_COMMAND = 0x18;
    // Non real time
    public static final String GRBL_KILL_ALARM_LOCK_COMMAND = "$X";
    public static final String GRBL_TOGGLE_CHECK_MODE_COMMAND = "$C";
    public static final String GRBL_VIEW_PARSER_STATE_COMMAND = "$G";
    public static final String GRBL_VIEW_SETTINGS_COMMAND = "$$";
    
    /**
     * Gcode Commands
     */
    public static final String GCODE_RESET_COORDINATES_TO_ZERO_V9 = "G10 P0 L20 X0 Y0 Z0";
    public static final String GCODE_RESET_COORDINATES_TO_ZERO_V8 = "G92 X0 Y0 Z0";

    public static final String GCODE_RESET_COORDINATE_TO_ZERO_V9 = "G10 P0 L20 %c0";
    public static final String GCODE_RESET_COORDINATE_TO_ZERO_V8 = "G92 %c0";
    
    public static final String GCODE_RETURN_TO_ZERO_LOCATION_V8 = "G90 G0 X0 Y0";
    public static final String GCODE_RETURN_TO_ZERO_LOCATION_Z0_V8 = "G90 G0 Z0";
    public static final String GCODE_RETURN_TO_MAX_Z_LOCATION_V8 = "G90 G0 Z";
    
    public static final String GCODE_PERFORM_HOMING_CYCLE_V8 = "G28 X0 Y0 Z0";
    public static final String GCODE_PERFORM_HOMING_CYCLE_V8C = "$H";
    
    public static class Capabilities {
        public boolean REAL_TIME = false;
        public boolean OVERRIDES = false;
        public boolean V1_FORMAT = false;
        public boolean JOG_MODE = false;
    }
    
    /** 
     * Checks if the string contains the GRBL version.
     */
    static Boolean isGrblVersionString(final String response) {
        Boolean version = response.startsWith("Grbl ") || response.startsWith("CarbideMotion ");
        return version && (getVersionDouble(response) != -1);
    }
    
    /** 
     * Parses the version double out of the version response string.
     */
    final static String VERSION_DOUBLE_REGEX = "[0-9]*\\.[0-9]*";
    final static Pattern VERSION_DOUBLE_PATTERN = Pattern.compile(VERSION_DOUBLE_REGEX);
    static protected double getVersionDouble(final String response) {
        double retValue = -1;
        
        // Search for a version.
        Matcher matcher = VERSION_DOUBLE_PATTERN.matcher(response);
        if (matcher.find()) {
            retValue = Double.parseDouble(matcher.group(0));
        }
        
        return retValue;
    }
    
    final static String VERSION_LETTER_REGEX = "(?<=[0-9]\\.[0-9])[a-zA-Z]";
    final static Pattern VERSION_LETTER_PATTERN = Pattern.compile(VERSION_LETTER_REGEX);
    static protected Character getVersionLetter(final String response) {
        Character retValue = null;
        
        // Search for a version.
        Matcher matcher = VERSION_LETTER_PATTERN.matcher(response);
        if (matcher.find()) {
            retValue = matcher.group(0).charAt(0);
            //retValue = Double.parseDouble(matcher.group(0));
        }
        
        return retValue;
    }

    static protected String getHomingCommand(final double version, final Character letter) {
        if ((version >= 0.8 && (letter != null) && (letter >= 'c'))
                || version >= 0.9) {
            return GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C;
        }
        else if (version >= 0.8) {
            return GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8;
        }
        else {
            return "";
        }
    }
    
    static protected String getResetCoordsToZeroCommand(final double version, final Character letter) {
        if (version >= 0.9) {
            return GrblUtils.GCODE_RESET_COORDINATES_TO_ZERO_V9;
        }
        else if (version >= 0.8 && (letter != null) && (letter >= 'c')) {
            // TODO: Is G10 available in 0.8c?
            // No it is not -> error: Unsupported statement
            return GrblUtils.GCODE_RESET_COORDINATES_TO_ZERO_V8;
        }
        else if (version >= 0.8) {
            return GrblUtils.GCODE_RESET_COORDINATES_TO_ZERO_V8;
        }
        else {
            return "";
        }
    }

    static protected String getResetCoordToZeroCommand(final char coord, final double version, final Character letter) {
        if (version >= 0.9) {
            return String.format(GrblUtils.GCODE_RESET_COORDINATE_TO_ZERO_V9, coord);
        }
        else if (version >= 0.8 && (letter != null) && (letter >= 'c')) {
            // TODO: Is G10 available in 0.8c?
            // No it is not -> error: Unsupported statement
            return String.format(GrblUtils.GCODE_RESET_COORDINATE_TO_ZERO_V8, coord);
        }
        else if (version >= 0.8) {
            return "";
        }
        else {
            return "";
        }
    }
    
    static protected ArrayList<String> getReturnToHomeCommands(final double version, final Character letter, final double zHeight) {
        ArrayList<String> commands = new ArrayList<>();    
        // If Z is less than zero, raise it before further movement.
        if (zHeight < 0) {
            commands.add(GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_Z0_V8);
        }
        commands.add(GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8);
        commands.add(GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_Z0_V8);
        
        return commands;
    }
    
    static protected String getKillAlarmLockCommand(final double version, final Character letter) {
        if ((version >= 0.8 && (letter != null) && letter >= 'c')
                || version >= 0.9) {
            return GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND;
        }
        else {
            return "";
        }
    }
    
    static protected String getToggleCheckModeCommand(final double version, final Character letter) {
        if ((version >= 0.8 && (letter != null) && letter >= 'c')
                || version >= 0.9) {
            return GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND;
        }
        else {
            return "";
        }
    }
    
    static protected String getViewParserStateCommand(final double version, final Character letter) {
        if ((version >= 0.8 && (letter != null) && letter >= 'c')
                || version >= 0.9) {
            return GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND;
        }
        else {
            return "";
        }
    }
    
    /**
     * Determines version of GRBL position capability.
     */
    static protected Capabilities getGrblStatusCapabilities(final double version, final Character letter) {
        Capabilities ret = new Capabilities();

        // Check if real time commands are enabled.
        if (version==0.8 && (letter != null) && (letter >= 'c')) {
            ret.REAL_TIME = true;
        } else if (version >= 0.9) {
            ret.REAL_TIME = true;
        }

        // Check for V1.x features
        if (version >= 1.1) {
            ret.REAL_TIME = true;

            // GRBL 1.1
            ret.V1_FORMAT = true;
            ret.OVERRIDES = true;
            ret.JOG_MODE = true;
        }

        return ret;
    }

    static String PROBE_POSITION_REGEX = "\\[PRB:(-?\\d*\\.\\d*),(-?\\d*\\.\\d*),(-?\\d*\\.\\d*)(?::(\\d))?]";
    static Pattern PROBE_POSITION_PATTERN = Pattern.compile(PROBE_POSITION_REGEX);
    static protected Position parseProbePosition(final String response, final Units units) {
        // Don't parse failed probe response.
        if (response.contains(":0]")) {
            return null;
        }

        return GrblUtils.getPositionFromStatusString(response, PROBE_POSITION_PATTERN, units);
    }
    
    /**
     * Check if a string contains a GRBL position string.
     */
    private static final String STATUS_REGEX = "\\<.*\\>";
    private static final Pattern STATUS_PATTERN = Pattern.compile(STATUS_REGEX);
    static protected Boolean isGrblStatusString(final String response) {
        return STATUS_PATTERN.matcher(response).find();
    }

    private static final String PROBE_REGEX = "\\[PRB:.*\\]";
    private static final Pattern PROBE_PATTERN = Pattern.compile(PROBE_REGEX);
    static protected Boolean isGrblProbeMessage(final String response) {
        return PROBE_PATTERN.matcher(response).find();
    }

    private static final String FEEDBACK_REGEX = "\\[.*\\]";
    private static final Pattern FEEDBACK_PATTERN = Pattern.compile(FEEDBACK_REGEX);
    static protected Boolean isGrblFeedbackMessage(final String response, Capabilities c) {
        if (c.V1_FORMAT) {
            return response.startsWith("[GC:");
        } else {
            return FEEDBACK_PATTERN.matcher(response).find();
        }
    }


    private static final String SETTING_REGEX = "\\$\\d+=.+";
    private static final Pattern SETTING_PATTERN = Pattern.compile(SETTING_REGEX);
    static protected Boolean isGrblSettingMessage(final String response) {
        return SETTING_PATTERN.matcher(response).find();
    }
    
    /**
     * Parses a GRBL status string in the legacy format or v1.x format:
     * legacy: <status,WPos:1,2,3,MPos:1,2,3>
     * 1.x: <status|WPos:1,2,3|Bf:0,0|WCO:0,0,0>
     * @param lastStatus required for the 1.x version which requires WCO coords
     *                   and override status from previous status updates.
     * @param status the raw status string
     * @param version capabilities flags
     * @param units units
     * @return 
     */
    static protected ControllerStatus getStatusFromStatusString(
            ControllerStatus lastStatus, final String status,
            final Capabilities version, Units reportingUnits) {
        // Legacy status.
        if (!version.V1_FORMAT) {
            return new ControllerStatus(
                getStateFromStatusString(status, version),
                getMachinePositionFromStatusString(status, version, reportingUnits),
                getWorkPositionFromStatusString(status, version, reportingUnits));
        } else {
            String state = "";
            Position MPos = null;
            Position WPos = null;
            Position WCO = null;

            OverridePercents overrides = null;
            EnabledPins pins = null;
            AccessoryStates accessoryStates = null;
            Double feedSpeed = null;
            Double spindleSpeed = null;

            boolean isOverrideReport = false;

            // Parse out the status messages.
            for (String part : status.substring(0, status.length()-1).split("\\|")) {
                if (part.startsWith("<")) {
                    int idx = part.indexOf(':');
                    if (idx == -1)
                        state = part.substring(1);
                    else
                        state = part.substring(1, idx);
                }
                else if (part.startsWith("MPos:")) {
                    MPos = GrblUtils.getPositionFromStatusString(status, machinePattern, reportingUnits);
                }
                else if (part.startsWith("WPos:")) {
                    WPos = GrblUtils.getPositionFromStatusString(status, workPattern, reportingUnits);
                }
                else if (part.startsWith("WCO:")) {
                    WCO = GrblUtils.getPositionFromStatusString(status, wcoPattern, reportingUnits);
                }
                else if (part.startsWith("Ov:")) {
                    isOverrideReport = true;
                    String[] overrideParts = part.substring(3).trim().split(",");
                    if (overrideParts.length == 3) {
                        overrides = new OverridePercents(
                                Integer.parseInt(overrideParts[0]),
                                Integer.parseInt(overrideParts[1]),
                                Integer.parseInt(overrideParts[2]));
                    }
                }
                else if (part.startsWith("F:")) {
                    feedSpeed = Double.parseDouble(part.substring(2));
                }
                else if (part.startsWith("FS:")) {
                    String[] parts = part.substring(3).split(",");
                    feedSpeed = Double.parseDouble(parts[0]);
                    spindleSpeed = Double.parseDouble(parts[1]);
                }
                else if (part.startsWith("Pn:")) {
                    String value = part.substring(part.indexOf(':')+1);
                    pins = new EnabledPins(value);
                }
                else if (part.startsWith("A:")) {
                    String value = part.substring(part.indexOf(':')+1);
                    accessoryStates = new AccessoryStates(value);
                }
            }

            // Grab WCO from state information if necessary.
            if (WCO == null) {
                // Grab the work coordinate offset.
                if (lastStatus != null && lastStatus.getWorkCoordinateOffset() != null) {
                    WCO = lastStatus.getWorkCoordinateOffset();
                } else {
                    WCO = new Position(0,0,0, reportingUnits);
                }
            }

            // Calculate missing coordinate with WCO
            if (WPos == null) {
                WPos = new Position(MPos.x-WCO.x, MPos.y-WCO.y, MPos.z-WCO.z, reportingUnits);
            }
            if (MPos == null) {
                MPos = new Position(WPos.x+WCO.x, WPos.y+WCO.y, WPos.z+WCO.z, reportingUnits);
            }

            if (!isOverrideReport && lastStatus != null) {
                overrides = lastStatus.getOverrides();
                pins = lastStatus.getEnabledPins();
                accessoryStates = lastStatus.getAccessoryStates();
            }
            else if (isOverrideReport) {
                // If this is an override report and the 'Pn:' field wasn't sent
                // set all pins to a disabled state.
                if (pins == null) {
                    pins = new EnabledPins("");
                }
                // Likewise for accessory states.
                if (accessoryStates == null) {
                    accessoryStates = new AccessoryStates("");
                }
            }

            return new ControllerStatus(state, MPos, WPos, feedSpeed, spindleSpeed, overrides, WCO, pins, accessoryStates); 
        }
    }

    /**
     * Parse state out of position string.
     */
    final static String STATUS_STATE_REGEX = "(?<=\\<)[a-zA-z]*(?=[,])";
    final static Pattern STATUS_STATE_PATTERN = Pattern.compile(STATUS_STATE_REGEX);
    static protected String getStateFromStatusString(final String status, final Capabilities version) {
        String retValue = null;
        
        if (!version.REAL_TIME) {
            return null;
        }
        
        // Search for a version.
        Matcher matcher = STATUS_STATE_PATTERN.matcher(status);
        if (matcher.find()) {
            retValue = matcher.group(0);;
        }

        return retValue;
    }
    
    static Pattern mmPattern = Pattern.compile(".*:\\d+\\.\\d\\d\\d,.*");
    static protected Units getUnitsFromStatusString(final String status, final Capabilities version) {
        if (version.REAL_TIME) {
            if (mmPattern.matcher(status).find()) {
                return Units.MM;
            } else {
                return Units.INCH;
            }
        }
        
        return Units.UNKNOWN;
    }

    static Pattern machinePattern = Pattern.compile("(?<=MPos:)(-?\\d*\\..\\d*),(-?\\d*\\..\\d*),(-?\\d*\\..\\d*)");
    static Pattern workPattern = Pattern.compile("(?<=WPos:)(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*)");
    static Pattern wcoPattern = Pattern.compile("(?<=WCO:)(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*)");
    static protected Position getMachinePositionFromStatusString(final String status, final Capabilities version, Units reportingUnits) {
        if (version.REAL_TIME) {
            return GrblUtils.getPositionFromStatusString(status, machinePattern, reportingUnits);
        } else {
            return null;
        }
    }
    
    static protected Position getWorkPositionFromStatusString(final String status, final Capabilities version, Units reportingUnits) {
        if (version.REAL_TIME) {
            return GrblUtils.getPositionFromStatusString(status, workPattern, reportingUnits);
        } else {
            return null;
        }
    }
    
    static private Position getPositionFromStatusString(final String status, final Pattern pattern, Units reportingUnits) {
        Matcher matcher = pattern.matcher(status);
        if (matcher.find()) {
            return new Position(Double.parseDouble(matcher.group(1)),
                                Double.parseDouble(matcher.group(2)),
                                Double.parseDouble(matcher.group(3)),
                                reportingUnits);
        }
        
        return null;
    }

    /**
     * Map version enum to GRBL real time command byte.
     */
    static public Byte getOverrideForEnum(final Overrides command, final Capabilities version) {
        if (version != null && version.OVERRIDES) {
            switch (command) {
                //CMD_DEBUG_REPORT, // 0x85 // Only when DEBUG enabled, sends debug report in '{}' braces.
                case CMD_FEED_OVR_RESET:
                    return (byte)0x90; // Restores feed override value to 100%.
                case CMD_FEED_OVR_COARSE_PLUS:
                    return (byte)0x91;
                case CMD_FEED_OVR_COARSE_MINUS:
                    return (byte)0x92;
                case CMD_FEED_OVR_FINE_PLUS :
                    return (byte)0x93;
                case CMD_FEED_OVR_FINE_MINUS :
                    return (byte)0x94;
                case CMD_RAPID_OVR_RESET:
                    return (byte)0x95;
                case CMD_RAPID_OVR_MEDIUM:
                    return (byte)0x96;
                case CMD_RAPID_OVR_LOW:
                    return (byte)0x97;
                case CMD_SPINDLE_OVR_RESET:
                    return (byte)0x99; // Restores spindle override value to 100%.
                case CMD_SPINDLE_OVR_COARSE_PLUS:
                    return (byte)0x9A;
                case CMD_SPINDLE_OVR_COARSE_MINUS:
                    return (byte)0x9B;
                case CMD_SPINDLE_OVR_FINE_PLUS:
                    return (byte)0x9C;
                case CMD_SPINDLE_OVR_FINE_MINUS:
                    return (byte)0x9D;
                case CMD_TOGGLE_SPINDLE:
                    return (byte)0x9E;
                case CMD_TOGGLE_FLOOD_COOLANT:
                    return (byte)0xA0;
                case CMD_TOGGLE_MIST_COOLANT:
                    return (byte)0xA1;
            }
        }
        return null;
    }

    public static boolean isOkErrorAlarmResponse(String response) {
        return isOkResponse(response) || isErrorResponse(response) || isAlarmResponse(response);
    }

    public static boolean isOkResponse(String response) {
        return StringUtils.equalsIgnoreCase(response, "ok");
    }

    public static boolean isErrorResponse(String response) {
        return StringUtils.containsIgnoreCase(response, "error");
    }

    public static boolean isAlarmResponse(String response) {
        return StringUtils.startsWith(response, "ALARM");
    }
}
