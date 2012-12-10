/*
 * Collection of useful Grbl related utilities.
 */

/*
    Copywrite 2012 Will Winder

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wwinder
 */
public class GrblUtils {
/** 
     * Checks if the string contains the GRBL version.
     */
    static Boolean isGrblVersionString(final String response) {
        return response.startsWith("Grbl ") && (getVersionDouble(response) != -1);
    }
    
    /** 
     * Parses the version double out of the version response string.
     */
    static double getVersionDouble(final String response) {
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
    
    static String getVersionLetter(final String response) {
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
    static Boolean isRealTimeCapable(final double version) {
        return version > 0.7;
    }
    
    /**
     * Determines version of GRBL position capability.
     */
    static CommUtils.Capabilities getGrblPositionCapabilities(final double version, final String letter) {
        if (version == 0.8 && letter.equals("c")) {
            return CommUtils.Capabilities.POSITION_C;
        }

        return null;
    }
    
    /**
     * Check if a string contains a GRBL position string.
     */
    static Boolean isGrblPositionString(final String response) {
        double retValue = -1;
        final String REGEX = "\\<.*\\>";
        
        // Search for a version.
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return true;
        }
        return false;
    }
    
    /**
     * Parse status out of position string.
     */
    static String getStatusFromPositionString(final String position, final CommUtils.Capabilities version) {
        String retValue = null;
        String REGEX;
        
        if (version == CommUtils.Capabilities.POSITION_C) {
            REGEX = "(?<=\\<)[a-zA-z]*(?=[,])";
        } else {
            return null;
        }
        
        
        // Search for a version.
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(position);
        if (matcher.find()) {
            retValue = matcher.group(0);;
        }

        return retValue;
    }
    
    
    static Coordinate getMachinePositionFromPositionString(final String position, final CommUtils.Capabilities version) {
        Coordinate ret = null;
        String REGEX;
        
        if (version == CommUtils.Capabilities.POSITION_C) {
            REGEX = "(?<=MPos:)(-?\\d*\\..\\d*),(-?\\d*\\..\\d*),(-?\\d*\\..\\d*)(?=,WPos:)";
        } else {
            return null;
        }
        
        // Search for a version.
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(position);
        if (matcher.find()) {
            ret = new Coordinate( Double.parseDouble(matcher.group(1)),
                                  Double.parseDouble(matcher.group(2)),
                                  Double.parseDouble(matcher.group(3)));
        }

        return ret;
    }

    static Coordinate getWorkPositionFromPositionString(final String position, final CommUtils.Capabilities version) {
        Coordinate ret = null;
        String REGEX;

        if (version == CommUtils.Capabilities.POSITION_C) {
            REGEX = "(?<=WPos:)(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*),(\\-?\\d*\\..\\d*)";
        } else {
            return null;
        }
        
        // Search for a version.
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(position);
        if (matcher.find()) {
            ret = new Coordinate( Double.parseDouble(matcher.group(1)),
                                  Double.parseDouble(matcher.group(2)),
                                  Double.parseDouble(matcher.group(3)));
        }

        return ret;
    }
}
