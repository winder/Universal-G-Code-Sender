/*
 * GRBL serial port interface class.
 */

/*
    Copywrite 2012-2015 Will Winder

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

import com.willwinder.universalgcodesender.utils.CommUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public abstract class BufferedCommunicator extends AbstractCommunicator {// extends AbstractSerialCommunicator {
    
    // Command streaming variables
    private Boolean sendPaused = false;
    private String nextCommand;                            // Cached command.
    Object nextCommandLock = new Object();
    private BufferedReader commandStream;                  // Arbitrary number of commands
    private LinkedBlockingDeque<String> commandBuffer;     // Manually specified commands
    private LinkedBlockingDeque<String> activeStringList;  // Currently running commands
    private int sentBufferSize = 0;
    
    private Boolean singleStepModeEnabled = false;
    
    //abstract public String getLineTerminator();
    abstract public int getBufferSize();
    
    protected void setQueuesForTesting(LinkedBlockingDeque<String> cb, LinkedBlockingDeque<String> asl) {
        this.commandBuffer = cb;
        this.activeStringList = asl;
    }
    
    public BufferedCommunicator() {
        //this.setLineTerminator(getLineTerminator());
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
        
        if (! commandString.endsWith("\n")) {
            commandString += "\n";
        }
        
        // Add command to queue
        this.commandBuffer.add(commandString);
    }
    
    /**
     * Arbitrary length of commands to send to the communicator.
     * @param input 
     */
    @Override
    public void queueStreamForComm(final Reader input) {
        commandStream = new BufferedReader(input);
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
    public boolean areActiveCommands() {
        return (this.activeStringList.size() > 0);
    }
    
    // Helper for determining if commands should be throttled.
    private boolean allowMoreCommands() {
        if (this.singleStepModeEnabled) {
            if (this.areActiveCommands()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns the next command with the following priority:
     * 1. nextCommand object if set.
     * 2. Front of the commandBuffer collection.
     * 3. Next line in the commandStream.
     * @return 
     */
    private Optional<String> getNextCommand() {
        synchronized (nextCommandLock) {
            if (nextCommand != null) {
                return Optional.of(nextCommand);
            }
            else if (!this.commandBuffer.isEmpty()) {
                nextCommand = commandBuffer.pop();
            }
            else try {
                if (commandStream != null && commandStream.ready()) {
                    // TODO: Special reader is going to be needed for feature parity
                    //       it will need to handle this thing.
                    nextCommand = commandStream.readLine() + "\n";
                }
            } catch (IOException ex) {
                // Fall through to null handling.
            }

            if (nextCommand != null) {
                return Optional.of(nextCommand);
            }
        }
        return Optional.empty();
    }
   
    /**
     * Streams anything in the command buffer to the comm port.
     */
    @Override
    public void streamCommands() {
        // If there are no commands to send, exit.
        if (!this.getNextCommand().isPresent()) {
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
        while (this.getNextCommand().isPresent() &&
                CommUtils.checkRoomInBuffer(
                    this.sentBufferSize,
                    this.getNextCommand().get(),
                    this.getBufferSize())
                && allowMoreCommands()) {

            String commandString = this.getNextCommand().get();
            this.activeStringList.add(commandString);

            this.sentBufferSize += commandString.length();
            
            // Newlines are embedded when they get queued so just send it.
        
            // Command already has a newline attached.
            this.sendMessageToConsoleListener(">>> " + commandString);
            
            try {
                this.sendingCommand(commandString);
                conn.sendStringToComm(commandString);
                synchronized(nextCommandLock) {
                    nextCommand = null;
                }
                dispatchListenerEvents(COMMAND_SENT, this.commandSentListeners, commandString.trim());
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
        synchronized (nextCommandLock) {
            this.nextCommand = null;
        }
        this.commandBuffer.clear();
        this.commandStream = null;
    }
    
    /**
     * This is to allow the GRBL Ctrl-C soft reset command.
     */
    @Override
    public void softReset() {
        this.commandBuffer.clear();
        this.activeStringList.clear();
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
        dispatchListenerEvents(RAW_RESPONSE, this.commRawResponseListener, response);

        // Keep the data flow going in case of an "ok/error".
        if (processedCommand(response)) {
            // Pop the front of the active list.
            if (this.activeStringList != null && this.activeStringList.size() > 0) {
                String commandString = this.activeStringList.pop();
                this.sentBufferSize -= commandString.length();

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
            this.activeStringList = new LinkedBlockingDeque<>();
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
        this.activeStringList = null;
    }

    @Override
    public void sendByteImmediately(byte b) throws Exception {
        conn.sendByteImmediately(b);
    }
}
