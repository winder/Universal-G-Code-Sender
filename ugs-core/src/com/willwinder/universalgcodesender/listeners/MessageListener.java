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
package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.services.MessageService;

/**
 * An interface for a message listener which can be used to listen for console messages.
 * Register this listener using {@link MessageService#addListener(MessageListener)}
 * and it will receive all messages dispatched to be written to the console.
 *
 * @author Joacim Breiler
 */
public interface MessageListener {

    /**
     * This method will be called when a new message is received to be written to the console.
     *
     * @param messageType the type of message to be written
     * @param message     the message to be written to the console
     */
    void onMessage(MessageType messageType, String message);
}
