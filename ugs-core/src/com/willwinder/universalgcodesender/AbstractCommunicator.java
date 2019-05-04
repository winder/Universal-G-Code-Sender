/*
    Copyright 2013-2018 Will Winder

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
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An Abstract communicator interface which implements listeners.
 *
 * @author wwinder
 */
public abstract class AbstractCommunicator {
    private static final Logger logger = Logger.getLogger(AbstractCommunicator.class.getName());

    protected Connection conn;

    // Allow events to be sent from same thread for unit tests.
    private boolean launchEventsInDispatchThread = true;

    // Serial Communicator Listener Events
    enum SerialCommunicatorEvent {
        COMMAND_SENT,
        COMMAND_SKIPPED,
        RAW_RESPONSE,
        PAUSED
    }
    // Callback interfaces
    private ArrayList<SerialCommunicatorListener> commandEventListeners;
    private ArrayList<SerialCommunicatorListener> commRawResponseListener;
    private HashMap<SerialCommunicatorEvent, ArrayList<SerialCommunicatorListener>> eventMap;

    public AbstractCommunicator() {
        this.commandEventListeners       = new ArrayList<>();
        this.commRawResponseListener     = new ArrayList<>();

        this.eventMap = new HashMap<>();
        eventMap.put(COMMAND_SENT,            commandEventListeners);
        eventMap.put(COMMAND_SKIPPED,         commandEventListeners);
        eventMap.put(PAUSED,                  commandEventListeners);
        eventMap.put(RAW_RESPONSE,            commRawResponseListener);
    }
    
    /*********************/
    /* Serial Layer API. */
    /*********************/
    abstract public void setSingleStepMode(boolean enable);
    abstract public boolean getSingleStepMode();
    abstract public void queueStringForComm(final String input);
    abstract public void queueStreamForComm(final GcodeStreamReader input);
    abstract public void sendByteImmediately(byte b) throws Exception;
    abstract public String activeCommandSummary();
    abstract public boolean areActiveCommands();
    abstract public void streamCommands();
    abstract public void pauseSend();
    abstract public void resumeSend();
    abstract public boolean isPaused();
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
    protected boolean openCommPort(ConnectionDriver connectionDriver, String name, int baud) throws Exception {
        if (conn == null) {
            String url = connectionDriver.getProtocol() + name + ":" + baud;
            conn = ConnectionFactory.getConnection(url);
            logger.info("Connecting to controller using class: " + conn.getClass().getSimpleName() + " with url " + url);
        }

        if (conn != null) {
            conn.setCommunicator(this);
        }
        
        if (conn==null) {
            throw new Exception(Localization.getString("communicator.exception.port") + ": " + name);
        }
        
        // Handle all events in a single thread.
        this.eventThread.start();

        //open it
        return conn.openPort();
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

    /** Getters & Setters. */
    abstract public String getLineTerminator();
    
    /* ****************** */
    /** Listener helpers. */
    /* ****************** */
    void setListenAll(SerialCommunicatorListener scl) {
        this.addCommandEventListener(scl);
        this.addCommRawResponseListener(scl);
    }

    public void removeListenAll(SerialCommunicatorListener scl) {
        this.removeCommandEventListener(scl);
        this.removeCommRawResponseListener(scl);
    }

    public void addCommandEventListener(SerialCommunicatorListener scl) {
        if (!this.commandEventListeners.contains(scl)) {
            this.commandEventListeners.add(scl);
        }
    }

    private void removeCommandEventListener(SerialCommunicatorListener scl) {
        this.commandEventListeners.remove(scl);
    }

    private void addCommRawResponseListener(SerialCommunicatorListener scl) {
        if (!this.commRawResponseListener.contains(scl)) {
            this.commRawResponseListener.add(scl);
        }
    }

    private void removeCommRawResponseListener(SerialCommunicatorListener scl) {
        this.commRawResponseListener.remove(scl);
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
            case RAW_RESPONSE:
                for (SerialCommunicatorListener scl : sclList)
                    scl.rawResponseListener(string);
                break;
            case PAUSED:
                sclList.forEach(SerialCommunicatorListener::communicatorPausedOnError);
                break;
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
            } catch (InterruptedException ignored) {
                stop = true;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Couldn't send event", e);
                stop = true;
            }
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
