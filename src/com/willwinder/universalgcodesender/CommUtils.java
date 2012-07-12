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
    
    /** Generates a list of available serial ports.
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
    
    /** Reads characters from the input stream until a terminating pattern is
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
    
    /** Checks if there is enough room in the GRBL buffer for nextCommand.
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
    
    /** Checks if the string contains the GRBL version.
     */
    static Boolean isGrblVersionString(String response) {
        return response.startsWith("Grbl ");
    }
    
    /** Parses the version double out of the version response string.
     */
    static double getVersion(String response) {
        String version = response.substring("Grbl ".length());
        return Double.parseDouble(version);
    }

    /** Determines if the version of GRBL is capable of realtime commands.
     */
    static Boolean isRealTimeCapable(double version) {
        return version > 0.7;
    }
}
