/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import gnu.io.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class SerialCommunicator implements SerialPortEventListener{
    
    // General variables
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    StringBuffer commandStream;
    private SerialWriter serialWriter;
    private Thread serialWriterThread;
    private boolean fileMode = false;
    private String lineTerminator = "\r\n";
    
    private GcodeCommandBuffer commandBuffer;   // All commands in a file
    private LinkedList<GcodeCommand> activeCommandList;  // Currently running commands
    
    // File transfer variables.
    private Boolean sendPaused = false;
    private Integer numResponses;
    
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
        this.commandStream = new StringBuffer();
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

                // Launch the writer thread.
                this.serialWriter= new SerialWriter(out, this.commandStream);
                this.serialWriterThread= new Thread(this.serialWriter);
                this.serialWriterThread.start();
                
                returnCode = true;
        }

        return returnCode;
    }
        
    void closeCommPort() {
        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.removeEventListener();
        this.cancelSend();
        this.commPort.close();
    }
    
    // Puts a command in the command buffer, the SerialWriter class should pick
    // it up and send it to the serial device.
    void sendStringToComm(String command) {
        // Command has a newline attached.
        this.sendMessageToConsoleListener(">>> " + command);
        this.commandStream.append(command);
        synchronized (this.serialWriterThread) {
            this.serialWriterThread.notifyAll();
        }
    }
       
    // TODO: Figure out why this isn't working ...
    boolean isCommPortOpen() throws NoSuchPortException {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.commPort.getName());
            String owner = portIdentifier.getCurrentOwner();
            String thisClass = this.getClass().getName();
            
            return portIdentifier.isCurrentlyOwned() && owner.equals(thisClass);                    
    }
      
    
    /** File Stream Methods. **/
    
    // Setup for streaming to serial port then launch the first command.
    void streamFileToComm(File file) throws Exception {
        this.fileMode = true;
        
        this.numResponses = 0;
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

        // Keep sending commands until there are no more, or the character
        // buffer is full.
        while (this.commandBuffer.hasNext() &&
                checkRoomInBuffer(this.activeCommandList, this.commandBuffer.currentCommand())) {

            GcodeCommand command = this.commandBuffer.currentCommand();

            // Allow a command preprocessor listener to preprocess the command.
            if (this.commandPreprocessorListener != null) {
                String processed = this.commandPreprocessorListener.preprocessCommand(command.getCommandString());
                command.setCommand(processed);
            }
            
            command.setSent(true);
            this.activeCommandList.add(command);
            this.sendStringToComm(command.getCommandString() + '\n');

            if (this.commandSentListener != null) {
                this.commandCompleteListener.commandSent(command);
            }

            // Load the next command.
            this.commandBuffer.nextCommand();
        }
        
        // If we've received as many responses as we expect... wrap up.
        if (this.commandBuffer.size() == this.numResponses) {
            this.finishStreamFileToComm();
        }            
    }
    
    void finishStreamFileToComm() {
        this.fileMode = false;

        this.sendMessageToConsoleListener("\n**** Finished sending file. ****\n\n");
        // Trigger callback
        if (this.fileStreamCompleteListener != null) {
            boolean success = (this.commandBuffer.size() == this.numResponses);
            this.fileStreamCompleteListener.fileStreamComplete(this.commandBuffer.getFile().getName(), success);
        }
    }
    
    void pauseSend() {
        this.sendMessageToConsoleListener("\n**** Pausing file transfer. ****\n");
        this.sendPaused = true;
    }
    
    void resumeSend() {
        this.sendMessageToConsoleListener("\n**** Resuming file transfer. ****\n");
                
        this.sendPaused = false;
        this.streamFileCommands();
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
        if (this.fileMode) {            
            // Check if was 'ok' or 'error'.
            if (GcodeCommand.isOkErrorResponse(response)) {

                // Pop the front of the active list.
                GcodeCommand command = this.activeCommandList.pop();

// TODO: This is a test to make sure the objects are equal
//       DELETE IT AFTER TESTING
GcodeCommand test = this.commandBuffer.test(this.numResponses);
System.out.println("*******\n command and test should be equal. (command == test) ==" + (command == test)+"\n******");
                
                command.setResponse(response);
                this.numResponses = this.numResponses + 1;

                if (this.commandCompleteListener != null) {
                    this.commandCompleteListener.commandComplete(command);
                }

                if (this.sendPaused == false) {
                    this.streamFileCommands();
                }
            }
        }
    }
    
    @Override
    // Reads data as it is returned by the serial port.
    public void serialEvent(SerialPortEvent arg0) {
        int data;
        StringBuilder buffer = new StringBuilder();
        
        if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                int len = 0;
                int terminatorPosition = 0;
                while ( ( data = in.read()) > -1 )
                {
                    // My funky way of checking for the terminating characters..
                    if ( data == lineTerminator.charAt(terminatorPosition) ) {
                        terminatorPosition++;
                    } else {
                        terminatorPosition = 0;
                    }
                    
                    if (terminatorPosition == lineTerminator.length()) {
                        break;
                    }
                    
                    buffer.append((char)data);
                }

                // Strip off that terminator.
                String output = buffer.toString();
                buffer.setLength(0);

                output = output.replace("\n", "").replace("\r","");
                
                // File mode has a stricter handling on data to GUI.
                this.responseMessage(output);
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
    
        
    // TODO: These could be a static helper functions in another class.
    private static Boolean checkRoomInBuffer(List<GcodeCommand> list, GcodeCommand nextCommand) {
        String command = nextCommand.getCommandString();
        int charInBuffer = numberOfCharacters(list);
        charInBuffer += command.length();
        return charInBuffer < CommPortUtils.GRBL_RX_BUFFER_SIZE;
    }
    
    private static int numberOfCharacters(List<GcodeCommand> arr) {
        Iterator<GcodeCommand> iter = arr.iterator();
        int characters = 0;
        while (iter.hasNext()) {
            String next = (iter.next().toString());
            characters += next.length();
        }
        return characters;
    }
}