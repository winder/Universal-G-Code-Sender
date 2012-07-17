/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import gnu.io.*;
import java.io.*;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class SerialCommunicator implements SerialPortEventListener{
    private double grblVersion;
    private Boolean realTimeMode = false;
    
    
    // General variables
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    private String lineTerminator = "\r\n";
        
    // File transfer variables.
    private Boolean sendPaused = false;
    private boolean fileMode = false;
    private GcodeCommandBuffer commandBuffer;   // All commands in a file
    private LinkedList<GcodeCommand> activeCommandList;  // Currently running commands

    // Callback interfaces
    SerialCommunicatorListener fileStreamCompleteListener;
    SerialCommunicatorListener commandQueuedListener;
    SerialCommunicatorListener commandSentListener;
    SerialCommunicatorListener commandCompleteListener;
    SerialCommunicatorListener commandPreprocessorListener;
    SerialCommunicatorListener commConsoleListener;

    /** Getters & Setters. */
    void setLineTerminator(String terminator) {
        if (terminator.length() < 1) {
            this.lineTerminator = "\r\n";
        } else {
            this.lineTerminator = terminator;
        }
    }

    // Register for callbacks
    void setFileStreamCompleteListener(SerialCommunicatorListener fscl) {
        this.fileStreamCompleteListener = fscl;
    }
    
    void setCommandQueuedListener(SerialCommunicatorListener fscl) {
        this.commandQueuedListener = fscl;
    }
    
    void setCommandSentListener(SerialCommunicatorListener fscl) {
        this.commandSentListener = fscl;
    }
    
    void setCommandCompleteListener(SerialCommunicatorListener fscl) {
        this.commandCompleteListener = fscl;
    }
    
    void setCommandPreprocessorListener(SerialCommunicatorListener fscl) {
        this.commandPreprocessorListener = fscl;
    }
    
    void setCommConsoleListener(SerialCommunicatorListener fscl) {
        this.commConsoleListener = fscl;
    }

    
    // Must create /var/lock on OSX, fixed in more current RXTX (supposidly):
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    synchronized boolean openCommPort(String name, int baud) throws Exception {
        this.activeCommandList = new LinkedList<GcodeCommand>();

        boolean returnCode;

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(name);
           
        if (portIdentifier.isCurrentlyOwned()) {
            returnCode = false;
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
        
    void closeCommPort() {
        this.cancelSend();

        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(SerialCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.removeEventListener();
        this.commPort.close();
    }
    
    /**
     * Sends a command to the serial device.
     * @param command   Command to be sent to serial device.
     */
    void sendStringToComm(String command) {
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
    void sendByteImmediately(byte b) throws IOException {
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
    
    // Setup for streaming to serial port then launch the first command.
    void streamFileToComm(File file) throws Exception {
        if (this.fileMode == true) {
            throw new Exception("Already sending a file.");
        }
        this.fileMode = true;
        this.activeCommandList.clear();

        // Get command list.
        try {
            this.commandBuffer = new GcodeCommandBuffer(file);
            
            // Loop through and notify command queue listener.
            if (this.commandQueuedListener != null) {
                this.commandBuffer.resetIterator();
                for (int i=0; i < this.commandBuffer.size(); i++) {
                    GcodeCommand next = this.commandBuffer.nextCommand();
                    this.commandQueuedListener.commandQueued(next);
                }
            }
        } catch (Exception e) {
            // On error, wrap up then re-throw exception for GUI to display.
            finishStreamFileToComm();
            throw e;
        }
        
        // Load the first command.
        this.commandBuffer.resetIterator();
        this.commandBuffer.nextCommand();
        this.streamFileCommands();     
    }
    
    void streamFileCommands() {

        // Keep sending commands until the last command is sent, or the
        // character buffer is full.
        while ((this.commandBuffer.currentCommand().isSent() == false) &&
                CommUtils.checkRoomInBuffer(this.activeCommandList, this.commandBuffer.currentCommand())) {

            GcodeCommand command = this.commandBuffer.currentCommand();

            // Allow a command preprocessor listener to preprocess the command.
            if (this.commandPreprocessorListener != null) {
                String processed = this.commandPreprocessorListener.preprocessCommand(command.getCommandString());
                command.setCommand(processed);
            }
            
            command.setSent(true);
            this.activeCommandList.add(command);
            
            // Commands parsed by the buffer list have embedded newlines.
            this.sendStringToComm(command.getCommandString());

            if (this.commandSentListener != null) {
                this.commandCompleteListener.commandSent(command);
            }

            // Load the next command.
            if (this.commandBuffer.hasNext()) {
                this.commandBuffer.nextCommand();
            }
        }

        // If the final response is done... wrap up.
        if (this.commandBuffer.getFinalCommand().isDone()) {
            this.finishStreamFileToComm();
        }            
    }
    
    void finishStreamFileToComm() {
        this.fileMode = false;

        this.sendMessageToConsoleListener("\n**** Finished sending file. ****\n\n");
        // Trigger callback
        if (this.fileStreamCompleteListener != null) {
            boolean success = this.commandBuffer.getFinalCommand().isDone();
            this.fileStreamCompleteListener.fileStreamComplete(this.commandBuffer.getFile().getName(), success);
        }
    }
    
    void pauseSend() throws IOException {
        this.sendMessageToConsoleListener("\n**** Pausing file transfer. ****\n");
        this.sendPaused = true;
        
        if (this.realTimeMode) {
            this.sendByteImmediately(CommUtils.GRBL_PAUSE_COMMAND);
        }
    }
    
    void resumeSend() throws IOException {
        this.sendMessageToConsoleListener("\n**** Resuming file transfer. ****\n");
                
        this.sendPaused = false;
        this.streamFileCommands();
        
        if (this.realTimeMode) {
            this.sendByteImmediately(CommUtils.GRBL_RESUME_COMMAND);
        }
    }
    
    void cancelSend() {
        if (this.fileMode) {
            this.finishStreamFileToComm();
        }
    }

    // Processes a serial response
    void responseMessage( String response ) {
        // If not file mode, send it to the console without processing.
        this.sendMessageToConsoleListener(response + "\n");
        
        // If file mode, parse the output
        // TODO: Make fileMode generic so that manual commands work with the table.
        if (this.fileMode) {
            // Check if was 'ok' or 'error'.
            if (GcodeCommand.isOkErrorResponse(response)) {

                // Pop the front of the active list.
                GcodeCommand command = this.activeCommandList.pop();
                
                command.setResponse(response);

                if (this.commandCompleteListener != null) {
                    this.commandCompleteListener.commandComplete(command);
                }

                if (this.sendPaused == false) {
                    this.streamFileCommands();
                }
            }
        }
        
        if (CommUtils.isGrblVersionString(response)) {
            this.grblVersion = CommUtils.getVersion(response);
            this.realTimeMode = CommUtils.isRealTimeCapable(this.grblVersion);
            System.out.println("Grbl version = " + this.grblVersion);
            System.out.println("Real time mode = " + this.realTimeMode);
        }
    }
    
    @Override
    // Reads data as it is returned by the serial port.
    public void serialEvent(SerialPortEvent arg0) {

        if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                String next;
                
                // Read one or more lines from the comm.
                while ((next = CommUtils.readLineFromCommUntil(in, lineTerminator)).isEmpty() == false) {
                    next = next.replace("\n", "").replace("\r","");
                
                    // Handle response.
                    this.responseMessage(next);
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    // Helper for the console listener.              
    void sendMessageToConsoleListener(String msg) {
        if (this.commConsoleListener != null) {
            this.commConsoleListener.messageForConsole(msg);
        }
    }
}
