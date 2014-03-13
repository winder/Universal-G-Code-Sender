/*
 * An Abstract communicator interface which implements listeners.
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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author wwinder
 */
public abstract class AbstractCommunicator {
    public static String DEFAULT_TERMINATOR = "\r\n";
    private String lineTerminator;
    protected Connection conn;

    // Callback interfaces
    ArrayList<SerialCommunicatorListener> commandSentListeners;
    ArrayList<SerialCommunicatorListener> commandCompleteListeners;
    ArrayList<SerialCommunicatorListener> commConsoleListeners;
    ArrayList<SerialCommunicatorListener> commVerboseConsoleListeners;
    ArrayList<SerialCommunicatorListener> commRawResponseListener;
    private final ArrayList<Connection>   connections;

    public AbstractCommunicator() {
        this.lineTerminator = DEFAULT_TERMINATOR;
        
        this.commandSentListeners        = new ArrayList<SerialCommunicatorListener>();
        this.commandCompleteListeners    = new ArrayList<SerialCommunicatorListener>();
        this.commConsoleListeners        = new ArrayList<SerialCommunicatorListener>();
        this.commVerboseConsoleListeners = new ArrayList<SerialCommunicatorListener>();
        this.commRawResponseListener     = new ArrayList<SerialCommunicatorListener>();

        //instanciate all known connection drivers
        //TODO: Scan the classpath for classes extending Connection,
        //      and instantiate them dynamically.
        this.connections = new ArrayList<Connection>();
        this.addConnectionType(new SerialConnection());
    }

    final public void addConnectionType(Connection conn) {
        this.connections.add(conn);
    }
    
    /*********************/
    /* Serial Layer API. */
    /*********************/
    abstract public void setSingleStepMode(boolean enable);
    abstract public boolean getSingleStepMode();
    abstract public void queueStringForComm(final String input);
    abstract public void sendByteImmediately(byte b) throws IOException;
    abstract public boolean areActiveCommands();
    abstract public void streamCommands();
    abstract public void pauseSend();
    abstract public void resumeSend();
    abstract public void cancelSend();
    abstract public void softReset();
    abstract public void responseMessage(String response);
    
    //do common operations (related to the connection, that is shared by all communicators)
    protected boolean openCommPort(String name, int baud) throws Exception {
        //choose port
        for(Connection candidate: connections) {
            if(candidate.supports(name)) {
                conn = candidate;
                conn.setCommunicator(this);
                break;
            }
        }
        
        if(conn==null) {
            throw new Exception(Localization.getString("communicator.exception.port") + ": "+name);
        }
        
        //open it
        conn.openPort(name, baud);

        return true;
    }


    //do common things (related to the connection, that is shared by all communicators)
    protected void closeCommPort() {
        conn.closePort();
    }
    
    /** Getters & Setters. */
    void setLineTerminator(String terminator) {
        if (terminator == null || terminator.length() < 1) {
            this.lineTerminator = DEFAULT_TERMINATOR;
        } else {
            this.lineTerminator = terminator;
        }
    }
    
    String getLineTerminator() {
        return this.lineTerminator;
    }
    
    /* ****************** */
    /** Listener helpers. */
    /* ****************** */
    void setListenAll(SerialCommunicatorListener scl) {
        this.addCommandSentListener(scl);
        this.addCommandCompleteListener(scl);
        this.addCommConsoleListener(scl);
        this.addCommVerboseConsoleListener(scl);
        this.addCommRawResponseListener(scl);
    }

    void addCommandSentListener(SerialCommunicatorListener scl) {
        this.commandSentListeners.add(scl);
    }

    void addCommandCompleteListener(SerialCommunicatorListener scl) {
        this.commandCompleteListeners.add(scl);
    }
    
    void addCommConsoleListener(SerialCommunicatorListener scl) {
        this.commConsoleListeners.add(scl);
    }

    void addCommVerboseConsoleListener(SerialCommunicatorListener scl) {
        this.commVerboseConsoleListeners.add(scl);
    }
    
    void addCommRawResponseListener(SerialCommunicatorListener scl) {
        this.commRawResponseListener.add(scl);
    }

    // Helper for the console listener.              
    protected void sendMessageToConsoleListener(String msg) {
        this.sendMessageToConsoleListener(msg, false);
    }
    
    protected void sendMessageToConsoleListener(String msg, boolean verbose) {
        // Exit early if there are no listeners.
        if (this.commConsoleListeners == null) {
            return;
        }
        
        int verbosity;
        if (!verbose) {
            verbosity = CONSOLE_MESSAGE;
        }
        else {
            verbosity = VERBOSE_CONSOLE_MESSAGE;
        }
        
        dispatchListenerEvents(verbosity, this.commConsoleListeners, msg);
    }
    
    // Serial Communicator Listener Events
    protected static final int COMMAND_SENT = 1;
    protected static final int COMMAND_COMPLETE = 2;
    protected static final int RAW_RESPONSE = 3;
    protected static final int CONSOLE_MESSAGE = 4;
    protected static final int VERBOSE_CONSOLE_MESSAGE = 5;
    
    /**
     * A bunch of methods to dispatch listener events with various arguments.
     */
    static protected void dispatchListenerEvents(int event, ArrayList<SerialCommunicatorListener> sclList, String message) {
        if (sclList != null) {
            for (SerialCommunicatorListener s : sclList) {
                sendEventToListener(event, s, message, null);
            }
        }
    }
    
    static protected void dispatchListenerEvents(int event, ArrayList<SerialCommunicatorListener> sclList, GcodeCommand command) {
        if (sclList != null) {
            for (SerialCommunicatorListener s : sclList) {
                sendEventToListener(event, s, null, command);
            }
        }
    }

    static protected void sendEventToListener(int event, SerialCommunicatorListener scl, 
                                            String string, GcodeCommand command) {
        switch(event) {
            case COMMAND_SENT:
                scl.commandSent(string);
                break;
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
