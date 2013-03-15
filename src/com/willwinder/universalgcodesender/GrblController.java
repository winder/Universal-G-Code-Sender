/*
 * Control layer, coordinates all aspects of GRBL control.
 */
/*
    Copywrite 2013 Will Winder

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

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GrblController implements SerialCommunicatorListener {
    private GrblCommunicator comm;

    // Grbl state
    private double grblVersion = 0.0;        // The 0.8 in 'Grbl 0.8c'
    private String grblVersionLetter = null; // The c in 'Grbl 0.8c'
    private Boolean isReady = false;         // Not ready until version is received.
    private Boolean commOpen = false;
    
    // Outside influence
    private double speedOverride = -1;
    
    // Grbl status members.
    private GrblUtils.Capabilities positionMode = null;
    private Boolean realTimeCapable = false;
    private String grblState;
    private Point3d machineLocation;
    private Point3d workLocation;
    
    // Added value
    private Boolean isStreaming = false;
    private Boolean paused = false;
    private long streamStart = 0;
    private long streamStop = 0;
    private File gcodeFile;
    
    // This metadata needs to be cached instead of inferred from queue's because
    // in case of a cancel the queues will be cleared.
    private int numCommands = 0;
    private int numCommandsSent = 0;
    private int numCommandsSkipped = 0;
    private int numCommandsCompleted = 0;
    
    // Polling state
    private int outstandingPolls = 0;
    private Timer positionPollTimer = null;  
    private int pollingRate = 200;
    
    // Structures for organizing all streaming commands.
    private LinkedList<GcodeCommand> commandQueue;          // preparing for send
    private LinkedList<GcodeCommand> outgoingQueue;         // waiting to be sent
    private LinkedList<GcodeCommand> awaitingResponseQueue; // waiting for response
    private LinkedList<GcodeCommand> completedCommandList;  // received response
    private LinkedList<GcodeCommand> errorCommandList;      // error in response
    
    // Listeners
    private ArrayList<ControllerListener> listeners;
    
    // Helper objects
    private GcodeCommandCreator commandCreator;
    
    /**
     * Dependency injection constructor to allow a mock communicator.
     */
    protected GrblController(GrblCommunicator comm) {
        this();
        this.comm = comm;
    }
    
    public GrblController() {
        this.comm = new GrblCommunicator();
        this.comm.setListenAll(this);
        
        this.commandQueue = new LinkedList<GcodeCommand>();
        this.outgoingQueue = new LinkedList<GcodeCommand>();
        this.awaitingResponseQueue = new LinkedList<GcodeCommand>();
        this.completedCommandList = new LinkedList<GcodeCommand>();
        this.errorCommandList = new LinkedList<GcodeCommand>();
        
        this.listeners = new ArrayList<ControllerListener>();
        
        this.commandCreator = new GcodeCommandCreator();
        
        // Action Listener for polling mechanism.
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (outstandingPolls == 0) {
                                outstandingPolls++;
                                comm.sendByteImmediately(GrblUtils.GRBL_STATUS_COMMAND);
                            } else {
                                // If a poll is somehow lost after 20 intervals,
                                // reset for sending another.
                                outstandingPolls++;
                                if (outstandingPolls >= 20) {
                                    outstandingPolls = 0;
                                }
                            }
                        } catch (IOException ex) {
                            messageForConsole("IOException while sending status command: " + ex.getMessage() + "\n");
                        }
                    }
                });
                
            }
        };

        this.positionPollTimer = new Timer(pollingRate, actionListener);
    }
    
    /**
     * Overrides the feed rate in gcode commands. Disable by setting to -1.
     */
    public void setSpeedOverride(double override) {
        this.speedOverride = override;
    }
    
    public double getSpeedOverride() {
        return this.speedOverride;
    }
    
    public String getGrblVersion() {
        if (this.commOpen) {
            StringBuilder str = new StringBuilder();
            str.append("Grbl ");
            if (this.grblVersion > 0.0) {
                str.append(this.grblVersion);
            }
            if (this.grblVersionLetter != null) {
                str.append(this.grblVersionLetter);
            }
            return str.toString();
        }
        return "<not connected>";
    }
    
    public Boolean openCommPort(String port, int portRate) throws Exception {
        if (this.commOpen) {
            throw new Exception("Comm port is already open.");
        }
        
        // Create and setup the new communicator.
        //this.comm = new GrblCommunicator();
        //this.comm.setListenAll(this);
        
        // No point in checking response, it throws an exception on errors.
        this.commOpen = this.comm.openCommPort(port, portRate);
        
        if (this.commOpen) {
            this.messageForConsole(
                   "**** Connected to " + port + " @ " + portRate + " baud ****\n");
        }
        
        return this.commOpen;
    }
    
    public Boolean closeCommPort() {
        // Already closed.
        if (this.commOpen == false) {
            return true;
        }
        
        this.messageForConsole("**** Connection closed ****\n");
        this.stopPollingPosition();
        
        // I was noticing odd behavior, such as continuing to send 'ok's after
        // closing and reopening the comm port.
        // Note: The "Configuring-Grbl-v0.8" documentation recommends frequent
        //       soft resets, but also warns that the "startup" block will run
        //       on a reset and startup blocks may include motion commands.
        //this.issueSoftReset();
        this.flushSendQueues();
        this.commandCreator.resetNum();
        this.comm.closeCommPort();
        //this.comm = null;
        this.commOpen = false;
        this.grblVersion = 0.0;
        this.grblVersionLetter = null;
        return true;
    }
    
    public Boolean isCommOpen() {
        // TODO: Query comm port for this information.
        return this.commOpen;
    }
    
    /**********************************/
    /* GRBL Version Specific Commands */
    /**********************************/
    
    /**
     * Sends the version specific homing cycle to the machine.
     */
    public void performHomingCycle() throws Exception {
        if (this.commOpen) {
            String command = GrblUtils.getHomingCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        throw new Exception("No supported homing method for " + this.getGrblVersion());
    }
    
    public void returnToHome() throws Exception {
        if (this.commOpen) {
            String command = GrblUtils.getReturnToHomeCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        throw new Exception("No supported homing method for " + this.getGrblVersion());
    }
    
    public void killAlarmLock() throws Exception {
        if (this.commOpen) {
            String command = GrblUtils.getKillAlarmLockCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        throw new Exception("No supported kill alarm lock method for " + this.getGrblVersion());
    }

    public void toggleCheckMode() throws Exception {
        if (this.commOpen) {
            String command = GrblUtils.getToggleCheckModeCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        throw new Exception("No supported toggle check mode method for " + this.getGrblVersion());
    }

    public void viewParserState() throws Exception {
        if (this.commOpen) {
            String command = GrblUtils.getViewParserStateCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        throw new Exception("No supported view parser state method for " + this.getGrblVersion());
    }
    
    /**
     * If it is supported, a soft reset real-time command will be issued.
     */
    public void issueSoftReset() throws IOException {
        if (this.commOpen && this.realTimeCapable) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
            //Does GRBL need more time to handle the reset?
            this.comm.softReset();
        }
    }
    
    //// File send metadata ////
    
    public Boolean isStreamingFile() {
        return this.isStreaming;
    }
    
    /**
     * Send duration can be one of 3 things:
     * 1. the current running time of a send.
     * 2. the entire duration of the most recent send.
     * 3. 0 if there has never been a send.
     */
    public long getSendDuration() {
        // Last send duration.
        if (this.isStreaming == false) {
            return this.streamStop - this.streamStart;
        
        }
        // No send duration data available.
        else if (this.streamStart == 0L) {
            return 0L;
        }
        // Current send duration.
        else {
            return System.currentTimeMillis() - this.streamStart;
        }
    }
    
    public int rowsInSend() {
        return this.numCommands;
    }
    
    public int rowsSent() {
        return this.numCommandsSent;
    }
    
    public int rowsRemaining() {
        return this.numCommands - this.numCommandsCompleted - this.numCommandsSkipped;
    }
    
    /**
     * Creates a gcode command and queues it for send immediately.
     * Note: this is the only place where a string is sent to the comm.
     */
    public void queueStringForComm(String str) throws Exception {
        GcodeCommand command = this.commandCreator.createCommand(str);
        this.outgoingQueue.add(command);

        this.commandQueued(command);
        this.sendStringToComm(command.getCommandString());
    }
    
    private void queueCommandForComm(GcodeCommand command) throws Exception {
        this.outgoingQueue.add(command);
        this.commandQueued(command);
        this.sendStringToComm(command.getCommandString());
    }
    
    /**
     * This is the only place where commands with an expected 'ok'/'error'
     * response are sent to the comm.
     */
    private void sendStringToComm(String command) {        
        this.comm.queueStringForComm(command+"\n");
        // Send command to the serial port.
        this.comm.streamCommands();

    }
    
    public Boolean isReadyToStreamFile() throws Exception {
        if (this.commOpen == false) {
            throw new Exception("Cannot begin streaming, comm port is not open.");
        }
        if (this.isReady == false) {
            throw new Exception("Grbl has not finished booting.");
        }
        if (this.awaitingResponseQueue.size() != 0 || this.outgoingQueue.size() != 0) {
            throw new Exception("Cannot stream while there are active commands (controller).");
        }
        if (this.comm.areActiveCommands()) {
            throw new Exception("Cannot stream while there are active commands (communicator).");
        }

        return true;
    }
    
    /**
     * Appends command string to a queue awaiting to be sent.
     */
    public void appendGcodeCommand(String commandString) {
        GcodeCommand command = this.commandCreator.createCommand(commandString);
        this.commandQueue.add(command);
    }
    
    /**
     * Appends file of commands to a queue awaiting to be sent. Exception is
     * thrown on file IO errors.
     */
    public void appendGcodeFile(File file) throws IOException {
        this.gcodeFile = file;
        ArrayList<String> linesInFile = 
                VisualizerUtils.readFiletoArrayList(this.gcodeFile.getAbsolutePath());

        for (String line : linesInFile) {
            this.appendGcodeCommand(line);
        }
    }
    
    /**
     * Send all queued commands to comm port.
     */
    public void beginStreaming() throws Exception {
        this.isReadyToStreamFile();
        
        if (this.commandQueue.size() == 0) {
            throw new Exception("There are no commands queued for streaming.");
        }
        
        // Grbl's "Configuring-Grbl-v0.8" documentation recommends a soft reset
        // prior to starting a job. But will this cause GRBL to reset all the
        // way to reporting version info? Need to double check that before
        // enabling.
        //this.issueSoftReset();
        
        this.isStreaming = true;
        this.streamStop = 0;
        this.streamStart = System.currentTimeMillis();
        this.numCommands = this.commandQueue.size();
        this.numCommandsSent = 0;
        this.numCommandsSkipped = 0;
        this.numCommandsCompleted = 0;

        try {
            // Send all queued commands and wait for a response.
            GcodeCommand command;
            String processed;
            while (this.commandQueue.size() > 0) {
                command = this.commandQueue.remove();
                
                // TODO: Expand this to handle canned cycles (Issue#49)
                processed = this.preprocessCommand(command.getCommandString());

                // Don't send zero length commands.
                if (processed.trim().equals("")) {
                    this.messageForConsole("Skipping command #" + command.getCommandNumber() + "\n");
                    command.setResponse("<skipped by application>");
                    // Need to queue the command first so that listeners don't
                    // see a random command complete without notice.
                    this.commandQueued(command);
                    this.commandComplete(command);
                    // For the listeners...
                    dispatchCommandSent(listeners, command);
                } else {
                    queueCommandForComm(command);
                }
            }
        } catch(Exception e) {
            this.isStreaming = false;
            this.streamStart = 0;
            throw e;
        }
    }
    
    public void pauseStreaming() throws IOException {
        this.messageForConsole("\n**** Pausing file transfer. ****\n\n");
        if (this.realTimeCapable) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
        }
        this.paused = true;
        this.comm.pauseSend();
    }
    
    public void resumeStreaming() throws IOException {
        this.messageForConsole("\n**** Resuming file transfer. ****\n\n");
        if (this.realTimeCapable) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_RESUME_COMMAND);
        }
        this.paused = false;
        this.comm.resumeSend();
    }
    
    public void cancelSend() {
        this.messageForConsole("\n**** Canceling file transfer. ****\n\n");

        // Check if we can get fancy with a soft reset.
        if (this.realTimeCapable == true) {
            // This doesn't seem to work.
            /*
            try {
                if (!this.paused) {
                    this.pauseStreaming();
                }
                this.issueSoftReset();
                this.awaitingResponseQueue.clear();
            } catch (IOException e) {
                // Oh well, was worth a shot.
                System.out.println("Exception while trying to issue a soft reset: " + e.getMessage());
            }
            */
        }
        
        // Don't clear the command queue, there might be a situation where a
        // send is in progress while the next queue is being built. In which
        // case a cancel would only be expected to cancel the current action
        // to make way for the queued commands.
        //this.commandQueue.clear();
        
        this.outgoingQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();

        this.comm.cancelSend();        
    }
    
    /**
     * Begin issuing GRBL status request commands.
     */
    private void beginPollingPosition() {
        if (this.positionPollTimer.isRunning() == false) {
            this.outstandingPolls = 0;
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
    
    private void flushSendQueues() {
        this.commandQueue.clear();
        this.outgoingQueue.clear();
        this.awaitingResponseQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();
    }

    private void printStateOfQueues() {
        System.out.println("command queue size = " + this.commandQueue.size());
        System.out.println("outgoing queue size = " + this.outgoingQueue.size());
        System.out.println("awaiting response queue size = " + this.awaitingResponseQueue.size());
        System.out.println("completed command list size = " + this.completedCommandList.size());
        System.out.println("error command list size = " + this.errorCommandList.size());
        System.out.println("============");
        
    }
    
    // No longer a listener event
    private void commandQueued(GcodeCommand command) {
        dispatchCommandQueued(listeners, command);
    }

    // No longer a listener event
    private void fileStreamComplete(String filename, boolean success) {
        this.messageForConsole("\n**** Finished sending file. ****\n\n");
        this.streamStop = System.currentTimeMillis();
        this.isStreaming = false;
        dispatchStreamComplete(listeners, filename, success);        
    }

    // No longer a listener event
    private String preprocessCommand(String command) {
        String newCommand = command;

        // Remove comments from command.
        newCommand = GrblUtils.removeComment(newCommand);

        // Check for comment if length changed while preprocessing.
        if (command.length() != newCommand.length()) {
            String comment = GrblUtils.parseComment(command);
            dispatchCommandCommment(listeners, GrblUtils.parseComment(command));
        }
        
        // Override feed speed
        if (this.speedOverride > 0) {
            newCommand = CommUtils.overrideSpeed(command, this.speedOverride);
        }

        // Return the post processed command.
        return newCommand;
    }
    
    // No longer a listener event
    private void handlePositionString(String string) {
        if (this.positionMode != null) {
            this.grblState = GrblUtils.getStateFromStatusString(string, this.positionMode);
            this.machineLocation = GrblUtils.getMachinePositionFromStatusString(string, this.positionMode);
            this.workLocation = GrblUtils.getWorkPositionFromStatusString(string, this.positionMode);
         
            dispatchStatusString(listeners, this.grblState, this.machineLocation, this.workLocation);
        }
    }
     
    @Override
    public void commandSent(GcodeCommand command) {
        if (this.isStreamingFile()) {
            this.numCommandsSent++;
        }

        GcodeCommand c = this.outgoingQueue.remove();
        c.setSent(true);
        
        this.awaitingResponseQueue.add(c);
        
        dispatchCommandSent(listeners, c);
    }
    
    @Override
    public void commandComplete(GcodeCommand command) throws Exception {
        GcodeCommand c = command;
        String received = command.getCommandString().trim();
        String expected = "";
        try {
            expected = this.awaitingResponseQueue.peek().getCommandString().trim();
        } catch (NullPointerException e) { }
        
        // If the command wasn't sent, it was skipped and should be ignored
        // from the remaining queues.
        System.out.println("Completed: " + c.getCommandString());
        System.out.println("expected: " + expected);
        if (expected.equals(received)) {
            if (this.awaitingResponseQueue.size() == 0) {
                throw new Exception("Attempting to completing a command that "
                        + "doesn't exist: <" + command.toString() + ">");
            }
            
            c = this.awaitingResponseQueue.remove();
            c.setResponse(command.getResponse());
            this.completedCommandList.add(c);
            
            if (this.isStreamingFile()) {
                this.numCommandsCompleted++;
            }
        } else {
            if (this.isStreamingFile()) {
                this.numCommandsSkipped++;
            }
        }
        
        dispatchCommandComplete(listeners, c);
        
        if (this.isStreamingFile() &&
                this.awaitingResponseQueue.size() == 0 &&
                this.outgoingQueue.size() == 0 &&
                this.commandQueue.size() == 0) {
            String streamName = "queued commands";
            if (this.gcodeFile != null) {
                streamName = this.gcodeFile.getName();
            } 
            
            boolean status = true;
            if (this.rowsRemaining() != 0) {
                status = false;
            }
            
            boolean isSuccess = (this.numCommands == (this.numCommandsSent + this.numCommandsSkipped));
            this.fileStreamComplete(streamName, isSuccess);
        }
    }

    @Override
    public void messageForConsole(String msg) {
        dispatchConsoleMessage(listeners, msg, Boolean.FALSE);
    }
    
    @Override
    public void verboseMessageForConsole(String msg) {
        dispatchConsoleMessage(listeners, msg, Boolean.TRUE);
    }

    @Override
    public void rawResponseListener(String response) {        
        /*
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
        */
        if (GcodeCommand.isOkErrorResponse(response)) {
            this.messageForConsole(response + "\n");
        }
        
        else if (GrblUtils.isGrblVersionString(response)) {
            // Version string goes to console
            this.messageForConsole(response + "\n");
            
            this.grblVersion = GrblUtils.getVersionDouble(response);
            this.grblVersionLetter = GrblUtils.getVersionLetter(response);
            this.isReady = true;
            
            this.realTimeCapable = GrblUtils.isRealTimeCapable(this.grblVersion);
            
            this.positionMode = GrblUtils.getGrblStatusCapabilities(this.grblVersion, this.grblVersionLetter);
            if (this.positionMode != null) {
                // Start sending '?' commands.
                this.beginPollingPosition();
            }
            
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                    "Grbl version = " + this.grblVersion + this.grblVersionLetter);
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG,
                    "Real time mode = " + this.realTimeCapable);
        }
        
        else if (GrblUtils.isGrblStatusString(response)) {
            // Only 1 poll is sent at a time so don't decrement, reset to zero.
            this.outstandingPolls = 0;

            // Status string goes to verbose console
            dispatchConsoleMessage(listeners, response + "\n", true);
            
            this.handlePositionString(response);
        }
        
        else {
            // Display any unhandled messages
            this.messageForConsole(response + "\n");
        }
    }

    /**
     * Listener management.
     */
    void addListener(ControllerListener cl) {
        this.listeners.add(cl);
    }

    //  void statusStringListener(String state, Point3d machineCoord, Point3d workCoord);
    static private void dispatchStatusString(ArrayList<ControllerListener> clList, String state, Point3d machine, Point3d work) {
        if (clList != null) {
            for (ControllerListener c : clList) {
                c.statusStringListener(state, machine, work);
            }
        }
    }
    
    //  void messageForConsole(String msg, Boolean verbose);
    static private void dispatchConsoleMessage(ArrayList<ControllerListener> clList, String message, Boolean verbose) {
        if (clList != null) {
            for (ControllerListener c : clList) {
                c.messageForConsole(message, verbose);
            }
        }
    }
    
    //  void fileStreamComplete(String filename, boolean success);
    static private void dispatchStreamComplete(ArrayList<ControllerListener> clList, String filename, Boolean success) {
        if (clList != null) {
            for (ControllerListener c : clList) {
                c.fileStreamComplete(filename, success);
            }
        }
    }
    
    //  void commandComplete(GcodeCommand command);
    static private void dispatchCommandQueued(ArrayList<ControllerListener> clList, GcodeCommand command) {
        if (clList != null) {
            for (ControllerListener c : clList) {
                c.commandQueued(command);
            }
        }
    }
    
    //  void commandSent(GcodeCommand command);
    static private void dispatchCommandSent(ArrayList<ControllerListener> clList, GcodeCommand command) {
        if (clList != null) {
            for (ControllerListener c : clList) {
                c.commandSent(command);
            }
        }
    }
    
    //  void commandComplete(GcodeCommand command);
    static private void dispatchCommandComplete(ArrayList<ControllerListener> clList, GcodeCommand command) {
        if (clList != null) {
            for (ControllerListener c : clList) {
                c.commandComplete(command);
            }
        }
    }
    
    //  void commandComment(String comment);
    static private void dispatchCommandCommment(ArrayList<ControllerListener> clList, String comment) {
        if (clList != null) {
            for (ControllerListener c : clList) {
                c.commandComment(comment);
            }
        }
    }
}
