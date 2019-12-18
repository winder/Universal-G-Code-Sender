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

package com.willwinder.universalgcodesender.connection;

import java.util.List;

/**
 * Connection interface.
 *
 * @author wwinder
 */
public interface Connection {

    /**
     * Adds a listener for events from the connection
     *
     * @param connectionListener a connection listener
     */
    void addListener(IConnectionListener connectionListener);

    /**
     * Sets the connection URI for the hardware and driver to connect with.
     *
     * Example with a serial port using the JSSC driver and baud rate 9600
     * jssc://dev/tty.usbmodem1421:9600
     *
     * Example with a serial port using the JSerialComm driver and baud rate 115200
     * jserialcomm://tty.usbmodem1421:115200
     *
     * Example with a TCP port using TCPConnection driver to example.com and port 9001
     * tcp://example.com:9001
     *
     * @param uri the connection uri for the hardware to connect to
     */
    void setUri(String uri);

    /**
     * Opens the connection
     *
     * @return true if the connection was established
     * @throws Exception if the connection couldn't be established
     */
    boolean openPort() throws Exception;

    /**
     * Closes the connection
     *
     * @throws Exception if the connection couldn't be closed
     */
    void closePort() throws Exception;

    /**
     * Immediately sends a byte, used for real-time commands.
     *
     * @param b the byte to send
     */
    void sendByteImmediately(byte b) throws Exception;

    /**
     * Sends a command to the serial device. This actually streams the bits to
     * the comm port.
     *
     * @param command Command to be sent to serial device.
     */
    void sendStringToComm(String command) throws Exception;

    /**
     * Checks if the communication is established
     *
     * @return true if connection is established.
     */
    boolean isOpen();

    /**
     * Returns a list of all port names available
     *
     * @return a list of available port names
     */
    List<String> getPortNames();
}
