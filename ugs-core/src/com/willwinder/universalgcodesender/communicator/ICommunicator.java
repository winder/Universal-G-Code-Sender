/*
    Copyright 2019 Will Winder

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
package com.willwinder.universalgcodesender.communicator;

import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.connection.IConnectionListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;

import java.io.IOException;

/**
 * An interface for describing a communicator, responsible for handling gcode command
 * queues and its streaming to a hardware connection.
 * <p>
 * To make ensure the performance of the stream, the events dispatched from this service should
 * be sent using a separate queue or else any slow UI operations may starve the command
 * stream to the service.
 *
 * @author Joacim Breiler
 */
public interface ICommunicator extends IConnectionListener {

    /**
     * Add command to the command buffer outside file mode. These commands will be sent
     * prior to any queued stream, they should typically be control commands calculated
     * by the application.
     * <p>
     * Invoke the method {@link #streamCommands()} to start sending any queued commands.
     *
     * @param command the command to send
     */
    void queueCommand(GcodeCommand command);

    /**
     * Arbitrary length of commands to send to the communicator which will be added
     * to the command buffer.
     * <p>
     * Invoke the method {@link #streamCommands()} to start sending any queued commands
     * from the stream.
     *
     * @param stream a stream of gcode commands to send
     */
    void queueStreamForComm(IGcodeStreamReader stream);

    /**
     * Sends a single byte to the controller immediately. These are typically control
     * bytes for special hardware commands which will be sent prior to any other queued
     * commands or streams.
     *
     * @param b the byte to send to the controller
     * @throws Exception if the byte couldn't be sent
     */
    void sendByteImmediately(byte b) throws Exception;

    String activeCommandSummary();

    /**
     * Returns if there is any active commands that has been sent or is being processed
     * by the hardware. These include streams or single queued commands.
     *
     * @return true if there is active commands being processed
     */
    boolean areActiveCommands();

    /**
     * Streams anything in the command buffer to the hardware.
     */
    void streamCommands();

    /**
     * Pause the streaming of commands in the command buffer.
     */
    void pauseSend();

    /**
     * Resumes the streaming of command buffer
     */
    void resumeSend();

    /**
     * Returns if the command stream has been paused.
     *
     * @return true if the command stream has been paused.
     */
    boolean isPaused();

    /**
     * Cancels the streaming of commands and resets all buffers.
     */
    void cancelSend();

    /**
     * Returns the number of active commands queued for streaming
     *
     * @return the number of commands in the queue
     */
    int numActiveCommands();

    /**
     * Resets internal buffers.
     */
    void resetBuffers();

    /**
     * Sets a connection to the communicator
     *
     * @param connection the connection to use
     */
    void setConnection(Connection connection);

    /**
     * Returns true if connected to the hardware
     *
     * @return true if connected
     */
    boolean isConnected();

    /**
     * Returns if the communicator should send one command at the time, waiting
     * for a response from the hardware.
     *
     * @return true if using single stepping.
     */
    boolean getSingleStepMode();

    /**
     * Enables or disables if single stepping should be used for streaming commands.
     * <p>
     * If enabled the communicator will issue one command at the time, waiting for the
     * hardware to respond before sending next command.
     * <p>
     * If disabled the communicator will send multiple commands to the hardware to make
     * sure all send buffers are filled. This could make the job faster as the hardware
     * has more commands for planning the movement.
     *
     * @param enable set to true to enable single stepping.
     */
    void setSingleStepMode(boolean enable);

    /**
     * Removes listeners for notifying about the progress for sending commands.
     *
     * @param listener a listener to remove
     */
    void removeListener(ICommunicatorListener listener);

    /**
     * Adds listeners to notify about the progress for sending commands.
     *
     * @param listener a listener to add
     */
    void addListener(ICommunicatorListener listener);

    /**
     * Connects to the hardware
     *
     * @param connectionDriver the connection driver to use
     * @param port             the port adress to use (i.e. /dev/ttyUSB0)
     * @param portRate         the port rate to use
     * @throws Exception if the connection couldn't be established
     */
    void connect(ConnectionDriver connectionDriver, String port, int portRate) throws Exception;

    /**
     * Disconnects from the hardware
     *
     * @throws Exception if the hardware couldn't be disconnected
     */
    void disconnect() throws Exception;

    /**
     * Enters a mode for receiving files using the xmodem protocol and return the data as a byte array.
     * This mode will block until the file stream has been received or until the protocol times out or an error occurs.
     *
     * @return a byte array with the received file
     * @throws IOException if there is a protocol error or a timeout occurs.
     */
    byte[] xmodemReceive() throws IOException;

    /**
     * Enters a mode for sending files using the xmodem protocol as a byte array.
     * This mode will block until the file stream has been sent or until the protocol times out or an error occurs.
     *
     * @param data the data to send
     * @throws IOException if there is a protocol error or a timeout occurs.
     */
    void xmodemSend(byte[] data) throws IOException;
}
