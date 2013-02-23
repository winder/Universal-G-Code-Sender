/*
 * A mistake.
 */
/*
    Copywrite 2013 Will Winder

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

import java.util.ArrayList;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class ListenerUtils {
    // Serial Communicator Listener Events
    static final int FILE_STREAM_COMPLETE = 1;
    static final int COMMAND_QUEUED = 2;
    static final int COMMAND_SENT = 3;
    static final int COMMAND_COMMENT = 4;
    static final int COMMAND_COMPLETE = 5;
    static final int COMMAND_PREPROCESS = 6;
    static final int CONSOLE_MESSAGE = 7;
    static final int VERBOSE_CONSOLE_MESSAGE = 8;
    static final int CAPABILITY = 9;
    static final int POSITION_UPDATE = 10;
    static final int RAW_RESPONSE = 11;
    
    /**
     * A bunch of methods to dispatch listener events with various arguments.
     */
    static protected void dispatchListenerEvents(int event, ArrayList<SerialCommunicatorListener> sclList, String message) {
        if (sclList != null) {
            for (SerialCommunicatorListener s : sclList) {
                sendEventToListener(event, s, message, false, null, null, null, null);
            }
        }
    }
    
    static protected void dispatchListenerEvents(int event, ArrayList<SerialCommunicatorListener> sclList, GcodeCommand command) {
        if (sclList != null) {
            for (SerialCommunicatorListener s : sclList) {
                sendEventToListener(event, s, null, false, null, command, null, null);
            }
        }
    }
        
    static protected void dispatchListenerEvents(int event, ArrayList<SerialCommunicatorListener> sclList, String filename, boolean success) {
        if (sclList != null) {
            for (SerialCommunicatorListener s : sclList) {
                sendEventToListener(event, s, filename, success, null, null, null, null);
            }
        }
    }
    
    static protected void dispatchListenerEvents(int event, ArrayList<SerialCommunicatorListener> sclList, CommUtils.Capabilities capability) {
        if (sclList != null) {
            for (SerialCommunicatorListener s : sclList) {
                sendEventToListener(event, s, null, false, capability, null, null, null);
            }
        }
    }
    
    static protected void sendEventToListener(int event, SerialCommunicatorListener scl, 
            String string, boolean bool, CommUtils.Capabilities capability, GcodeCommand command,
            Point3d workLocation, Point3d machineLocation) {
        switch(event) {
            case COMMAND_COMPLETE:
                scl.commandComplete(command);
                break;
            case COMMAND_SENT:
                scl.commandSent(command);
                break;
            case COMMAND_PREPROCESS:
                throw new UnsupportedOperationException("Cannot dispatch preprocessor listeners");

            case CONSOLE_MESSAGE:
                scl.messageForConsole(string);
                break;
            case VERBOSE_CONSOLE_MESSAGE:
                scl.verboseMessageForConsole(string);
                break;
            case RAW_RESPONSE:
                scl.rawResponseListener(string);
            default:

        }
    }
}
