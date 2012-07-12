/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import gnu.io.CommPortIdentifier;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class CommUtils {
    // Note: One character of this buffer is reserved for real time commands.
    public static final int GRBL_RX_BUFFER_SIZE= 127;
    public static final byte GRBL_PAUSE_COMMAND = '!';
    public static final byte GRBL_RESUME_COMMAND = '~';
    public static final byte GRBL_STATUS_COMMAND = '?';
    public static final byte GRBL_RESET_COMMAND = 0x18;
    
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
        int characters = 0;
        
        // Number of characters in list.
        Iterator<GcodeCommand> iter = list.iterator();
        while (iter.hasNext()) {
            String next = iter.next().toString();
            characters += next.length();
        }
        
        characters += command.length();
        return characters < CommUtils.GRBL_RX_BUFFER_SIZE;
    }
    
    /** 
     * Checks if the string contains the GRBL version.
     */
    static Boolean isGrblVersionString(final String response) {
        return response.startsWith("Grbl ");
    }
    
    /** 
     * Parses the version double out of the version response string.
     */
    static double getVersion(final String response) {
        String version = response.substring("Grbl ".length());
        StringBuilder numString = new StringBuilder();
        int numDecimals = 0;
        for (int i=0; i < response.length(); i++) {
            char ch = response.charAt(i);
            if (Character.isDigit(ch)) {
                numString.append(ch);
            } else if ( ch == '.') {
                numDecimals++;
                // Only major/minor supported (i.e. 7.0)
                // major/minor/subminor will fail (i.e. 7.0.1)
                if (numDecimals > 1) {
                    break;
                }
                numString.append(ch);
            }
        }

        return Double.parseDouble(numString.toString());
    }

    /** 
     * Determines if the version of GRBL is capable of realtime commands.
     */
    static Boolean isRealTimeCapable(final double version) {
        return version > 0.7;
    }
    
    /**
     * Searches the command string for an 'f' and replaces the text between the
     * 'f' and the next space with the integer speed (followed by .0).
     */
    static String overrideSpeed(String command, Integer speed) {
        String returnString = command;
        // Check if command sets feed speed.
        int index = command.toLowerCase().indexOf('f');
        if (index > 0) {
            int indexSpaceAfterF = command.indexOf(" ", index+1);
            // Build that new command.
            returnString = (new StringBuilder()
                    .append(command.substring(0, index+1))
                    .append(speed.toString())
                    .append(".0")
                    .append(command.substring(indexSpaceAfterF))
                    ).toString();
        }

        return returnString;
    }
    
    /**
     * Removes any comments within parentheses or beginning with a semi-colon.
     */
    static String removeComments(String command) {
        String newCommand = command;
        String tempCommand = command;
        int index;

        // Remove any comments within ( parentheses )
        while ( (index = newCommand.indexOf('(')) > -1 ) {
            newCommand = tempCommand.substring(0, index);

            index = tempCommand.indexOf(')');
            newCommand += tempCommand.substring(index+1);
            tempCommand = newCommand;
        }

        // Remove any comment beginning with ';'
        index = newCommand.indexOf(';');
        if (index > -1) {
            newCommand = newCommand.substring(0, index);
        }

        return newCommand;
    }
}
