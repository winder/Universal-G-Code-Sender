/*
 * TinyG serial port interface class.
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

import com.willwinder.universalgcodesender.types.TinyGGcodeCommand;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.TooManyListenersException;

/**
 *
 * @author wwinder
 */
public class TinyGCommunicator extends AbstractCommunicator {
    Connection conn;
    
    // Command streaming variables
    private Boolean sendPaused = false;
    private LinkedList<String> commandBuffer;     // All commands in a file
    private LinkedList<String> activeStringList;  // Currently running commands
    private int sentBufferSize = 0;
    
    
    TinyGCommunicator(Connection c) {
        this.setLineTerminator("\r\n");
        this.conn = c;
    }
    
    /**
     * This constructor is for dependency injection so a mock serial device can
     * act as GRBL.
     */
    protected TinyGCommunicator(final InputStream in, final OutputStream out,
            LinkedList<String> cb, LinkedList<String> asl) {
        // Base constructor.
        this(new SerialConnection());
        
        //this.in = in;
        //this.out = out;
        this.commandBuffer = cb;
        this.activeStringList = asl;
    }
    
    // TODO: Override openCommPort and use socket flow control?

    @Override
    public void setSingleStepMode(boolean enable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean getSingleStepMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** 
     * Processes message from GRBL.
     */
    @Override
    protected void responseMessage(String response) {
        // GrblCommunicator no longer knows what to do with responses.
        dispatchListenerEvents(RAW_RESPONSE, this.commRawResponseListener, response);

        // Keep the data flow going for now.
        if (TinyGGcodeCommand.isOkErrorResponse(response)) {
            // Pop the front of the active list.
            String commandString = this.activeStringList.pop();
            this.sentBufferSize -= commandString.length();
            
            TinyGGcodeCommand command = new TinyGGcodeCommand(commandString);
            command.setResponse(response);

            dispatchListenerEvents(COMMAND_COMPLETE, this.commandCompleteListeners, command);

            if (this.sendPaused == false) {
                this.streamCommands();
            }
        }
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


    @Override
    public boolean areActiveCommands() {
        return (this.activeStringList.size() > 0);
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
        
        // TODO: Find out rules for TinyG buffer size
        // Try sending the first command.
        while (CommUtils.checkRoomInBuffer(this.sentBufferSize, this.commandBuffer.peek())) {
            String commandString = this.commandBuffer.pop();
            this.activeStringList.add(commandString);
            this.sentBufferSize += commandString.length();
            
            // Newlines are embedded when they get queued so just send it.
            conn.sendStringToComm(commandString);
            
            TinyGGcodeCommand command = new TinyGGcodeCommand(commandString);
            command.setSent(true);
            dispatchListenerEvents(COMMAND_SENT, this.commandSentListeners, command);
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
    
    @Override
    public void softReset() {
        throw new UnsupportedOperationException("Not supported yet.");
        /*
        this.commandBuffer.clear();
        this.activeStringList.clear();
        this.sentBufferSize = 0;
        */
    }

    @Override
    public boolean openCommPort(String name, int baud) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException, Exception {
        boolean ret = conn.openCommPort(name, baud);
        
        if (ret) {
            this.commandBuffer = new LinkedList<String>();
            this.activeStringList = new LinkedList<String>();
            this.sentBufferSize = 0;
        }
        
        return ret;
    }

    @Override
    public void closeCommPort() {
        this.cancelSend();
        conn.closeCommPort();

        this.sendPaused = false;
        this.commandBuffer = null;
        this.activeStringList = null;
    }

    @Override
    public void sendByteImmediately(byte b) throws IOException {
        conn.sendByteImmediately(b);
    }
}