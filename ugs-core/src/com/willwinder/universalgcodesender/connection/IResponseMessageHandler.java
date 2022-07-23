/*
    Copyright 2022 Will Winder

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
 * Handles response messages from the serial connection and dispatches them listeners
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public interface IResponseMessageHandler {
    /**
     * Receives byte data from the serial connection to be processed.
     *
     * @param buffer the byte buffer with data
     * @param offset the start index of the byte buffer to read from
     * @param length the number of bytes to read
     */
    void handleResponse(byte[] buffer, int offset, int length);

    void addListener(IConnectionListener connectionListener);

    void notifyListeners(String message);
}
