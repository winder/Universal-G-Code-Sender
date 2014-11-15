/*
 * GRBL serial port interface class.
 */

/*
    Copywrite 2012-2013 Will Winder

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

import com.willwinder.universalgcodesender.types.GcodeCommand;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.*;
import java.util.LinkedList;
import java.util.TooManyListenersException;

/**
 *
 * @author wwinder
 */
public class GrblCommunicator extends AbstractCommunicator {// extends AbstractSerialCommunicator {
    
    // Command streaming variables
    private Boolean sendPaused = false;
    private LinkedList<String> commandBuffer;     // All commands in a file
    private LinkedList<String> activeStringList;  // Currently running commands
    private int sentBufferSize = 0;
    
    private Boolean singleStepModeEnabled = false;
    
    public GrblCommunicator() {
        this.setLineTerminator("\r\n");
    }
    
    /**
     * This constructor is for dependency injection so a mock serial device can
     * act as GRBL.
     */
    protected GrblCommunicator(
            LinkedList<String> cb, LinkedList<String> asl, Connection c) {
        // Base constructor.
        this();
        //TODO-f4grx-DONE: Mock connection
        this.conn = c;
        this.conn.setCommunicator(this);
        
        this.commandBuffer = cb;
        this.activeStringList = asl;
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
     * Streams anything in the command buffer to the comm port.
     */
    @Override
    public void streamCommands() {
        if (this.commandBuffer.size() == 0) {
            // NO-OP
            return;
        }
        
        if (this.sendPaused) {
            // Another NO-OP
            return;
        }
        
        // Send command if:
        // There is room in the buffer.
        // AND We are NOT in single step mode.
        // OR  We are in single command mode and there are no active commands.
        while (CommUtils.checkRoomInBuffer(this.sentBufferSize, this.commandBuffer.peek())
                && allowMoreCommands()) {
            String commandString = this.commandBuffer.pop();
            this.activeStringList.add(commandString);
            this.sentBufferSize += commandString.length();
            
            // Newlines are embedded when they get queued so just send it.
        
            // Command already has a newline attached.
            this.sendMessageToConsoleListener(">>> " + commandString);
            conn.sendStringToComm(commandString);
            
            dispatchListenerEvents(COMMAND_SENT, this.commandSentListeners, commandString.trim());
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
        this.commandBuffer.clear();
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
     * Processes message from GRBL. This should only be called from the
     * connection object.
     */
    @Override
    public void responseMessage(String response) {
        // Send this information back up to the Controller.
        dispatchListenerEvents(RAW_RESPONSE, this.commRawResponseListener, response);

        // Keep the data flow going in case of an "ok/error".
        if (GcodeCommand.isOkErrorResponse(response)) {
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
    public boolean openCommPort(String name, int baud) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException, Exception {
        boolean ret = super.openCommPort(name, baud);
        
        if (ret) {
            this.commandBuffer = new LinkedList<>();
            this.activeStringList = new LinkedList<>();
            this.sentBufferSize = 0;
        }
        return ret;
    }

    @Override
    public void closeCommPort() {
        this.cancelSend();
        super.closeCommPort();
        
        this.sendPaused = false;
        this.commandBuffer = null;
        this.activeStringList = null;
    }

    @Override
    public void sendByteImmediately(byte b) throws IOException {
        conn.sendByteImmediately(b);
    }
}
