/*
 * Collection of useful Grbl related utilities.
 */

/*
    Copywrite 2012-2013 Will Winder

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

import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Utils.Units;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    public static final String GCODE_RETURN_TO_ZERO_LOCATION_V8 = "G91 G0 X0 Y0 Z0";
    //public static final String GCODE_RETURN_TO_ZERO_LOCATION_V8C = "G91 G28 X0 Y0 Z4.0";
    public static final String GCODE_RETURN_TO_ZERO_LOCATION_V8C = "G90 G0 X0 Y0 Z0";
    public static final String GCODE_RETURN_TO_MAX_Z_LOCATION_V8C = "G90 G0 Z";
    
    public static final String GCODE_PERFORM_HOMING_CYCLE_V8 = "G28 X0 Y0 Z0";
    public static final String GCODE_PERFORM_HOMING_CYCLE_V8C = "$H";
    
    public static class Capabilities {
        public boolean REAL_TIME = false;
        public boolean OVERRIDES = false;
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
    static protected double getVersionDouble(final String response) {
        double retValue = -1;
        final String VERSION_REGEX = "[0-9]*\\.[0-9]*";
        
        // Search for a version.
        Pattern pattern = Pattern.compile(VERSION_REGEX);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            retValue = Double.parseDouble(matcher.group(0));
        }
        
        return retValue;
    }
    
    static protected String getVersionLetter(final String response) {
        String retValue = null;
        final String VERSION_REGEX = "(?<=[0-9]\\.[0-9])[a-zA-Z]";
        
        // Search for a version.
        Pattern pattern = Pattern.compile(VERSION_REGEX);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            retValue = matcher.group(0);
            //retValue = Double.parseDouble(matcher.group(0));
        }
        
        return retValue;

    }

    /** 
     * Determines if the version of GRBL is capable of realtime commands.
     */
    static protected Boolean isRealTimeCapable(final double version) {
        return version > 0.7;
    }
    
    static protected String getHomingCommand(final double version, final String letter) {
        if ((version >= 0.8 && (letter != null) && letter.equals("c"))
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
    
    static protected String getResetCoordsToZeroCommand(final double version, final String letter) {
        if (version >= 0.9) {
            return GrblUtils.GCODE_RESET_COORDINATES_TO_ZERO_V9;
        }
        else if ((version >= 0.8 && (letter != null) && letter.equals("c"))) {
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

    static protected String getResetCoordToZeroCommand(final char coord, final double version, final String letter) {
        if (version >= 0.9) {
            return String.format(GrblUtils.GCODE_RESET_COORDINATE_TO_ZERO_V9, coord);
        }
        else if ((version >= 0.8 && (letter != null) && letter.equals("c"))) {
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
    
    @Deprecated
    static protected String getReturnToHomeCommand(final double version, final String letter) {
        if ((version >= 0.8 && (letter != null) && letter.equals("c"))
                || version >= 0.9) {
            return GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8C;
        }
        else if (version >= 0.8) {
            return GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8;
        }
        else {
            return "";
        }
    }
    
    static protected ArrayList<String> getReturnToHomeCommands(final double version, final String letter, final double maxZOffset) {
        ArrayList<String> commands = new ArrayList<>();    
        if ((version >= 0.8 && (letter != null) && letter.equals("c"))
                || version >= 0.9) {
            commands.add(GrblUtils.GCODE_RETURN_TO_MAX_Z_LOCATION_V8C + maxZOffset);
            commands.add(GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8C);
        }
        else if (version >= 0.8) {
            commands.add(GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8);
        }
        
        return commands;
    }
    
    static protected String getKillAlarmLockCommand(final double version, final String letter) {
        if ((version >= 0.8 && (letter != null) && letter.equals("c"))
                || version >= 0.9) {
            return GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND;
        }
        else {
            return "";
        }
    }
    
    static protected String getToggleCheckModeCommand(final double version, final String letter) {
        if ((version >= 0.8 && (letter != null) && letter.equals("c"))
                || version >= 0.9) {
            return GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND;
        }
        else {
            return "";
        }
    }
    
    static protected String getViewParserStateCommand(final double version, final String letter) {
        if ((version >= 0.8 && (letter != null) && letter.equals("c"))
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
    static protected Capabilities getGrblStatusCapabilities(final double version, final String letter) {
        Capabilities ret = new Capabilities();

        if (version >= 1.0) {
            ret.REAL_TIME = true;
            ret.OVERRIDES = true;
        }

        else if (version >= 0.8) {
            if (version==0.8 && (letter != null) && letter.equals("c")) {
                ret.REAL_TIME = true;
            } else if (version >= 0.9) {
                ret.REAL_TIME = true;
            }
        }
        return ret;
    }
    
    /**
     * Check if a string contains a GRBL position string.
     */
    private static final String STATUS_REGEX = "\\<.*\\>";
    private static final Pattern STATUS_PATTERN = Pattern.compile(STATUS_REGEX);
    static protected Boolean isGrblStatusString(final String response) {
        return STATUS_PATTERN.matcher(response).find();
    }

    private static final String FEEDBACK_REGEX = "[.*]";
    private static final Pattern FEEDBACK_PATTERN = Pattern.compile(FEEDBACK_REGEX);
    static protected Boolean isGrblFeedbackMessage(final String response) {
        return FEEDBACK_PATTERN.matcher(response).find();
    }


    static protected Boolean isGrblSettingMessage(final String response) {
        return response.startsWith("$") && response.endsWith(")");
    }
    
    /**
     * Parse state out of position string.
     */
    static protected String getStateFromStatusString(final String status, final Capabilities version) {
        String retValue = null;
        String REGEX;
        
        if (version.REAL_TIME) {
            REGEX = "(?<=\\<)[a-zA-z]*(?=[,])";
        } else {
            return null;
        }
        
        
        // Search for a version.
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(status);
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
    static protected Position getMachinePositionFromStatusString(final String status, final Capabilities version, Units reportingUnits) {
        if (version.REAL_TIME) {
            return GrblUtils.getPositionFromStatusString(status, machinePattern, reportingUnits);
        } else {
            return null;
        }
    }
    
    static Pattern workPattern = Pattern.compile("(?<=WPos:)(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*)");
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
            return new Position( Double.parseDouble(matcher.group(1)),
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
                case CMD_SPINDLE_OVR_STOP:
                    return (byte)0x9E;
            }
        }
        return null;
    }
}
