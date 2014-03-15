/*
 * Abstract Control layer, coordinates all aspects of control.
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

import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.PointSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public abstract class AbstractController implements SerialCommunicatorListener {
    /** API Interface. */
    
    /**
     * Called before and after comm shutdown allowing device specific behavior.
     */
    abstract protected void closeCommBeforeEvent();
    abstract protected void closeCommAfterEvent();
    
    /**
     * Called after comm opening allowing device specific behavior.
     * @throws IOException 
     */
    protected void openCommAfterEvent() throws IOException {
    	// Empty default implementation. 
    }
    
    /**
     * Called before and after a send cancel allowing device specific behavior.
     */
    abstract protected void cancelSendBeforeEvent();
    abstract protected void cancelSendAfterEvent();
    
    /**
     * Called before the comm is paused and before it is resumed. 
     */
    abstract protected void pauseStreamingEvent() throws IOException;
    abstract protected void resumeStreamingEvent() throws IOException;
    
    /**
     * Called prior to streaming commands, throw an exception if not ready.
     */
    abstract protected void isReadyToStreamFileEvent() throws Exception;

    /**
     * Raw responses from the serial communicator.
     */
    abstract protected void rawResponseHandler(String response);
    
    /**
     * Performs homing cycle, throw an exception if not supported.
     */
    public void performHomingCycle() throws Exception {
        throw new Exception(Localization.getString("controller.exception.homing"));
    }
    
    /**
     * Returns machine to home location, throw an exception if not supported.
     */
    public void returnToHome() throws Exception {
        throw new Exception(Localization.getString("controller.exception.gohome"));
    }
        
    /**
     * Reset machine coordinates to zero at the current location.
     */
    public void resetCoordinatesToZero() throws Exception {
        throw new Exception(Localization.getString("controller.exception.reset"));
    }
    
    /**
     * Disable alarm mode and put device into idle state, throw an exception 
     * if not supported.
     */
    public void killAlarmLock() throws Exception {
        throw new Exception(Localization.getString("controller.exception.killalarm"));
    }
    
    /**
     * Toggles check mode on or off, throw an exception if not supported.
     */
    public void toggleCheckMode() throws Exception {
        throw new Exception(Localization.getString("controller.exception.checkmode"));
    }
    
    /**
     * Request parser state, either print it here or expect it in the response
     * handler. Throw an exception if not supported.
     */
    public void viewParserState() throws Exception {
        throw new Exception(Localization.getString("controller.exception.parserstate"));
    }
    
    /**
     * Execute a soft reset, throw an exception if not supported.
     */
    public void issueSoftReset() throws Exception {
        flushSendQueues();
        softReset();
    }

    protected void softReset() throws Exception {
        throw new Exception(Localization.getString("controller.exception.softreset"));
    }
    
    /**
     * Listener event for status update values;
     */
    abstract protected void statusUpdatesEnabledValueChanged(boolean enabled);
    abstract protected void statusUpdatesRateValueChanged(int rate);
    
    // These abstract objects are initialized in concrete class.
    protected AbstractCommunicator comm;
    protected GcodeCommandCreator commandCreator;
    
    // Outside influence
    private boolean statusUpdatesEnabled = true;
    private int statusUpdateRate = 200;
    
    // State
    private Boolean commOpen = false;
    private Boolean saveToFileMode = false;
    private GcodeParser gcp;
    
    // Parser state
    private Boolean absoluteMode = true;
    
    // Added value
    private Boolean isStreaming = false;
    private Boolean paused = false;
    private long streamStart = 0;
    private long streamStop = 0;
    private PrintWriter outputFileWriter;
    private File gcodeFile;
    
    // This metadata needs to be cached instead of inferred from queue's because
    // in case of a cancel the queues will be cleared.
    private int numCommandsProcessed = 0;
    private int numCommandsStreamed = 0;
    private int numCommandsSent = 0;
    private int numCommandsSkipped = 0;
    private int numCommandsCompleted = 0;
    
    // Structures for organizing all streaming commands.
    private LinkedList<GcodeCommand> prepQueue;             // preparing for send
    private LinkedList<GcodeCommand> outgoingQueue;         // waiting to be sent
    private LinkedList<GcodeCommand> awaitingResponseQueue; // waiting for response
    private LinkedList<GcodeCommand> completedCommandList;  // received response
    private LinkedList<GcodeCommand> errorCommandList;      // error in response
    
    // Listeners
    private ArrayList<ControllerListener> listeners;
        
    /**
     * Dependency injection constructor to allow a mock communicator.
     */
    protected AbstractController(AbstractCommunicator comm) {
        this.comm = comm;
        this.comm.setListenAll(this);
        
        this.gcp = new GcodeParser();

        this.prepQueue = new LinkedList<GcodeCommand>();
        this.outgoingQueue = new LinkedList<GcodeCommand>();
        this.awaitingResponseQueue = new LinkedList<GcodeCommand>();
        this.completedCommandList = new LinkedList<GcodeCommand>();
        this.errorCommandList = new LinkedList<GcodeCommand>();
        
        this.listeners = new ArrayList<ControllerListener>();
    }
    
    @Deprecated public AbstractController() {
        this(new GrblCommunicator()); //f4grx: connection created at opencomm() time
    }
    
    /**
     * Overrides the feed rate in gcode commands. Disable by setting to -1.
     */
    public void setSpeedOverride(double override) {
        this.gcp.setSpeedOverride(override);
    }
    
    public double getSpeedOverride() {
        return this.gcp.getSpeedOverride();
    }

    public void setMaxCommandLength(int length) {
        this.commandCreator.setMaxCommandLength(length);
    }
    
    public int getMaxCommandLength() {
        return this.commandCreator.getMaxCommandLength();
    }
    
    public void setTruncateDecimalLength(int length) {
        this.gcp.setTruncateDecimalLength(length);
    }
    
    public void setSingleStepMode(boolean enabled) {
        this.comm.setSingleStepMode(enabled);
    }

    public boolean getSingleStepMode() {
        return this.comm.getSingleStepMode();
    }
    
    public void setRemoveAllWhitespace(boolean enabled) {
        this.gcp.setRemoveAllWhitespace(enabled);
    }

    public boolean getRemoveAllWhitespace() {
        return this.gcp.getRemoveAllWhitespace();
    }

    public void setConvertArcsToLines(boolean enabled) {
        this.gcp.setConvertArcsToLines(enabled);
    }

    public boolean getConvertArcsToLines() {
        return this.gcp.getConvertArcsToLines();
    }
    
    public void setSmallArcThreshold(double length) {
        this.gcp.setSmallArcThreshold(length);
    }
    
    public double getSmallArcThreshold() {
        return this.gcp.getSmallArcThreshold();
    }
    
    public void setSmallArcSegmentLength(double length) {
        this.gcp.setSmallArcSegmentLength(length);
    }
    
    public double getSmallArcSegmentLength() {
        return this.gcp.getSmallArcSegmentLength();
    }
    
    public void setArcLineLength(double length) {
        this.gcp.setSmallArcSegmentLength(length);
    }
    
    public double getArcLineLength() {
        return this.gcp.getSmallArcSegmentLength();
    }
    
    public void setStatusUpdatesEnabled(boolean enabled) {
        if (this.statusUpdatesEnabled != enabled) {
            this.statusUpdatesEnabled = enabled;
            statusUpdatesEnabledValueChanged(enabled);
        }
    }
    
    public boolean getStatusUpdatesEnabled() {
        return this.statusUpdatesEnabled;
    }
    
    public void setStatusUpdateRate(int rate) {
        if (this.statusUpdateRate != rate) {
            this.statusUpdateRate = rate;
            statusUpdatesRateValueChanged(rate);
        }
    }
    
    public int getStatusUpdateRate() {
        return this.statusUpdateRate;
    }
    
    public Boolean openCommPort(String port, int portRate) throws Exception {
        if (this.commOpen) {
            throw new Exception("Comm port is already open.");
        }
        
        // No point in checking response, it throws an exception on errors.
        this.commOpen = this.comm.openCommPort(port, portRate);
        
        if (this.commOpen) {
            this.openCommAfterEvent();

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
        
        this.closeCommBeforeEvent();
        
        this.messageForConsole("**** Connection closed ****\n");
        
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
        
        this.closeCommAfterEvent();
        return true;
    }
    
    public Boolean isCommOpen() {
        // TODO: Query comm port for this information.
        return this.commOpen;
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

    public int rowsInQueue() {
        return this.prepQueue.size();
    }

    public int rowsInSend() {
        return this.numCommandsStreamed;
    }
    
    public int rowsSent() {
        return this.numCommandsSent;
    }
    
    public int rowsRemaining() {
        return this.numCommandsStreamed - this.numCommandsCompleted - this.numCommandsSkipped;
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
    
    /**
     * Accepts a command floating between the prepQueue and adds it to the next
     * stage.
     */
    private void queueCommandForComm(GcodeCommand command) throws Exception {
        // Don't send zero length commands.
        if (command.getCommandString().equals("")) {
            this.messageForConsole("Skipping command #" + command.getCommandNumber() + "\n");
            command.setResponse("<skipped by application>");
            command.setSkipped(true);
            // Need to queue the command first so that listeners don't
            // see a random command complete without notice.
            this.commandQueued(command);
            this.commandComplete(command);
            // For the listeners...
            dispatchCommandSent(command);
        } else {
            // Special case for the first command because it is usually updated by completed commands looking back.
            if (this.outgoingQueue.size() == 0 && command.hasComment())
                dispatchCommandCommment(command.getComment());
            this.outgoingQueue.add(command);
            this.commandQueued(command);
            this.sendStringToComm(command.getCommandString());
        }
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
        isReadyToStreamFileEvent();
        
        if (this.commOpen == false) {
            throw new Exception("Cannot begin streaming, comm port is not open.");
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
    public void preprocessAndAppendGcodeCommand(String commandString) throws Exception{
        
        // TODO: Expand this to handle canned cycles (Issue#49)
        List<String> processed = gcp.preprocessCommand(commandString);

        for (String s : processed) {
            GcodeCommand command = this.commandCreator.createCommand(s);
            this.prepQueue.add(command);
        }        
    }

    public void appendGcodeCommands(Iterable<String> commandStrings) throws Exception{
        for (String s : commandStrings) {
            GcodeCommand command = this.commandCreator.createCommand(s);
            this.prepQueue.add(command);
        }
    }

    public void appendGcodeCommands(Iterable<String> commandStrings, File fromFile) throws Exception {
        this.gcodeFile = fromFile;
        appendGcodeCommands(commandStrings);
    }

    public void saveToFile(File f) throws Exception {
        this.saveToFileMode = true;
        this.outputFileWriter = new PrintWriter(f, "UTF-8");
        beginStreaming();
        this.outputFileWriter.close();
        this.saveToFileMode = false;
    }
    
    /**
     * Send all queued commands to comm port.
     */
    public void beginStreaming() throws Exception {

        // Throw caution to the wind when saving to a file.
        if (!this.saveToFileMode) {
            this.isReadyToStreamFile();
        }
        
        if (this.prepQueue.size() == 0) {
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
        this.numCommandsStreamed = 0;
        this.numCommandsSent = 0;
        this.numCommandsSkipped = 0;
        this.numCommandsCompleted = 0;

        try {
            // Send all queued commands and wait for a response.
            GcodeCommand command;
            while (this.prepQueue.size() > 0) {
                numCommandsStreamed++;
                command = this.prepQueue.remove();
                if (this.saveToFileMode) {
                    this.outputFileWriter.println(command.getCommandString());
                } else {
                    queueCommandForComm(command);
                }
            }
            
            // Inform the GUI of the postprocessed number of commands.
            this.dispatchPostProcessData(numCommandsStreamed);
        } catch(Exception e) {
            e.printStackTrace();
            this.isStreaming = false;
            this.streamStart = 0;
            throw e;
        }
    }
    
    public void pauseStreaming() throws IOException {
        this.messageForConsole("\n**** Pausing file transfer. ****\n\n");
        pauseStreamingEvent();
        this.paused = true;
        this.comm.pauseSend();
    }
    
    public void resumeStreaming() throws IOException {
        this.messageForConsole("\n**** Resuming file transfer. ****\n\n");
        resumeStreamingEvent();
        this.paused = false;
        this.comm.resumeSend();
    }
    
    public void cancelSend() {
        this.messageForConsole("\n**** Canceling file transfer. ****\n\n");

        cancelSendBeforeEvent();
        
        // Don't clear the command queue, there might be a situation where a
        // send is in progress while the next queue is being built. In which
        // case a cancel would only be expected to cancel the current action
        // to make way for the queued commands.
        //this.prepQueue.clear();
        
        this.outgoingQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();

        this.comm.cancelSend();
        
        cancelSendAfterEvent();
    }
    
    private void flushSendQueues() {
        this.prepQueue.clear();
        this.outgoingQueue.clear();
        this.awaitingResponseQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();
    }

    private void printStateOfQueues() {
        System.out.println("command queue size = " + this.prepQueue.size());
        System.out.println("outgoing queue size = " + this.outgoingQueue.size());
        System.out.println("awaiting response queue size = " + this.awaitingResponseQueue.size());
        System.out.println("completed command list size = " + this.completedCommandList.size());
        System.out.println("error command list size = " + this.errorCommandList.size());
        System.out.println("============");
        
    }
    
    // No longer a listener event
    private void commandQueued(GcodeCommand command) {
        dispatchCommandQueued(command);
    }

    // No longer a listener event
    private void fileStreamComplete(String filename, boolean success) {
        this.messageForConsole("\n**** Finished sending file. ****\n\n");
        this.streamStop = System.currentTimeMillis();
        this.isStreaming = false;
        dispatchStreamComplete(filename, success);        
    }

    public List<String> preprocess(List<String> lines) {
        return gcp.preprocessCommands(lines);
    }
    
    @Override
    public void commandSent(String command) {
        if (this.isStreamingFile()) {
            this.numCommandsSent++;
        }

        GcodeCommand c = this.outgoingQueue.remove();
        c.setSent(true);
        
        if (!c.getCommandString().equals(command)) {
            this.errorMessageForConsole("Command <"+c.getCommandString()+
                    "> does not equal expected command <"+command+">");
            try{
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.awaitingResponseQueue.add(c);
        
        dispatchCommandSent(c);
    }
    
    /**
     * Notify controller that the next command has completed with response.
     */
    public void commandComplete(String response) throws Exception {
        GcodeCommand command = this.awaitingResponseQueue.peek();
        if (command == null) {
            command = new GcodeCommand("");
        }
        command.setResponse(response);
        this.commandComplete(command);
    }
    
    /**
     * Internal command complete has extra handling for skipped command case.
     */
    private void commandComplete(GcodeCommand command) throws Exception {
        GcodeCommand c = command;

        // If the command wasn't sent, it was skipped and should be ignored
        // from the remaining queues.
        if (!command.isSkipped()) {
            if (this.awaitingResponseQueue.size() == 0) {
                throw new Exception("Attempting to complete a command that "
                        + "doesn't exist: <" + command.toString() + ">");
            }
            
            c = this.awaitingResponseQueue.remove();
            c.setResponse(command.getResponse());
            this.completedCommandList.add(c);
            
            if (this.isStreamingFile()) {
                // Peek to see if the currently processing command has a comment.
                GcodeCommand next = this.awaitingResponseQueue.peek();
                if (next != null && next.hasComment()) {
                    dispatchCommandCommment(next.getComment());
                }
                this.numCommandsCompleted++;
            }
        } else {
            if (this.isStreamingFile()) {
                this.numCommandsSkipped++;
            }
        }
        
        dispatchCommandComplete(c);
        
        if (this.isStreamingFile() &&
                this.awaitingResponseQueue.size() == 0 &&
                this.outgoingQueue.size() == 0 &&
                this.prepQueue.size() == 0) {
            String streamName = "queued commands";
            if (this.gcodeFile != null) {
                streamName = this.gcodeFile.getName();
            }
            
            boolean isSuccess = (this.numCommandsStreamed == (this.numCommandsCompleted + this.numCommandsSkipped));
            this.fileStreamComplete(streamName, isSuccess);
        }
    }

    @Override
    public void messageForConsole(String msg) {
        dispatchConsoleMessage(msg, Boolean.FALSE);
    }
    
    @Override
    public void verboseMessageForConsole(String msg) {
        dispatchConsoleMessage(msg, Boolean.TRUE);
    }
    
    @Override
    public void errorMessageForConsole(String msg) {
        dispatchConsoleMessage("[Error] " + msg, Boolean.TRUE);
    }


    @Override
    public void rawResponseListener(String response) {
        rawResponseHandler(response);
    }

    /**
     * Listener management.
     */
    public void addListener(ControllerListener cl) {
        this.listeners.add(cl);
    }

    protected void dispatchStatusString(String state, Point3d machine, Point3d work) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.statusStringListener(state, machine, work);
            }
        }
    }
    
    protected void dispatchConsoleMessage(String message, Boolean verbose) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.messageForConsole(message, verbose);
            }
        }
    }
    
    protected void dispatchStreamComplete(String filename, Boolean success) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.fileStreamComplete(filename, success);
            }
        }
    }
    
    protected void dispatchCommandQueued(GcodeCommand command) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandQueued(command);
            }
        }
    }
    
    protected void dispatchCommandSent(GcodeCommand command) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandSent(command);
            }
        }
    }
    
    protected void dispatchCommandComplete(GcodeCommand command) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandComplete(command);
            }
        }
    }
    
    protected void dispatchCommandCommment(String comment) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandComment(comment);
            }
        }
    }
    
    protected void dispatchPostProcessData(int numRows) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.postProcessData(numRows);
            }
        }
    }

    public abstract long getJobLengthEstimate(Collection<String> jobLines);
}
