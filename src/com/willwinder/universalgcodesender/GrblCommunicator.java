/*
 * Serial port interface class. Also coordinates a buffer of Gcode commands.
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
import gnu.io.*;
import java.io.*;
import java.util.LinkedList;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class GrblCommunicator extends AbstractCommunicator 
implements SerialPortEventListener{    
    
    private static Logger logger = Logger.getLogger(GrblCommunicator.class.getName());
    
    // General variables
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    private StringBuilder inputBuffer = null;
    
    // Command streaming variables
    private Boolean sendPaused = false;
    private GcodeCommandBuffer commandBuffer;   // All commands in a file
    private LinkedList<GcodeCommand> activeCommandList;  // Currently running commands
    private boolean isTinygMode;

    public GrblCommunicator() {
        this.setLineTerminator("\r\n");
    }
    
    /**
     * This constructor is for dependency injection so a mock serial device can
     * act as GRBL.
     */
    protected GrblCommunicator(final InputStream in, final OutputStream out,
            GcodeCommandBuffer gcb, LinkedList<GcodeCommand> acl) {
        // Base constructor.
        this();
        
        this.in = in;
        this.out = out;
        this.commandBuffer = gcb;
        this.activeCommandList = acl;
    }
    // Added to support TinyG mode where no response comes from the controller on 
    // each send.
    public boolean openCommPort(String name, int baud, boolean isTinygMode) 
            throws NoSuchPortException, PortInUseException, 
            UnsupportedCommOperationException, IOException, 
            TooManyListenersException, Exception {
        logger.info("Going into tinyg mode");
        this.isTinygMode = isTinygMode;
        return openCommPort(name, baud);
    }
    // Must create /var/lock on OSX, fixed in more current RXTX (supposidly):
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    @Override
    synchronized public boolean openCommPort(String name, int baud) 
            throws NoSuchPortException, PortInUseException, 
            UnsupportedCommOperationException, IOException, 
            TooManyListenersException, Exception {
        
        // TODO: Move command buffer control into the GrblController class.
        this.commandBuffer = new GcodeCommandBuffer();
        this.activeCommandList = new LinkedList<GcodeCommand>();
        this.inputBuffer = new StringBuilder();
        
        boolean returnCode;

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(name);
           
        if (portIdentifier.isCurrentlyOwned()) {
            throw new Exception("This port is already owned by another process.");
        } else {
            this.commPort = portIdentifier.open(this.getClass().getName(), 2000);

            SerialPort serialPort = (SerialPort) this.commPort;
            serialPort.setSerialPortParams(baud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

            this.in = serialPort.getInputStream();
            this.out = serialPort.getOutputStream();

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);  
            serialPort.notifyOnBreakInterrupt(true);

            returnCode = true;
        }

        return returnCode;
    }
        
    @Override
    public void closeCommPort() {
        // Stop listening before anything, we're done here.
        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.removeEventListener();

        this.cancelSend();
        
        try {
            in.close();
            out.close();
            in = null;
            out = null;
        } catch (IOException ex) {
            Logger.getLogger(GrblCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.inputBuffer = null;
        this.sendPaused = false;
        this.commandBuffer = null;
        this.activeCommandList = null;
        
        this.commPort.close();

        this.commPort = null;

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
        GcodeCommand command = this.commandBuffer.appendCommandString(commandString, this.isTinygMode);
    }
    
    /**
     * Sends a command to the serial device. This actually streams the bits to
     * the comm port.
     * @param command   Command to be sent to serial device.
     */
    private void sendStringToComm(String command) {
        
        logger.info("sendStringToComm. command: " + command);
        
        // Command already has a newline attached.
        this.sendMessageToConsoleListener(">>> " + command);
        
        // Send command to the serial port.
        PrintStream printStream = new PrintStream(this.out);
        printStream.print(command);
        printStream.close(); 
        
    }
    
    /**
     * Immediately sends a byte, used for real-time commands.
     */
    @Override
    public void sendByteImmediately(byte b) throws IOException {
        out.write(b);
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
        return (this.activeCommandList.size() > 0);
    }
    
    /**
     * Streams anything in the command buffer to the comm port.
     */
    @Override
    public void streamCommands() {
        
        logger.info("We are streaming to the comm port");
        
        if (this.commandBuffer.currentCommand() == null) {
            // NO-OP
            return;
        }
        
        if (this.sendPaused) {
            // Another NO-OP
            return;
        }
        
        // The GcodeCommandBuffer class always preloads the next command, so as
        // long as the currentCommand exists and hasn't been sent it is the next
        // which should be sent.
        
        while ((this.commandBuffer.currentCommand().isSent() == false) &&
                CommUtils.checkRoomInBuffer(this.activeCommandList, this.commandBuffer.currentCommand())) {
            GcodeCommand command = this.commandBuffer.currentCommand();
            
            command.setSent(true);
            this.activeCommandList.add(command);

            // Newlines are embedded when they get queued.
            this.sendStringToComm(command.getCommandString());
            
            dispatchListenerEvents(COMMAND_SENT, this.commandSentListeners, command);

            // Load the next command.
            this.commandBuffer.nextCommand();
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
        this.commandBuffer.clearBuffer();
    }
    
    /**
     * This is to allow the GRBL Ctrl-C soft reset command.
     */
    @Override
    public void softReset() {
        this.commandBuffer.clearBuffer();
        this.activeCommandList.clear();
    }

    /** 
     * Processes message from GRBL.
     */
    private void responseMessage( String response ) {
        
        logger.info("Inside responseMessage. response is: " + response);
        
        // GrblCommunicator no longer knows what to do with responses.
        dispatchListenerEvents(RAW_RESPONSE, this.commRawResponseListener, response);

        // Keep the data flow going for now.
        if (GcodeCommand.isOkErrorResponse(response, this.isTinygMode)) {
            // Pop the front of the active list.
            GcodeCommand command = this.activeCommandList.pop();

            command.setResponse(response);

            dispatchListenerEvents(COMMAND_COMPLETE, this.commandCompleteListeners, command);

            if (this.sendPaused == false) {
                this.streamCommands();
            }
        }
    }
    
    /**
     * Reads data from the serial port. RXTX SerialPortEventListener method.
     */
    @Override
    public void serialEvent(SerialPortEvent evt) {
        
        logger.info("Got serialEvent");
        
        if (inputBuffer == null) {
            inputBuffer = new StringBuilder();
        }
        
        // Check for evt == null to allow faking a call to this event.
        if (evt == null || evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                int availableBytes = in.available();
                if (availableBytes > 0) {
                    byte[] readBuffer = new byte[availableBytes];

                    // Read from serial port
                    in.read(readBuffer, 0, availableBytes);
                    inputBuffer.append(new String(readBuffer, 0, availableBytes));
                                        
                    // Check for line terminator and split out command(s).
                    String lineTerminator = this.getLineTerminator(this.isTinygMode);
                    //if (this.isTinygMode) lineTerminator = "tinyg [mm] ok>";
                    
                    if (inputBuffer.toString().contains(lineTerminator)) {
                        // Split with the -1 option will give an empty string at
                        // the end if there is a terminator there as well.
                        //String []commands = inputBuffer.toString().split(getLineTerminator(), -1);
                        String []commands = inputBuffer.toString().split(lineTerminator, -1);

                        for (int i=0; i < commands.length; i++) {
                            if ((i+1) < commands.length) {
                                
                                this.responseMessage(commands[i]);
                            } else {
                                
                                inputBuffer = new StringBuilder().append(commands[i]);
                            }
                        }
                    }
                }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
            
            logger.info("inputBuffer is: " + inputBuffer);
            /*if (this.isTinygMode) {
                // Now call responseMessage once
                this.responseMessage(inputBuffer.toString());
                inputBuffer = new StringBuilder();
            }*/
        } else {
            logger.info("didn't have a real event???");
        }
    }
}
