/*
 * GRBL serial port interface class.
 */

/*
    Copywrite 2012-2016 Will Winder

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
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * @author wwinder
 */
public abstract class BufferedCommunicator extends AbstractCommunicator {
    
    // Command streaming variables
    private Boolean sendPaused = false;
    private GcodeCommand nextCommand;                      // Cached command.
    private BufferedReader    rawCommandStream;            // Arbitrary number of commands
    private GcodeStreamReader commandStream;               // Arbitrary number of commands
    private LinkedBlockingDeque<String> commandBuffer;     // Manually specified commands
    private LinkedBlockingDeque<GcodeCommand> activeCommandList;  // Currently running commands
    private int sentBufferSize = 0;
    
    private Boolean singleStepModeEnabled = false;
    
    abstract public int getBufferSize();
    
    protected void setQueuesForTesting(LinkedBlockingDeque<String> cb, LinkedBlockingDeque<GcodeCommand> asl) {
        this.commandBuffer = cb;
        this.activeCommandList = asl;
    }
    
    public BufferedCommunicator() {
    }
    
    @Override
    public void setSingleStepMode(boolean enable) {
        this.singleStepModeEnabled = enable;
    }
    
    @Override
    public boolean getSingleStepMode() {
        return this.singleStepModeEnabled;
    }
    
    /**
     * Add command to the command queue outside file mode. This is the only way
     * to send a command to the comm port without being in file mode.
     * These commands will be sent prior to any queued stream, they should
     * typically be control commands calculated by the application.
     */
    @Override
    public void queueStringForComm(final String input) {        
        String commandString = input;
        
        // Add command to queue
        this.commandBuffer.add(commandString);
    }
    

    /**
     * Arbitrary length of commands to send to the communicator.
     * @param input 
     */
    @Override
    public void queueRawStreamForComm(final Reader input) {
        rawCommandStream = new BufferedReader(input);
    }

    /**
     * Arbitrary length of commands to send to the communicator.
     * @param input 
     */
    @Override
    public void queueStreamForComm(final GcodeStreamReader input) {
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
    public void resetBuffersInternal() {
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
        return (this.activeCommandList.size() > 0)
                || (this.commandStream != null && this.commandStream.getNumRowsRemaining() > 0);
    }

    @Override
    public int numActiveCommands() {
        int streamingCount =
                commandStream == null ? 0 : commandStream.getNumRowsRemaining();
        return this.activeCommandList.size() + streamingCount;
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
     * @return 
     */
    private GcodeCommand getNextCommand() {
        if (nextCommand != null) {
            return nextCommand;
        }
        else if (!this.commandBuffer.isEmpty()) {
            nextCommand = new GcodeCommand(commandBuffer.pop());
        }
        else try {
            if (rawCommandStream != null && rawCommandStream.ready()) {
                nextCommand = new GcodeCommand(rawCommandStream.readLine());
            }
            else if (commandStream != null && commandStream.ready()) {
                nextCommand = commandStream.getNextCommand();
            }
        } catch (IOException ex) {
            // Fall through to null handling.
        }

        if (nextCommand != null) {
            nextCommand.setCommandNumber(getNextCommandId());
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
        Optional.of(this);
        // If there are no commands to send, exit.
        if (this.getNextCommand() == null) {
            return;
        }
        
        // If streaming is paused, exit.
        if (this.sendPaused) {
            // Another NO-OP
            return;
        }
        
        // Send command if:
        // There is room in the buffer.
        // AND We are NOT in single step mode.
        // OR  We are in single command mode and there are no active commands.
        while (this.getNextCommand() != null &&
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

            String commandString = command.getCommandString().trim();
            
            this.activeCommandList.add(command);
            this.sentBufferSize += (commandString.length() + 1);
        
            // Command already has a newline attached.
            this.sendMessageToConsoleListener(">>> " + commandString + "\n");
            
            try {
                this.sendingCommand(commandString);
                conn.sendStringToComm(commandString + "\n");
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
    public void cancelSend() {
        this.nextCommand = null;
        this.commandBuffer.clear();
        this.commandStream = null;
        this.sendPaused = false;
    }
    
    /**
     * This is to allow the GRBL Ctrl-C soft reset command.
     */
    @Override
    public void softReset() {
        this.commandBuffer.clear();
        this.activeCommandList.clear();
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
     * Processes message from GRBL. This should only be called from the
     * connection object.
     * @param response
     */
    @Override
    public void responseMessage(String response) {
        // Send this information back up to the Controller.
        dispatchListenerEvents(SerialCommunicatorEvent.RAW_RESPONSE, response);

        // Keep the data flow going in case of an "ok/error".
        if (processedCommand(response)) {
            // Pop the front of the active list.
            if (this.activeCommandList != null && this.activeCommandList.size() > 0) {
                GcodeCommand command = this.activeCommandList.pop();
                this.sentBufferSize -= (command.getCommandString().length() + 1);

                if (this.sendPaused == false) {
                    this.streamCommands();
                }
            }
        }
    }

    @Override
    public boolean openCommPort(String name, int baud) throws Exception {
        boolean ret = super.openCommPort(name, baud);
        
        if (ret) {
            this.commandBuffer = new LinkedBlockingDeque<>();
            this.activeCommandList = new LinkedBlockingDeque<>();
            this.sentBufferSize = 0;
        }
        return ret;
    }

    @Override
    public void closeCommPort() throws Exception {
        this.cancelSend();
        super.closeCommPort();
        
        this.sendPaused = false;
        this.commandBuffer = null;
        this.activeCommandList = null;
    }

    @Override
    public void sendByteImmediately(byte b) throws Exception {
        conn.sendByteImmediately(b);
    }
}
