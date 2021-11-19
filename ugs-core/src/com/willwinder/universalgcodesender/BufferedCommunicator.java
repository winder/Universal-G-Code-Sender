/*
    Copyright 2012-2018 Will Winder

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

import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GRBL serial port interface class.
 *
 * @author wwinder
 */
public abstract class BufferedCommunicator extends AbstractCommunicator {
    private static final Logger logger = Logger.getLogger(BufferedCommunicator.class.getName());

    // Command streaming variables
    private Boolean sendPaused = false;
    private GcodeCommand nextCommand;                      // Cached command.
    private IGcodeStreamReader commandStream;               // Arbitrary number of commands
    private final LinkedBlockingDeque<GcodeCommand> commandBuffer;     // Manually specified commands
    private final LinkedBlockingDeque<GcodeCommand> activeCommandList;  // Currently running commands
    private int sentBufferSize = 0;
    
    private Boolean singleStepModeEnabled = false;
    
    abstract public int getBufferSize();

    public BufferedCommunicator() {
        this.commandBuffer = new LinkedBlockingDeque<>();
        this.activeCommandList = new LinkedBlockingDeque<>();
    }

    public BufferedCommunicator(LinkedBlockingDeque<GcodeCommand> cb, LinkedBlockingDeque<GcodeCommand> asl) {
        this.commandBuffer = cb;
        this.activeCommandList = asl;
    }
    
    @Override
    public void setSingleStepMode(boolean enable) {
        this.singleStepModeEnabled = enable;
    }
    
    @Override
    public boolean getSingleStepMode() {
        return this.singleStepModeEnabled;
    }

    @Override
    public void queueCommand(GcodeCommand command) {
        // Add command to queue
        this.commandBuffer.add(command);
    }

    @Override
    public void queueStreamForComm(final IGcodeStreamReader input) {
        commandStream = input;
    }
       
    /*
    // TODO: Figure out why this isn't working ...
    boolean isCommPortOpen() throws NoSuchPortException {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.commPort.getName());
            String owner = portIdentifier.getCurrentOwner();
            String thisClass = this.getClass().getName();
            
            return portIdentifier.isCurrentlyOwned() && owner.equals(thisClass);                    
    }
    */
    
    /** File Stream Methods. **/
    @Override
    public void resetBuffers() {
        super.resetBuffers();
        if (activeCommandList != null) {
            activeCommandList.clear();
        }
    }

    @Override
    public String activeCommandSummary() {
        StringBuilder sb = new StringBuilder();
        String comma = "";

        for (GcodeCommand gc : activeCommandList) {
            sb.append(comma).append(gc.getCommandString());
            comma = ", ";
        }

        if (commandStream != null) {
            sb.append(comma)
                    .append(commandStream.getNumRowsRemaining())
                    .append(" streaming commands.");
        }

        return sb.toString();
    }
    
    @Override
    public boolean areActiveCommands() {
        return numActiveCommands() > 0;
    }

    @Override
    public int numActiveCommands() {
        int streamingCount =
                commandStream == null ? 0 : commandStream.getNumRowsRemaining();
        int cachedCommand = nextCommand == null ? 0 : 1;
        return this.activeCommandList.size() + streamingCount + cachedCommand;
    }

    public int numBufferedCommands() {
        return commandBuffer.size();
    }

    // Helper for determining if commands should be throttled.
    private boolean allowMoreCommands() {
        if (this.singleStepModeEnabled) {
            return this.activeCommandList.isEmpty();
        }
        return true;
    }
    
    /**
     * THIS COMMAND CAN ONLY BE CALLED FROM streamCommands UNLESS
     * THE nextCommand OBJECT IS SYNCHRONIZED.
     * 
     * Returns the next command with the following priority:
     * 1. nextCommand object if set.
     * 2. Front of the commandBuffer collection.
     * 3. Next line in the commandStream.
     * @return the next command to be streamed
     */
    private GcodeCommand getNextCommand() {
        if (nextCommand != null) {
            return nextCommand;
        }
        else if (!this.commandBuffer.isEmpty()) {
            nextCommand = commandBuffer.pop();
        }
        else try {
            if (commandStream != null && commandStream.ready()) {
                nextCommand = commandStream.getNextCommand();
            }
        } catch (IOException ignored) {
            // Fall through to null handling.
        }

        if (nextCommand != null) {
            if (nextCommand.getCommandString().endsWith("\n")) {
                nextCommand.setCommand(nextCommand.getCommandString().trim());
            }
            return nextCommand;
        }
        return null;
    }
   
    /**
     * Streams anything in the command buffer to the comm port.
     * Synchronized to prevent commands from sending out of order.
     */
    @Override
    synchronized public void streamCommands() {
        // If there are no commands to send, exit.
        if (this.getNextCommand() == null) {
            logger.log(Level.FINE, "There are no more commands to stream");
            return;
        }
        
        // Send command if:
        // There is room in the buffer.
        // AND we are NOT paused
        // AND We are NOT in single step mode.
        // OR  We are in single command mode and there are no active commands.
        while (this.getNextCommand() != null &&
                !isPaused() &&
                CommUtils.checkRoomInBuffer(
                    this.sentBufferSize,
                    this.getNextCommand().getCommandString(),
                    this.getBufferSize())
                && allowMoreCommands()) {

            GcodeCommand command = this.getNextCommand();

            if (command.getCommandString().isEmpty()) {
                dispatchListenerEvents(COMMAND_SKIPPED, command);
                nextCommand = null;
                continue;
            }

            String commandString = command.getCommandString();
            
            this.activeCommandList.add(command);
            this.sentBufferSize += (commandString.length() + 1);

            try {
                this.sendingCommand(commandString);
                connection.sendStringToComm(commandString + "\n");
                dispatchListenerEvents(COMMAND_SENT, command);
                nextCommand = null;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
    
    @Override
    public void pauseSend() {
        this.sendPaused = true;
    }
    
    @Override
    public void resumeSend() {
        this.sendPaused = false;
        this.streamCommands();
    }

    @Override
    public boolean isPaused() {
        return sendPaused;
    }
    
    @Override
    public void cancelSend() {
        this.nextCommand = null;
        this.commandBuffer.clear();
        this.activeCommandList.clear();
        this.commandStream = null;
        this.sendPaused = false;
        this.sentBufferSize = 0;
    }

    /**
     * Notifies the subclass that a command has been sent.
     * @param command The command being sent.
     */
    abstract protected void sendingCommand(String command);
    
    /**
     * Returns whether or not a command has been completed based on a response
     * from the controller.
     * @param response
     * @return true if a command has completed.
     */
    abstract protected boolean processedCommand(String response);

    /**
     * Returns whether or not a completed command had an error based on a
     * response from the controller.
     * @param response
     * @return true if a command has completed.
     */
    abstract protected boolean processedCommandIsError(String response);
    
    /** 
     * Processes message from GRBL. This should only be called from the
     * connection object.
     * @param response
     */
    @Override
    public void handleResponseMessage(String response) {
        // Send this information back up to the Controller.
        dispatchListenerEvents(SerialCommunicatorEvent.RAW_RESPONSE, response);


        // Pause if there was an error and if there are more commands queued
        if (processedCommandIsError(response) &&
                (nextCommand != null                    // No cached command
                    || (activeCommandList.size() > 1)   // No more commands (except for the one being popped further down)
                    || (commandStream != null && commandStream.getNumRowsRemaining() > 0) // No more rows in stream
                    || (commandBuffer != null && commandBuffer.size() > 0))) { // No commands in buffer

            pauseSend();
            dispatchListenerEvents(PAUSED, "");
        }

        // Keep the data flow going in case of an "ok" or an "error".
        if (processedCommand(response)) {
            // Pop the front of the active list.
            if (this.activeCommandList != null && this.activeCommandList.size() > 0) {
                GcodeCommand command = this.activeCommandList.pop();
                this.sentBufferSize -= (command.getCommandString().length() + 1);

                if (!isPaused()) {
                    this.streamCommands();
                }
            }
        }
    }

    @Override
    public void connect(ConnectionDriver connectionDriver, String name, int baud) throws Exception {
        super.connect(connectionDriver, name, baud);

        this.commandBuffer.clear();
        this.activeCommandList.clear();
        this.sentBufferSize = 0;
    }

    @Override
    public void disconnect() throws Exception {
        this.cancelSend();
        super.disconnect();
        
        this.sendPaused = false;
        this.commandBuffer.clear();
        this.activeCommandList.clear();
    }

    @Override
    public void sendByteImmediately(byte b) throws Exception {
        connection.sendByteImmediately(b);
    }
}
