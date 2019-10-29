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

import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.AbstractCommunicator.SerialCommunicatorEvent.COMMAND_SENT;
import static com.willwinder.universalgcodesender.AbstractCommunicator.SerialCommunicatorEvent.COMMAND_SKIPPED;

/**
 * An Abstract communicator interface which implements listeners.
 *
 * @author wwinder
 */
public abstract class AbstractCommunicator implements ICommunicator {
    private static final Logger logger = Logger.getLogger(AbstractCommunicator.class.getName());

    protected Connection connection;

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
    private Set<CommunicatorListener> communicatorListeners;

    public AbstractCommunicator() {
        this.communicatorListeners = new HashSet<>();
    }

    /*********************/
    /* Serial Layer API. */
    /*********************/
    @Override
    public void resetBuffers() {
        if (eventQueue != null) {
            eventQueue.clear();
        }
    }

    @Override
    public void setConnection(Connection c) {
        connection = c;
        c.addListener(this);
    }

    //do common operations (related to the connection, that is shared by all communicators)
    @Override
    public void connect(ConnectionDriver connectionDriver, String name, int baud) throws Exception {
        if (connection == null) {
            String url = connectionDriver.getProtocol() + name + ":" + baud;
            connection = ConnectionFactory.getConnection(url);
            logger.info("Connecting to controller using class: " + connection.getClass().getSimpleName() + " with url " + url);
        }

        if (connection != null) {
            connection.addListener(this);
        }

        if (connection == null) {
            throw new Exception(Localization.getString("communicator.exception.port") + ": " + name);
        }

        // Handle all events in a single thread.
        this.eventThread.start();

        //open it
        connection.openPort();
    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }


    //do common things (related to the connection, that is shared by all communicators)
    @Override
    public void disconnect() throws Exception {
        this.stop = true;
        this.eventThread.interrupt();
        connection.closePort();
    }

    /* ****************** */

    @Override
    public void removeListener(CommunicatorListener scl) {
        this.communicatorListeners.remove(scl);
    }

    @Override
    public void addListener(CommunicatorListener scl) {
        this.communicatorListeners.add(scl);
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
            if (command == null) {
                throw new IllegalArgumentException("Dispatching a COMMAND_SENT event requires a GcodeCommand object.");
            }
        } else if (string == null) {
            throw new IllegalArgumentException("Dispatching a " + event + " event requires a String object.");
        }

        if (launchEventsInDispatchThread) {
            this.eventQueue.add(new EventData(event, string, command));
        } else {
            sendEventToListeners(event, string, command);
        }
    }

    private void sendEventToListeners(final SerialCommunicatorEvent event,
                                      String string, GcodeCommand command) {
        switch (event) {
            case COMMAND_SENT:
                for (CommunicatorListener scl : communicatorListeners)
                    scl.commandSent(command);
                break;
            case COMMAND_SKIPPED:
                for (CommunicatorListener scl : communicatorListeners)
                    scl.commandSkipped(command);
                break;
            case RAW_RESPONSE:
                for (CommunicatorListener scl : communicatorListeners)
                    scl.rawResponseListener(string);
                break;
            case PAUSED:
                communicatorListeners.forEach(CommunicatorListener::communicatorPausedOnError);
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
                sendEventToListeners(e.event, e.string, e.command);
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
                SerialCommunicatorEvent event,
                String string,
                GcodeCommand command) {
            this.event = event;
            this.command = command;
            this.string = string;
        }

        public SerialCommunicatorEvent event;
        public GcodeCommand command;
        public String string;
    }
}
