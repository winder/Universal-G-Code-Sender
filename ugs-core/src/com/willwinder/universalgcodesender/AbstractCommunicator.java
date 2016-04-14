/*
 * An Abstract communicator interface which implements listeners.
 */

/*
    Copywrite 2013-2016 Will Winder

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

import static com.willwinder.universalgcodesender.AbstractCommunicator.SerialCommunicatorEvent.*;
import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * @author wwinder
 */
public abstract class AbstractCommunicator {
    public static String DEFAULT_TERMINATOR = "\r\n";
    protected Connection conn;
    private int commandCounter = 0;

    // Allow events to be sent from same thread for unit tests.
    private boolean launchEventsInDispatchThread = true;

    // Serial Communicator Listener Events
    enum SerialCommunicatorEvent {
        COMMAND_SENT,
        COMMAND_SKIPPED,
        RAW_RESPONSE,
        CONSOLE_MESSAGE,
        VERBOSE_CONSOLE_MESSAGE
    }
    // Callback interfaces
    private ArrayList<SerialCommunicatorListener> commandEventListeners;
    private ArrayList<SerialCommunicatorListener> commConsoleListeners;
    private ArrayList<SerialCommunicatorListener> commVerboseConsoleListeners;
    private ArrayList<SerialCommunicatorListener> commRawResponseListener;
    private HashMap<SerialCommunicatorEvent, ArrayList<SerialCommunicatorListener>> eventMap;

    public AbstractCommunicator() {
        this.commandEventListeners       = new ArrayList<>();
        this.commConsoleListeners        = new ArrayList<>();
        this.commVerboseConsoleListeners = new ArrayList<>();
        this.commRawResponseListener     = new ArrayList<>();

        this.eventMap = new HashMap<>();
        eventMap.put(SerialCommunicatorEvent.COMMAND_SENT,            commandEventListeners);
        eventMap.put(SerialCommunicatorEvent.COMMAND_SKIPPED,         commandEventListeners);
        eventMap.put(SerialCommunicatorEvent.CONSOLE_MESSAGE,         commConsoleListeners);
        eventMap.put(SerialCommunicatorEvent.VERBOSE_CONSOLE_MESSAGE, commVerboseConsoleListeners);
        eventMap.put(SerialCommunicatorEvent.RAW_RESPONSE,            commRawResponseListener);
    }
    
    /*********************/
    /* Serial Layer API. */
    /*********************/
    abstract public void setSingleStepMode(boolean enable);
    abstract public boolean getSingleStepMode();
    abstract public void queueStringForComm(final String input);
    /**
     * Use GcodeStreamReader to allow GUIs to display better execution progress.
     */
    @Deprecated
    abstract public void queueRawStreamForComm(final Reader input);
    abstract public void queueStreamForComm(final GcodeStreamReader input);
    abstract public void sendByteImmediately(byte b) throws Exception;
    abstract public String activeCommandSummary();
    abstract public boolean areActiveCommands();
    abstract public void streamCommands();
    abstract public void pauseSend();
    abstract public void resumeSend();
    abstract public void cancelSend();
    abstract public void softReset();
    abstract public void responseMessage(String response);
    abstract public int numActiveCommands();

    /**
     * Reset any internal buffers. In case a controller reset was detected call
     * this.
     */
    abstract public void resetBuffersInternal();
    final public void resetBuffers() {
        if (eventQueue != null) {
            eventQueue.clear();
        }
        resetBuffersInternal();
    }
    
    public void setConnection(Connection c) {
        conn = c;
    }

    //do common operations (related to the connection, that is shared by all communicators)
    protected boolean openCommPort(String name, int baud) throws Exception {
        if (conn == null) {
            conn = ConnectionFactory.getConnectionFor(name, baud);
        }

        if (conn != null) {
            conn.setCommunicator(this);
        }
        
        if (conn==null) {
            throw new Exception(Localization.getString("communicator.exception.port") + ": "+name);
        }
        
        // Handle all events in a single thread.
        this.eventThread.start();

        //open it
        return conn.openPort(name, baud);
    }

    public boolean isCommOpen() {
        return conn != null && conn.isOpen();
    }


    //do common things (related to the connection, that is shared by all communicators)
    protected void closeCommPort() throws Exception {
        this.stop = true;
        this.eventThread.interrupt();

        conn.closePort();
    }

    protected int getNextCommandId() {
        return this.commandCounter++;
    }
    
    /** Getters & Setters. */
    abstract public String getLineTerminator();
    
    /* ****************** */
    /** Listener helpers. */
    /* ****************** */
    void setListenAll(SerialCommunicatorListener scl) {
        this.addCommandEventListener(scl);
        this.addCommConsoleListener(scl);
        this.addCommVerboseConsoleListener(scl);
        this.addCommRawResponseListener(scl);
    }

    void addCommandEventListener(SerialCommunicatorListener scl) {
        this.commandEventListeners.add(scl);
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
        
        SerialCommunicatorEvent verbosity;
        if (!verbose) {
            verbosity = SerialCommunicatorEvent.CONSOLE_MESSAGE;
        }
        else {
            verbosity = SerialCommunicatorEvent.VERBOSE_CONSOLE_MESSAGE;
        }
        
        dispatchListenerEvents(verbosity, msg);
    }
    
    /**
     * A bunch of methods to dispatch listener events with various arguments.
     */
    protected void dispatchListenerEvents(final SerialCommunicatorEvent event, final String message) {
        dispatchListenerEvents(event, message, null);
    }
    
    protected void dispatchListenerEvents(final SerialCommunicatorEvent event, final GcodeCommand command) {
        dispatchListenerEvents(event, null, command);
    }

    private void dispatchListenerEvents(final SerialCommunicatorEvent event, 
                                            final String string, final GcodeCommand command) {
        if (event == COMMAND_SENT || event == COMMAND_SKIPPED) {
            if (command == null)
                throw new IllegalArgumentException("Dispatching a COMMAND_SENT event requires a GcodeCommand object.");
        } else if (string == null) {
            throw new IllegalArgumentException("Dispatching a " +event+ " event requires a String object.");
        }


        final ArrayList<SerialCommunicatorListener> sclList = eventMap.get(event);

        if (launchEventsInDispatchThread) {
            this.eventQueue.add(new EventData(event, sclList, string, command));
        } else {
            sendEventToListeners(event, sclList, string, command);
        }
    }

    private void sendEventToListeners(final SerialCommunicatorEvent event, 
                                            ArrayList<SerialCommunicatorListener> sclList,
                                            String string, GcodeCommand command) {
        switch(event) {
            case COMMAND_SENT:
                for (SerialCommunicatorListener scl : sclList)
                    scl.commandSent(command);
                break;
            case COMMAND_SKIPPED:
                for (SerialCommunicatorListener scl : sclList)
                    scl.commandSkipped(command);
                break;
            case CONSOLE_MESSAGE:
                for (SerialCommunicatorListener scl : sclList)
                    scl.messageForConsole(string);
                break;
            case VERBOSE_CONSOLE_MESSAGE:
                for (SerialCommunicatorListener scl : sclList)
                    scl.verboseMessageForConsole(string);
                break;
            case RAW_RESPONSE:
                for (SerialCommunicatorListener scl : sclList)
                    scl.rawResponseListener(string);
            default:

        }
    }

    /**
     * If commands complete very fast, like several comments in a row being
     * skipped, then multiple event handlers could process them out of order. To
     * prevent that from happening we use a blocking queue to add events in the
     * main thread, and process them in order a single event thread.
     */
    private final LinkedBlockingDeque<EventData> eventQueue = new LinkedBlockingDeque<>();
    private boolean stop = false;
    private Thread eventThread = new Thread(() -> {
        while (!stop) {
            try {
                EventData e = eventQueue.take();
                sendEventToListeners(e.event, e.sclList, e.string, e.command);
            } catch (Exception e) {}
        }
    });

    // Simple data class used to pass data to the event thread.
    private class EventData {
        public EventData(
                SerialCommunicatorEvent               event,
                ArrayList<SerialCommunicatorListener> sclList, 
                String                                string,
                GcodeCommand                          command) {
            this.sclList = sclList;
            this.event = event;
            this.command = command;
            this.string = string;
        }
        public ArrayList<SerialCommunicatorListener> sclList;
        public SerialCommunicatorEvent               event;
        public GcodeCommand                          command;
        public String                                string;
    }

}
