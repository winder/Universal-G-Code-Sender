/*
 * Collection of useful Comm related utilities.
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

import gnu.io.CommPortIdentifier;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wwinder
 */
public class CommUtils {
    // Note: One character of this buffer is reserved for real time commands.
    public static final int GRBL_RX_BUFFER_SIZE= 127;
    
    /**
     * Real-time commands
     */
    public static final byte GRBL_PAUSE_COMMAND = '!';
    public static final byte GRBL_RESUME_COMMAND = '~';
    public static final byte GRBL_STATUS_COMMAND = '?';
    public static final byte GRBL_RESET_COMMAND = 0x18;
    
    /**
     * Gcode Commands
     */
    public static final String GCODE_RESET_COORDINATES_TO_ZERO = "G92 X0 Y0 Z0";
    public static final String GCODE_RETURN_TO_ZERO_LOCATION = "G0 X0 Y0 Z0";
    public static final String GCODE_PERFORM_HOMING_CYCLE = "G28 X0 Y0 Z0";
    
    public enum Capabilities {
        REAL_TIME, POSITION_C
    }

    /** 
     * Generates a list of available serial ports.
     */
    static java.util.List<CommPortIdentifier> getSerialPortList() {
        int type = CommPortIdentifier.PORT_SERIAL;
        
        java.util.Enumeration<CommPortIdentifier> portEnum = 
                CommPortIdentifier.getPortIdentifiers();
        java.util.List<CommPortIdentifier> returnList =
                new java.util.ArrayList<CommPortIdentifier>();
        
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if (portIdentifier.getPortType() == type) {
                returnList.add(portIdentifier);
            }
        }
        return returnList;
    }
    
    /** 
     * Reads characters from the input stream until a terminating pattern is
     * reached.
     */
    static String readLineFromCommUntil(InputStream in, String term) throws IOException {
        
        if (term == null || term.length() == 0) {
            return "";
        }
        
        int data;
        StringBuilder buffer = new StringBuilder();
        int terminatorPosition = 0;
        
        while ( ( data = in.read()) > -1 ) {
            // My funky way of checking for the terminating characters..
            if ( data == term.charAt(terminatorPosition) ) {
                terminatorPosition++;
            } else {
                terminatorPosition = 0;
            }

            if (terminatorPosition == term.length()) {
                break;
            }

            buffer.append((char)data);
        }
        
        return buffer.toString();
    }
    
    /** 
     * Checks if there is enough room in the GRBL buffer for nextCommand.
     */
    static Boolean checkRoomInBuffer(List<GcodeCommand> list, GcodeCommand nextCommand) {
        String command = nextCommand.getCommandString();
        int characters = getSizeOfBuffer(list);
        // TODO: Carefully trace the newlines in commands and make sure
        //       the GRBL_RX_BUFFER_SIZE is honored.
        //       For now add a safety character to each command.
        characters += command.length() + 1;
        return characters < CommUtils.GRBL_RX_BUFFER_SIZE;
    }
    
    static int getSizeOfBuffer(List<GcodeCommand> list) {
        int characters = 0;
        // Number of characters in list.
        Iterator<GcodeCommand> iter = list.iterator();
        while (iter.hasNext()) {
            String next = iter.next().toString();
            // TODO: Carefully trace the newlines in commands and make sure
            //       the GRBL_RX_BUFFER_SIZE is honored.
            //       For now add a safety character to each command.
            characters += next.length() + 1;
        }
        return characters;
    }
    
    /**
     * Searches the command string for an 'f' and replaces the speed value 
     * between the 'f' and the next space with a percentage of that speed.
     * In that way all speed values become a ratio of the provided speed 
     * and don't get overridden with just a fixed speed.
     */
    static String overrideSpeed(String command, Integer speed) {
        String returnString = command;
        
        // Check if command sets feed speed.
        Pattern pattern = Pattern.compile("F([0-9.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()){
            Double originalFeedRate = Double.parseDouble(matcher.group(1));
            //System.out.println( "Found feed     " + originalFeedRate.toString() );
            Double newFeedRate      = originalFeedRate * speed / 100.0;
            //System.out.println( "Change to feed " + newFeedRate.toString() );
            returnString = matcher.replaceAll( "F" + newFeedRate.toString() );
        }

        return returnString;
    }
    
    /**
     * Removes any comments within parentheses or beginning with a semi-colon.
     */
    static String removeComment(String command) {
        String newCommand = command;

        // Remove any comments within ( parentheses ) with regex "\([^\(]*\)"
        newCommand = newCommand.replaceAll("\\([^\\(]*\\)", "");

        // Remove any comment beginning with ';' with regex "\;[^\\(]*"
        newCommand = newCommand.replaceAll("\\;[^\\\\(]*", "");
        
        return newCommand;
    }
    
    /**
     * Searches for a comment in the input string and returns the first match.
     */
    static String parseComment(String command) {
        String comment = "";

        // REGEX: Find any comment, includes the comment characters:
        //              "(?<=\()[^\(\)]*|(?<=\;)[^;]*"
        //              "(?<=\\()[^\\(\\)]*|(?<=\\;)[^;]*"
        
        Pattern pattern = Pattern.compile("(?<=\\()[^\\(\\)]*|(?<=\\;)[^;]*");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()){
            comment = matcher.group(0);
        }

        return comment;
    }
}
