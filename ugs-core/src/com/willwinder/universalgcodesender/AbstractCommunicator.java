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

import com.willwinder.universalgcodesender.communicator.event.AsyncCommunicatorEventDispatcher;
import com.willwinder.universalgcodesender.communicator.event.CommunicatorEvent;
import com.willwinder.universalgcodesender.communicator.event.ICommunicatorEventDispatcher;
import com.willwinder.universalgcodesender.communicator.event.CommunicatorEventType;
import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.io.IOException;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.communicator.event.CommunicatorEventType.COMMAND_SENT;
import static com.willwinder.universalgcodesender.communicator.event.CommunicatorEventType.COMMAND_SKIPPED;

/**
 * An Abstract communicator interface which implements listeners.
 *
 * @author wwinder
 */
public abstract class AbstractCommunicator implements ICommunicator {
    private static final Logger logger = Logger.getLogger(AbstractCommunicator.class.getName());

    private final ICommunicatorEventDispatcher eventDispatcher;

    protected Connection connection;

    protected AbstractCommunicator() {
        this(new AsyncCommunicatorEventDispatcher());
    }

    protected AbstractCommunicator(ICommunicatorEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    /*********************/
    /* Serial Layer API. */
    /*********************/
    @Override
    public void resetBuffers() {
        eventDispatcher.reset();
    }

    @Override
    public void setConnection(Connection c) {
        connection = c;
        c.addListener(this);
    }

    //do common operations (related to the connection, that is shared by all communicators)
    @Override
    public void connect(ConnectionDriver connectionDriver, String name, int baud) throws Exception {
        String url = connectionDriver.getProtocol() + name + ":" + baud;
        if (connection == null) {
            connection = ConnectionFactory.getConnection(url);
            logger.info("Connecting to controller using class: " + connection.getClass().getSimpleName() + " with url " + url);
        }

        if (connection != null) {
            connection.addListener(this);
        }

        if (connection == null) {
            throw new Exception(Localization.getString("communicator.exception.port") + ": " + name);
        }

        eventDispatcher.start();

        //open it
        if (!connection.openPort()) {
           throw new Exception("Could not connect to controller on port " + url);
        }
    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }


    //do common things (related to the connection, that is shared by all communicators)
    @Override
    public void disconnect() throws Exception {
        eventDispatcher.stop();
        connection.closePort();
    }

    /* ****************** */

    @Override
    public void removeListener(CommunicatorListener scl) {
        eventDispatcher.removeListener(scl);
    }

    @Override
    public void addListener(CommunicatorListener scl) {
        eventDispatcher.addListener(scl);
    }

    /**
     * A bunch of methods to dispatch listener events with various arguments.
     */
    protected void dispatchListenerEvents(final CommunicatorEventType event, final String message) {
        dispatchListenerEvents(event, message, null);
    }

    protected void dispatchListenerEvents(final CommunicatorEventType event, final GcodeCommand command) {
        dispatchListenerEvents(event, null, command);
    }

    private void dispatchListenerEvents(final CommunicatorEventType event,
                                        final String string, final GcodeCommand command) {
        if (event == COMMAND_SENT || event == COMMAND_SKIPPED) {
            if (command == null) {
                throw new IllegalArgumentException("Dispatching a COMMAND_SENT event requires a GcodeCommand object.");
            }
        } else if (string == null) {
            throw new IllegalArgumentException("Dispatching a " + event + " event requires a String object.");
        }

        eventDispatcher.dispatch(new CommunicatorEvent(event, string, command));
    }

    @Override
    public byte[] xmodemReceive() throws IOException {
        return connection.xmodemReceive();
    }

    @Override
    public void xmodemSend(byte[] data) throws IOException {
        connection.xmodemSend(data);
    }
}
