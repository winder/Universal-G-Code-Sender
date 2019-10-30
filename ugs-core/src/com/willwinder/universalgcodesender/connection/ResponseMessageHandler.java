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
package com.willwinder.universalgcodesender.connection;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles response messages from the serial connection buffering the data
 * until we have a complete line. It will then attempt to dispatch that
 * data to a communicator.
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class ResponseMessageHandler {

    private final StringBuilder inputBuffer;
    private Set<IConnectionListener> listeners = new HashSet<>();

    public ResponseMessageHandler() {
        inputBuffer = new StringBuilder();
    }



    /**
     * Appends the response data to a buffer, then splits out response rows and sends them
     * to the communicator using the {@link IConnectionListener#handleResponseMessage(String)}
     *
     * @param response a complete or part of a response message
     */
    public void handleResponse(String response) {
        inputBuffer.append(response);

        // Only continue if there is a line terminator and split out command(response).
        if (!inputBuffer.toString().contains("\n")) {
            return;
        }

        // Split with the -1 option will give an empty string at
        // the end if there is a terminator there as well.
        String[] messages = inputBuffer.toString().split("\\r?\\n", -1);
        for (int i = 0; i < messages.length; i++) {
            // Make sure this isn't the last command.
            if ((i + 1) < messages.length) {

                notifyListeners(messages[i]);

                // Append last command to input buffer because it didn't have a terminator.
            } else {
                inputBuffer.setLength(0);
                inputBuffer.append(messages[i]);
            }
        }
    }

    public void notifyListeners(String message) {
        listeners.forEach(listener -> {
            listener.handleResponseMessage(message);
        });
    }

    public void addListener(IConnectionListener connectionListener) {
        listeners.add(connectionListener);
    }
}
