/*
 * Serial port interface class. Also coordinates a buffer of Gcode commands.
 */

/*
    Copywrite 2012 Will Winder

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

import gnu.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.LinkedList;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author wwinder
 */
public class SerialCommunicator implements SerialPortEventListener{
    private double grblVersion;         // The 0.8 in 'Grbl 0.8c'
    private String grblVersionLetter;   // The c in 'Grbl 0.8c'
    
    // Capability flags
    private Boolean realTimeMode = false;
    private Boolean realTimePosition = false;
    private CommUtils.Capabilities positionMode = null;
    
    // General variables
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    private String lineTerminator = "\r\n";
    private Timer positionPollTimer = null;  
    
    // File transfer variables.
    private Boolean sendPaused = false;
    private boolean fileMode = false;
    private boolean fileModeSending = false;
    private File file = null;
    private GcodeCommandBuffer commandBuffer;   // All commands in a file
    private LinkedList<GcodeCommand> activeCommandList;  // Currently running commands

    // Callback interfaces
    SerialCommunicatorListener fileStreamCompleteListener;
    SerialCommunicatorListener commandQueuedListener;
    SerialCommunicatorListener commandSentListener;
    SerialCommunicatorListener commandCommentListener;
    SerialCommunicatorListener commandCompleteListener;
    SerialCommunicatorListener commandPreprocessorListener;
    SerialCommunicatorListener commConsoleListener;
    SerialCommunicatorListener commVerboseConsoleListener;
    SerialCommunicatorListener capabilitiesListener;
    SerialCommunicatorListener positionListener;
    
    /** Getters & Setters. */
    void setLineTerminator(String terminator) {
        if (terminator.length() < 1) {
            this.lineTerminator = "\r\n";
        } else {
            this.lineTerminator = terminator;
        }
    }

    // Register for callbacks
    void setListenAll(SerialCommunicatorListener fscl) {
        this.setFileStreamCompleteListener(fscl);
        this.setCommandQueuedListener(fscl);
        this.setCommandSentListener(fscl);
        this.setCommandCompleteListener(fscl);
        this.setCommandCommentListener(fscl);
        this.setCommandPreprocessorListener(fscl);
        this.setCommConsoleListener(fscl);
        this.setCommVerboseConsoleListener(fscl);
        this.setCapabilitiesListener(fscl);
        this.setPositionStringListener(fscl);
    }
    
    void setFileStreamCompleteListener(SerialCommunicatorListener fscl) {
        this.fileStreamCompleteListener = fscl;
    }
    
    void setCommandQueuedListener(SerialCommunicatorListener fscl) {
        this.commandQueuedListener = fscl;
    }
    
    void setCommandSentListener(SerialCommunicatorListener fscl) {
        this.commandSentListener = fscl;
    }
    
    void setCommandCommentListener(SerialCommunicatorListener fscl) {
        this.commandCommentListener = fscl;
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

    void setCommVerboseConsoleListener(SerialCommunicatorListener fscl) {
        this.commVerboseConsoleListener = fscl;
    }
    
    void setCapabilitiesListener(SerialCommunicatorListener fscl) {
        this.capabilitiesListener = fscl;
    }
    
    void setPositionStringListener(SerialCommunicatorListener fscl) {
        this.positionListener = fscl;
    }

    
    // Must create /var/lock on OSX, fixed in more current RXTX (supposidly):
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    synchronized boolean openCommPort(String name, int baud) 
            throws NoSuchPortException, PortInUseException, 
            UnsupportedCommOperationException, IOException, 
            TooManyListenersException, Exception {
        
        this.commandBuffer = new GcodeCommandBuffer();
        this.activeCommandList = new LinkedList<GcodeCommand>();

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

                this.sendMessageToConsoleListener("**** Connected to " 
                                        + name
                                        + " @ "
                                        + baud
                                        + " baud ****\n");

                returnCode = true;
        }

        return returnCode;
    }
        
    void closeCommPort() {
        this.cancelSend();

        this.stopPollingPosition();
        
        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(SerialCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.removeEventListener();
        this.commPort.close();

        this.commPort = null;
        this.sendMessageToConsoleListener("**** Connection closed ****\n");
    }
    
    /**
     * Add command to the command queue outside file mode. This is the only way
     * to send a command to the comm port without being in file mode.
     */
    void queueStringForComm(final String input) throws Exception {
        if (this.fileMode) {
            throw new Exception("Cannot add commands while in file mode.");
        }
        
        String commandString = input;
        
        if (! commandString.endsWith("\n")) {
            commandString += "\n";
        }
        
        // Add command to queue
        GcodeCommand command = this.commandBuffer.appendCommandString(commandString);

        if (this.commandQueuedListener != null) {
            this.commandQueuedListener.commandQueued(command);
        }

        // Send command to the serial port.
        this.streamCommands();
    }
    
    /**
     * Sends a command to the serial device. This actually streams the bits to
     * the comm port.
     * @param command   Command to be sent to serial device.
     */
    private void sendStringToComm(String command) {
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
    private void sendByteImmediately(byte b) throws IOException {
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
    
    void isReadyToStreamFile() throws Exception {
        if (this.fileModeSending == true) {
            throw new Exception("Already sending a file.");
        }
        if ((this.fileMode == false) &&
                (this.commandBuffer.size() > 0) && 
                (this.commandBuffer.currentCommand().isDone() != true)) {
            throw new Exception("Cannot send file until there are no commands running. (Commands remaining in command buffer)");
        }
        if (this.activeCommandList.size() > 0) {
            throw new Exception("Cannot send file until there are no commands running. (Active Command List > 0).");
        }

    }
    
    void appendGcodeCommand(String input) throws Exception {
        isReadyToStreamFile();

        // Changing to file mode.
        this.fileMode = true;
        
        String commandString = input;
        
        if (! commandString.endsWith("\n")) {
            commandString += "\n";
        }
        
        GcodeCommand command;
        command = this.commandBuffer.appendCommandString(commandString);
        
        // Notify listener of new command
        if (this.commandQueuedListener != null) {
            this.commandQueuedListener.commandQueued(command);
        }
    }
    
    void appendGcodeFile(File commandfile) throws Exception {
        isReadyToStreamFile();

        // Changing to file mode.
        this.fileMode = true;
        
        // Get command list.
        try {
            this.file = commandfile;
            
            FileInputStream fstream = new FileInputStream(this.file);
            DataInputStream dis = new DataInputStream(fstream);
            BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));

            String line;
            GcodeCommand command;
            while ((line = fileStream.readLine()) != null) {
                // Add command to queue
                command = this.commandBuffer.appendCommandString(line + '\n');
                
                // Notify listener of new command
                if (this.commandQueuedListener != null) {
                    this.commandQueuedListener.commandQueued(command);
                }
            }
        } catch (Exception e) {
            // On error, wrap up then re-throw exception for GUI to display.
            finishStreamFileToComm();
            throw e;
        }
    }
    
    // Setup for streaming to serial port then launch the first command.
    void streamToComm() throws Exception {
        isReadyToStreamFile();

        // Changing to file mode.
        this.fileMode = true;

        // Now that we are setup for file mode, enable and begin streaming.
        this.fileModeSending = true;
        
        // Start sending commands.
        this.streamCommands();     
    }
    
    private void streamCommands() {
        boolean skip;
        
        // The GcodeCommandBuffer class always preloads the next command, so as
        // long as the currentCommand exists and hasn't been sent it is the next
        // which should be sent.
        
        while ((this.commandBuffer.currentCommand().isSent() == false) &&
                CommUtils.checkRoomInBuffer(this.activeCommandList, this.commandBuffer.currentCommand())) {

            skip = false;
            GcodeCommand command = this.commandBuffer.currentCommand();

            // Allow a command preprocessor listener to preprocess the command.
            if (this.commandPreprocessorListener != null) {
                String processed = this.commandPreprocessorListener.preprocessCommand(command.getCommandString());
                
                // If the lengths differ, update the latest comment.
                if (this.commandCommentListener != null) {
                    if (processed.length() != command.getCommandString().length()) {
                        this.commandCommentListener.commandComment(CommUtils.parseComment(command.getCommandString()));
                    }
                }
        
                command.setCommand(processed);
                
                if (processed.trim().equals("")) {
                    skip = true;
                }
            }
            
            // Don't send skipped commands.
            if (!skip) {
                command.setSent(true);

                this.activeCommandList.add(command);
            
                // Commands parsed by the buffer list have embedded newlines.
                this.sendStringToComm(command.getCommandString());
            }
            
            if (this.commandSentListener != null) {
                this.commandCompleteListener.commandSent(command);
            }
            
            // If the command was skipped let the listeners know.
            if (skip) {
                this.sendMessageToConsoleListener("Skipping command #"
                        + command.getCommandNumber() + "\n");
                command.setResponse("<skipped by application>");

                if (this.commandCompleteListener != null) {
                    this.commandCompleteListener.commandComplete(command);
                }
            }

            // Load the next command.
            this.commandBuffer.nextCommand();
        }

        // If the final response is done and we're in file mode then wrap up.
        if (this.fileMode && (this.commandBuffer.size() == 0) &&
                this.commandBuffer.currentCommand().isDone() ) {
            this.finishStreamFileToComm();
        }            
    }
    
    private void finishStreamFileToComm() {
        this.fileMode = false;

        this.sendMessageToConsoleListener("\n**** Finished sending file. ****\n\n");
        // Trigger callback
        if (this.fileStreamCompleteListener != null) {
            boolean success = this.commandBuffer.currentCommand().isDone();
            this.fileStreamCompleteListener.fileStreamComplete(file.getName(), success);
        }
    }
    
    void pauseSend() throws IOException {
        this.sendMessageToConsoleListener("\n**** Pausing file transfer. ****\n\n");
        this.sendPaused = true;
        
        if (this.realTimeMode) {
            this.sendByteImmediately(CommUtils.GRBL_PAUSE_COMMAND);
        }
    }
    
    void resumeSend() throws IOException {
        this.sendMessageToConsoleListener("\n**** Resuming file transfer. ****\n\n");
                
        this.sendPaused = false;
        this.streamCommands();
        
        if (this.realTimeMode) {
            this.sendByteImmediately(CommUtils.GRBL_RESUME_COMMAND);
        }
    }
    
    void cancelSend() {
        if (this.fileMode || this.fileModeSending) {
            this.fileMode = false;
            this.fileModeSending = false;
            
            this.sendMessageToConsoleListener("\n**** Canceling file transfer. ****\n\n");

            this.commandBuffer.clearBuffer();
            
            // Clear the active command list?
            this.activeCommandList.clear();
            
            // Canceling the remaining commands rather than just clearing the
            // buffer would be nice, but it is too slow.
            /*
            this.sendPaused = true;

             GcodeCommand command;
 
            // Cancel the current command if it isn't too late.
            command = this.commandBuffer.currentCommand();
            if (command.isSent() == false) {
                command.setResponse("<canceled by application>");                
                if (this.commandCompleteListener != null) {
                    this.commandCompleteListener.commandComplete(command);
                }
            }
            
            // Cancel the rest.
            while (this.commandBuffer.hasNext()) {
                command = this.commandBuffer.nextCommand();
                command.setResponse("<canceled by application>");
                
                if (this.commandCompleteListener != null) {
                    this.commandCompleteListener.commandComplete(command);
                }
            }
            
            this.sendPaused = false;
            
            */
            
            //this.finishStreamFileToComm();
        }
    }

    // Processes a serial response
    private void responseMessage( String response ) {

        // Check if was 'ok' or 'error'.
        if (GcodeCommand.isOkErrorResponse(response)) {
            // All Ok/Error messages go to console
            this.sendMessageToConsoleListener(response + "\n");

            // Pop the front of the active list.
            GcodeCommand command = this.activeCommandList.pop();

            command.setResponse(response);

            if (this.commandCompleteListener != null) {
                this.commandCompleteListener.commandComplete(command);
            }

            if (this.sendPaused == false) {
                this.streamCommands();
            }
        }
        
        else if (GrblUtils.isGrblVersionString(response)) {
            // Version string goes to console
            this.sendMessageToConsoleListener(response + "\n");
            
            this.grblVersion = GrblUtils.getVersionDouble(response);
            this.grblVersionLetter = GrblUtils.getVersionLetter(response);
            
            this.realTimeMode = GrblUtils.isRealTimeCapable(this.grblVersion);
            if (this.realTimeMode) {
                if (this.capabilitiesListener != null) {
                    this.capabilitiesListener.capabilitiesListener(CommUtils.Capabilities.REAL_TIME);
                }
            }
            
            this.positionMode = GrblUtils.getGrblPositionCapabilities(this.grblVersion, this.grblVersionLetter);
            if (this.positionMode != null) {
                this.realTimePosition = true;
                
                // Start sending '?' commands.
                this.beginPollingPosition();
            }
            
            if (this.realTimePosition) {
                if (this.capabilitiesListener != null) {
                    this.capabilitiesListener.capabilitiesListener(CommUtils.Capabilities.POSITION_C);
                }
            }
            
            System.out.println("Grbl version = " + this.grblVersion + this.grblVersionLetter);
            System.out.println("Real time mode = " + this.realTimeMode);
        }
        
        else if (GrblUtils.isGrblPositionString(response)) {
            // Position string goes to verbose console
            this.sendMessageToConsoleListener(response + "\n", true);
            
            if (this.positionListener != null) {
                this.positionListener.positionStringListener(response);
            }
        }
        
        else {
            // Display any unhandled messages
            this.sendMessageToConsoleListener(response + "\n");
        }
    }
    
    private void beginPollingPosition() {
        System.out.println("BEGIN POLLING POSITION");
        
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendByteImmediately(CommUtils.GRBL_STATUS_COMMAND);
                        } catch (IOException ex) {
                            sendMessageToConsoleListener("IOException while sending status command: " + ex.getMessage() + "\n");
                        }
                    }
                });
                
            }
        };
        
        this.positionPollTimer = new Timer(1000, actionListener);
        this.positionPollTimer.start();
    }

    private void stopPollingPosition() {
        if (this.positionPollTimer != null)
            this.positionPollTimer.stop();
    }
    
    @Override
    // Reads data as it is returned by the serial port.
    public void serialEvent(SerialPortEvent arg0) {

        if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                String next = CommUtils.readLineFromCommUntil(in, lineTerminator);
                next = next.replace("\n", "").replace("\r","");
                
                // Handle response.
                this.responseMessage(next);
                    
                /*
                 * For some reason calling readLineFromCommUntil more than once
                 * prevents the close command from working later on.
                // Read one or more lines from the comm.
                while ((next = CommUtils.readLineFromCommUntil(in, lineTerminator)).isEmpty() == false) {
                    next = next.replace("\n", "").replace("\r","");
                
                    // Handle response.
                    this.responseMessage(next);
                }
                */
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    // Helper for the console listener.              
    private void sendMessageToConsoleListener(String msg) {
        this.sendMessageToConsoleListener(msg, false);
    }
    
    private void sendMessageToConsoleListener(String msg, boolean verbose) {
        if (!verbose && this.commConsoleListener != null) {
            this.commConsoleListener.messageForConsole(msg);
        }
        else if (verbose && this.commVerboseConsoleListener != null) {
            this.commVerboseConsoleListener.verboseMessageForConsole(msg);
        }
    }
}
