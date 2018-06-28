/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;

import java.util.HashSet;
import java.util.Set;

/**
 * A service for handling message listeners and for dispatching messages to them.
 *
 * @author Joacim Breiler
 */
public class MessageService {

    /**
     * A set of message listeners
     */
    private final Set<MessageListener> listeners;

    /**
     * Default constructor
     */
    public MessageService() {
        this.listeners = new HashSet<>();
    }

    /**
     * Dispatches a message to all message listeners.
     *
     * @param messageType the verbosity of the message
     * @param message the message text to be written
     */
    public void dispatchMessage(MessageType messageType, String message) {
        listeners.forEach(listener -> listener.onMessage(messageType, message));
    }

    /**
     * Adds a new listener for console messages
     *
     * @param messageListener the message listener to add
     */
    public void addListener(MessageListener messageListener) {
        listeners.add(messageListener);
    }

    /**
     * Removes a message listener
     *
     * @param messageListener the message listener to remove
     */
    public void removeListener(MessageListener messageListener) {
        listeners.remove(messageListener);
    }
}
