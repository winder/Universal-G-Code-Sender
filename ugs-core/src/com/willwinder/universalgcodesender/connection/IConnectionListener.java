/*
    Copyright 2019-2024 Will Winder

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

/**
 * An interface for listening on response messages from a connection
 */
public interface IConnectionListener {

    /**
     * Method is invoked when a new response message is received from
     * the connection.
     *
     * @param response a response message
     */
    void handleResponseMessage(String response);

    /**
     * This method will be executed if the connection is closed
     */
    void onConnectionClosed();
}
