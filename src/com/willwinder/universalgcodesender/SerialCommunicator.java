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
import java.util.ArrayList;
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
    private Boolean isReady = false;    // Not ready until version is received.
    
    // Capability flags
    private Boolean realTimeMode = false;
    private Boolean realTimePosition = false;
    private CommUtils.Capabilities positionMode = null;
    
    // General variables
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    private StringBuilder inputBuffer = null;
    private String lineTerminator = "\r\n";
    private Timer positionPollTimer = null;  
    private int pollingRate = 200;
    private Boolean outstandingPolls = false;
    
    // Command streaming variables
    private Boolean sendPaused = false;
    private GcodeCommandBuffer commandBuffer;   // All commands in a file
    private LinkedList<GcodeCommand> activeCommandList;  // Currently running commands

    // File transfer variables.
    // TODO: Finish deleting these
    private boolean fileMode = false;
    private boolean fileModeSending = false;
    private File file = null;

    // Callback interfaces
    ArrayList<SerialCommunicatorListener> fileStreamCompleteListeners;
    ArrayList<SerialCommunicatorListener> commandSentListeners;
    ArrayList<SerialCommunicatorListener> commandCommentListeners;
    ArrayList<SerialCommunicatorListener> commandCompleteListeners;
    ArrayList<SerialCommunicatorListener> commConsoleListeners;
    ArrayList<SerialCommunicatorListener> commVerboseConsoleListeners;
    ArrayList<SerialCommunicatorListener> capabilitiesListeners;
    ArrayList<SerialCommunicatorListener> positionListeners;
    // Only one listener can preprocess a command.
    SerialCommunicatorListener commandPreprocessorListener;
    
    SerialCommunicator() {
        this.fileStreamCompleteListeners = new ArrayList<SerialCommunicatorListener>();
        this.commandSentListeners        = new ArrayList<SerialCommunicatorListener>();
        this.commandCommentListeners     = new ArrayList<SerialCommunicatorListener>();
        this.commandCompleteListeners    = new ArrayList<SerialCommunicatorListener>();
        this.commConsoleListeners        = new ArrayList<SerialCommunicatorListener>();
        this.commVerboseConsoleListeners = new ArrayList<SerialCommunicatorListener>();
        this.capabilitiesListeners       = new ArrayList<SerialCommunicatorListener>();
        this.positionListeners           = new ArrayList<SerialCommunicatorListener>();
        
        // Part of the serial data read event.
        this.inputBuffer = new StringBuilder();
        
        // Action Listener for polling mechanism.
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!outstandingPolls) {
                                sendByteImmediately(CommUtils.GRBL_STATUS_COMMAND);
                                outstandingPolls = true;
                            } else {
                                System.out.println("Outstanding poll...");
                            }
                        } catch (IOException ex) {
                            sendMessageToConsoleListener("IOException while sending status command: " + ex.getMessage() + "\n");
                        }
                    }
                });
                
            }
        };
        
        this.positionPollTimer = new Timer(pollingRate, actionListener);

    }
    
    /** Getters & Setters. */
    void setLineTerminator(String terminator) {
        if (terminator.length() < 1) {
            this.lineTerminator = "\r\n";
        } else {
            this.lineTerminator = terminator;
        }
    }

    // Register for callbacks
    void setListenAll(SerialCommunicatorListener scl) {
        this.addFileStreamCompleteListener(scl);
        this.addCommandSentListener(scl);
        this.addCommandCompleteListener(scl);
        this.addCommandCommentListener(scl);
        this.addCommConsoleListener(scl);
        this.addCommVerboseConsoleListener(scl);
        this.addCapabilitiesListener(scl);
        this.addPositionStringListener(scl);
    }
    
    void addFileStreamCompleteListener(SerialCommunicatorListener scl) {
        this.fileStreamCompleteListeners.add(scl);
    }
    
    void addCommandSentListener(SerialCommunicatorListener scl) {
        this.commandSentListeners.add(scl);
    }
    
    void addCommandCommentListener(SerialCommunicatorListener scl) {
        this.commandCommentListeners.add(scl);
    }
    
    void addCommandCompleteListener(SerialCommunicatorListener scl) {
        this.commandCompleteListeners.add(scl);
    }
    
    void commandPreprocessorListener(SerialCommunicatorListener scl) {
        this.commandPreprocessorListener = scl;
    }
    
    void addCommConsoleListener(SerialCommunicatorListener scl) {
        this.commConsoleListeners.add(scl);
    }

    void addCommVerboseConsoleListener(SerialCommunicatorListener scl) {
        this.commVerboseConsoleListeners.add(scl);
    }
    
    void addCapabilitiesListener(SerialCommunicatorListener scl) {
        this.capabilitiesListeners.add(scl);
        
        // TODO: Stick the capabilities updates in a new function
        if (this.positionMode != null) {
            scl.capabilitiesListener(this.positionMode);
        }
        
        if (this.realTimeMode) {
            scl.capabilitiesListener(CommUtils.Capabilities.REAL_TIME);
        }
    }
    
    void addPositionStringListener(SerialCommunicatorListener scl) {
        this.positionListeners.add(scl);
    }

    
    // Must create /var/lock on OSX, fixed in more current RXTX (supposidly):
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    synchronized boolean openCommPort(String name, int baud) 
            throws NoSuchPortException, PortInUseException, 
            UnsupportedCommOperationException, IOException, 
            TooManyListenersException, Exception {
        
        // TODO: Move command buffer control into the GrblController class.
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

        // Send command to the serial port.
        this.streamCommands();
    }
    
    /**
     * Sends a command to the serial device. This actually streams the bits to
     * the comm port.
     * @param command   Command to be sent to serial device.
     */
    public void sendStringToComm(String command) {
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
        if (isReady == false) {
            throw new Exception("Grbl has not finished booting.");
        }
        if (this.activeCommandList.size() > 0) {
            throw new Exception("Cannot send file until there are no commands running. (Active Command List > 0).");
        }

        // TODO: delete all trace of a 'file mode' concept in this class.
        if (this.fileModeSending == true) {
            throw new Exception("Already sending a file.");
        }
        if ((this.fileMode == false) &&
                (this.commandBuffer.size() > 0) && 
                (this.commandBuffer.currentCommand().isDone() != true)) {
            throw new Exception("Cannot send file until there are no commands running. (Commands remaining in command buffer)");
        }

    }
    
    /**
     * Streams anything in the command buffer to the comm port.
     */
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
                if (processed.length() != command.getCommandString().length()) {
                    String comment = CommUtils.parseComment(command.getCommandString());
                    ListenerUtils.dispatchListenerEvents(ListenerUtils.COMMAND_COMMENT, 
                            this.commandCommentListeners, comment);
                }
        
                command.setCommand(processed);
                
                // If the Controller class hits this case all hell breaks loose.
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
            
            ListenerUtils.dispatchListenerEvents(ListenerUtils.COMMAND_SENT, 
                    this.commandCompleteListeners, command);
            
            // If the command was skipped let the listeners know.
            if (skip) {
                this.sendMessageToConsoleListener("Skipping command #"
                        + command.getCommandNumber() + "\n");
                command.setResponse("<skipped by application>");

                ListenerUtils.dispatchListenerEvents(ListenerUtils.COMMAND_COMPLETE, 
                        this.commandCompleteListeners, command);
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
        this.fileModeSending = false;

        this.sendMessageToConsoleListener("\n**** Finished sending file. ****\n\n");
        // Trigger callback
        ListenerUtils.dispatchListenerEvents(ListenerUtils.FILE_STREAM_COMPLETE, 
                this.fileStreamCompleteListeners, file.getName(), 
                this.commandBuffer.currentCommand().isDone());
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
        //if (this.fileMode || this.fileModeSending) {
        //    this.fileMode = false;
        //    this.fileModeSending = false;
            
        //    this.sendMessageToConsoleListener("\n**** Canceling file transfer. ****\n\n");

            this.commandBuffer.clearBuffer();
            
            // Clear the active command list?
            this.activeCommandList.clear();
        //}
    }


    /** 
     * Processes message from GRBL.
     */
    private void responseMessage( String response ) {
        
        // Check if was 'ok' or 'error'.
        if (GcodeCommand.isOkErrorResponse(response)) {
            
            // All Ok/Error messages go to console
            this.sendMessageToConsoleListener(response + "\n");

            // Pop the front of the active list.
            GcodeCommand command = this.activeCommandList.pop();

            command.setResponse(response);

            ListenerUtils.dispatchListenerEvents(ListenerUtils.COMMAND_COMPLETE, 
                    this.commandCompleteListeners, command);

            if (this.sendPaused == false) {
                this.streamCommands();
            }
        }
        else if (response.contains("ok") || response.contains("error)")) {
            System.out.println("MISSED AN OK OR ERROR: " + response);
        }
        else if (GrblUtils.isGrblVersionString(response)) {
            // Version string goes to console
            this.sendMessageToConsoleListener(response + "\n");
            
            this.grblVersion = GrblUtils.getVersionDouble(response);
            this.grblVersionLetter = GrblUtils.getVersionLetter(response);
            this.isReady = true;
            
            this.realTimeMode = GrblUtils.isRealTimeCapable(this.grblVersion);
            if (this.realTimeMode) {
                ListenerUtils.dispatchListenerEvents(ListenerUtils.CAPABILITY, 
                        this.capabilitiesListeners, 
                        CommUtils.Capabilities.REAL_TIME);
            }
            
            this.positionMode = GrblUtils.getGrblPositionCapabilities(this.grblVersion, this.grblVersionLetter);
            if (this.positionMode != null) {
                this.realTimePosition = true;
                
                // Start sending '?' commands.
                this.beginPollingPosition();
            }
            
            if (this.realTimePosition) {
                ListenerUtils.dispatchListenerEvents(ListenerUtils.CAPABILITY, 
                        this.capabilitiesListeners, 
                        CommUtils.Capabilities.POSITION_C);
            }
            
            System.out.println("Grbl version = " + this.grblVersion + this.grblVersionLetter);
            System.out.println("Real time mode = " + this.realTimeMode);
        }
        
        else if (GrblUtils.isGrblPositionString(response)) {
            this.outstandingPolls = false;

            // Position string goes to verbose console
            this.sendMessageToConsoleListener(response + "\n", true);
            
            ListenerUtils.dispatchListenerEvents(ListenerUtils.POSITION_UPDATE, 
                    this.positionListeners, response);
        }
        
        else {
            // Display any unhandled messages
            this.sendMessageToConsoleListener(response + "\n");
        }
    }
    
    /**
     * Begin issuing GRBL status request commands.
     */
    private void beginPollingPosition() {
        if (this.positionPollTimer.isRunning() == false) {
            this.positionPollTimer.start();
        }
    }

    /**
     * Stop issuing GRBL status request commands.
     */
    private void stopPollingPosition() {
        if (this.positionPollTimer.isRunning()) {
            this.positionPollTimer.stop();
        }
    }
    
    @Override
    /**
     * Reads data from the serial port. RXTX SerialPortEventListener method.
     */
    public void serialEvent(SerialPortEvent arg0) {
        //System.out.println("Serial Event.");
        if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                int availableBytes = in.available();
                if (availableBytes > 0) {
                    byte[] readBuffer = new byte[availableBytes];

                    // Read from serial port
                    in.read(readBuffer, 0, availableBytes);
                    inputBuffer.append(new String(readBuffer, 0, availableBytes));
                                        
                    // Check for line terminator and split out command(s).
                    if (inputBuffer.toString().contains(lineTerminator)) {
                        // Split with the -1 option will give an empty string at
                        // the end if there is a terminator there as well.
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
        }
    }

    // Helper for the console listener.              
    private void sendMessageToConsoleListener(String msg) {
        this.sendMessageToConsoleListener(msg, false);
    }
    
    private void sendMessageToConsoleListener(String msg, boolean verbose) {
        // Exit early if there are no listeners.
        if (this.commConsoleListeners == null) {
            return;
        }
        
        int verbosity;
        if (!verbose) {
            verbosity = ListenerUtils.CONSOLE_MESSAGE;
        }
        else {
            verbosity = ListenerUtils.VERBOSE_CONSOLE_MESSAGE;
        }
        
        ListenerUtils.dispatchListenerEvents(verbosity, this.commConsoleListeners, msg);
    }    
}
