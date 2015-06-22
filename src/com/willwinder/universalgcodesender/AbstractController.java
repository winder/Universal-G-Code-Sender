/*
 * Abstract Control layer, coordinates all aspects of control.
 */
/*
    Copywrite 2013-2015 Will Winder

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
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.io.*;
import java.util.*;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public abstract class AbstractController implements SerialCommunicatorListener, IController {
    public class UnexpectedCommand extends Exception {
        
    }
    
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
    protected void openCommAfterEvent() throws Exception {
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
    abstract protected void pauseStreamingEvent() throws Exception;
    abstract protected void resumeStreamingEvent() throws Exception;
    
    /**
     * Called prior to sending commands, throw an exception if not ready.
     */
    abstract protected void isReadyToSendCommandsEvent() throws Exception;

    /**
     * Raw responses from the serial communicator.
     */
    abstract protected void rawResponseHandler(String response);
    
    /**
     * Performs homing cycle, throw an exception if not supported.
     */
    @Override
    public void performHomingCycle() throws Exception {
        throw new Exception(Localization.getString("controller.exception.homing"));
    }
    
    /**
     * Returns machine to home location, throw an exception if not supported.
     */
    @Override
    public void returnToHome() throws Exception {
        throw new Exception(Localization.getString("controller.exception.gohome"));
    }
        
    /**
     * Reset machine coordinates to zero at the current location.
     */
    @Override
    public void resetCoordinatesToZero() throws Exception {
        throw new Exception(Localization.getString("controller.exception.reset"));
    }
    
    /**
     * Reset given machine coordinate to zero at the current location.
     */
    @Override
    public void resetCoordinateToZero(final char coord) throws Exception {
        throw new Exception(Localization.getString("controller.exception.reset"));
    }
    
    /**
     * Disable alarm mode and put device into idle state, throw an exception 
     * if not supported.
     */
    @Override
    public void killAlarmLock() throws Exception {
        throw new Exception(Localization.getString("controller.exception.killalarm"));
    }
    
    /**
     * Toggles check mode on or off, throw an exception if not supported.
     */
    @Override
    public void toggleCheckMode() throws Exception {
        throw new Exception(Localization.getString("controller.exception.checkmode"));
    }
    
    /**
     * Request parser state, either print it here or expect it in the response
     * handler. Throw an exception if not supported.
     */
    @Override
    public void viewParserState() throws Exception {
        throw new Exception(Localization.getString("controller.exception.parserstate"));
    }
    
    /**
     * Execute a soft reset, throw an exception if not supported.
     */
    @Override
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
    
    /**
     * Accessible so that it can be configured.
     * @return 
     */
    public GcodeCommandCreator getCommandCreator() {
        return commandCreator;
    }
    
    // Outside influence
    private boolean statusUpdatesEnabled = true;
    private int statusUpdateRate = 200;
    
    // State
    private Boolean commOpen = false;
    
    // Parser state
    private Boolean absoluteMode = true;
    
    // Added value
    private Boolean isStreaming = false;
    private Boolean paused = false;
    private long streamStart = 0;
    private long streamStop = 0;
    private File gcodeFile;
    
    // This metadata needs to be cached instead of inferred from queue's because
    // in case of a cancel the queues will be cleared.
    private int numCommandsStreamed = 0;
    private int numCommandsSent = 0;
    private int numCommandsSkipped = 0;
    private int numCommandsCompleted = 0;
    
    // Structures for organizing all streaming commands.
    // This is structured as an array of commands with pointers that move across
    // the array.
    //
    // outgoingQueueIdx (o) - if 0, then no commands have been sent.
    //                      - if 50, then commands 0-50 have been queued to the comm layer.
    //
    // sentIdx (s) - (s - o) are waiting on the comm layer.
    //             - (a - s) are sent and awaiting a response.
    //
    // awaitingResponseIdx (a) - if less than outgoingIdx, then there are 
    //                           (o - a + 1) commands in progress.
    //                         - if equal to outgoingIdx, then all commands have
    //                           a response.
    //
    // completedCommandIdx (c) - this should basically always be equal to a - 1.
    //
    // ==========================================|  // 'o' and earlier are queued
    //                       |=======|           |  // 'a' through 's' are sent but not completed commands
    // ==================|   |       |           |  // 'c' and before are completed
    // |   |   |   |   | c | a |   | s |   |   | o |   |   |   |   |   |   |
    
    private ArrayList<GcodeCommand> commands;  // The list of commands to be sent.
    private int outgoingQueueIdx;              // Index of the next command waiting to be queued.
    private int sentIdx;                       // Index of the next command to send.
    private int awaitingResponseIdx;           // Index of the next command expecting a response.
    private int completedCommandIdx;           // Index of the last command completed.
    private int errorCount;                    // Number of 'error' responses.
    
    // Listeners
    private ArrayList<ControllerListener> listeners;
        
    /**
     * Dependency injection constructor to allow a mock communicator.
     */
    protected AbstractController(AbstractCommunicator comm) {
        this.comm = comm;
        this.comm.setListenAll(this);
        
        //this.gcp = new GcodeParser();

        commands = new ArrayList<>();
        outgoingQueueIdx = 0;
        sentIdx = 0;
        awaitingResponseIdx = 0;
        completedCommandIdx = -1;
        
        /*
        this.prepQueue = new ConcurrentLinkedQueue<>();
        this.outgoingQueue = new ConcurrentLinkedQueue<>();
        this.awaitingResponseQueue = new ConcurrentLinkedQueue<>();
        this.completedCommandList = new ConcurrentLinkedQueue<>();
        this.errorCommandList = new ConcurrentLinkedQueue<>();
        */
        
        this.listeners = new ArrayList<>();
    }
    
    @Deprecated public AbstractController() {
        this(new GrblCommunicator()); //f4grx: connection created at opencomm() time
    }

    @Override
    public void setSingleStepMode(boolean enabled) {
        this.comm.setSingleStepMode(enabled);
    }

    @Override
    public boolean getSingleStepMode() {
        return this.comm.getSingleStepMode();
    }
    
    @Override
    public void setStatusUpdatesEnabled(boolean enabled) {
        if (this.statusUpdatesEnabled != enabled) {
            this.statusUpdatesEnabled = enabled;
            statusUpdatesEnabledValueChanged(enabled);
        }
    }
    
    @Override
    public boolean getStatusUpdatesEnabled() {
        return this.statusUpdatesEnabled;
    }
    
    @Override
    public void setStatusUpdateRate(int rate) {
        if (this.statusUpdateRate != rate) {
            this.statusUpdateRate = rate;
            statusUpdatesRateValueChanged(rate);
        }
    }
    
    @Override
    public int getStatusUpdateRate() {
        return this.statusUpdateRate;
    }
    
    @Override
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
    
    @Override
    public Boolean closeCommPort() throws Exception {
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
    
    @Override
    public Boolean isCommOpen() {
        // TODO: Query comm port for this information.
        return this.commOpen;
    }
    
    //// File send metadata ////
    
    @Override
    public Boolean isStreamingFile() {
        return this.isStreaming;
    }
    
    /**
     * Send duration can be one of 3 things:
     * 1. the current running time of a send.
     * 2. the entire duration of the most recent send.
     * 3. 0 if there has never been a send.
     */
    @Override
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

    @Override
    public int rowsInQueue() {
        if (this.completedCommandIdx < 0) return this.commands.size();
        return this.commands.size() - this.completedCommandIdx;
    }

    @Override
    public int rowsInSend() {
        return this.numCommandsStreamed;
    }
    
    @Override
    public int rowsSent() {
        return this.numCommandsSent;
    }
    
    @Override
    public int rowsRemaining() {
        return this.numCommandsStreamed - this.numCommandsCompleted - this.numCommandsSkipped;
    }
    
    /**
     * Creates a gcode command and queues it for send immediately.
     * Note: this is the only place where a string is sent to the comm.
     */
    @Override
    public void sendCommandImmediately(String str) throws Exception {
        isReadyToSendCommandsEvent();
        
        if (!this.commOpen) {
            throw new Exception("Cannot send command(s), comm port is not open.");
        }

        GcodeCommand command = this.commandCreator.createCommand(str);
        //this.outgoingQueue.add(command);
        this.commands.add(outgoingQueueIdx, command);

        this.commandQueued(command);
        this.sendStringToComm(command.getCommandString());
    }
    
    private void prepCommandForCommAndSend(int idx) throws Exception {
        GcodeCommand command = commands.get(idx);
        
        // Special case for the first command because it is usually updated by completed commands looking back.
        if (this.outgoingQueueIdx == this.awaitingResponseIdx && command.hasComment())
            dispatchCommandCommment(command.getComment());

        // Add to the list of commands to send.
        //this.commands.add(outgoingIdx, command);

        this.commandQueued(command);

        // Don't send zero length commands.
        if (command.getCommandString().equals("")) {
            this.messageForConsole("Skipping command #" + command.getCommandNumber() + "\n");

            if (command.hasComment())
                command.setResponse("<comment skipped by application>");
            else
                command.setResponse("<skipped by application>");
            command.setSkipped(true);
            // Need to queue the command first so that listeners don't
            // see a random command complete without notice.
            this.commandComplete(command);
            // For the listeners...
            dispatchCommandSent(command);
        } else {
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
        numCommandsStreamed++;
        this.comm.streamCommands();
    }
    
    @Override
    public Boolean isReadyToStreamFile() throws Exception {
        isReadyToSendCommandsEvent();
        
        if (this.commOpen == false) {
            throw new Exception("Cannot send command(s), comm port is not open.");
        }
        if (this.awaitingResponseIdx != this.outgoingQueueIdx) {
            throw new Exception("Cannot stream while there are active commands (controller).");
        }
        if (this.comm.areActiveCommands()) {
            throw new Exception("Cannot stream while there are active commands (communicator).");
        }

        return true;
    }

    @Override
    public void queueCommand(String str) throws Exception {
        GcodeCommand command = this.commandCreator.createCommand(str);
        this.commands.add(command);
    }
    
    @Override
    public void queueCommands(Iterable<String> commandStrings) throws Exception {
        for (String s : commandStrings) {
            queueCommand(s);
        }
    }
    
    /**
     * Send all queued commands to comm port.
     * @throws java.lang.Exception
     */
    @Override
    public void beginStreaming() throws Exception {

        this.isReadyToStreamFile();
        
        if (this.commands.size() <= this.outgoingQueueIdx) {
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
            while (this.outgoingQueueIdx < this.commands.size()) {
                prepCommandForCommAndSend(outgoingQueueIdx);
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
    
    @Override
    public void pauseStreaming() throws Exception {
        this.messageForConsole("\n**** Pausing file transfer. ****\n\n");
        pauseStreamingEvent();
        this.paused = true;
        this.comm.pauseSend();
    }
    
    @Override
    public void resumeStreaming() throws Exception {
        this.messageForConsole("\n**** Resuming file transfer. ****\n\n");
        resumeStreamingEvent();
        this.paused = false;
        this.comm.resumeSend();
    }
    
    @Override
    public void cancelSend() {
        this.messageForConsole("\n**** Canceling file transfer. ****\n\n");

        cancelSendBeforeEvent();
        
        // Don't clear the command queue, there might be a situation where a
        // send is in progress while the next queue is being built. In which
        // case a cancel would only be expected to cancel the current action
        // to make way for the queued commands.
        //this.prepQueue.clear();
        
        //flushSendQueues();
        flushQueuedCommands();
        
        /*
        this.outgoingQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();
        */
        
        this.comm.cancelSend();
        
        cancelSendAfterEvent();
    }
    
    private synchronized void flushQueuedCommands() {
        if (this.commands.size() > 0) {
            this.commands.subList(sentIdx, this.commands.size()).clear();
            this.outgoingQueueIdx = this.awaitingResponseIdx;
        }
    }
    // Reset send queue and idx's.
    private void flushSendQueues() {
        
        this.commands.clear();
        this.sentIdx = 0;
        this.outgoingQueueIdx = 0;
        this.awaitingResponseIdx = 0;
        this.completedCommandIdx = -1;
        this.errorCount = 0;
        
        /*
        this.prepQueue.clear();
        this.outgoingQueue.clear();
        this.awaitingResponseQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();
        */
    }

    private void printStateOfQueues() {
        System.out.println("command queue size = " + this.commands.size());
        System.out.println("sentIdx = " + this.sentIdx);
        System.out.println("outgoingQueueIdx = " + this.outgoingQueueIdx);
        System.out.println("awaitingResponseIdx = " + this.awaitingResponseIdx);
        System.out.println("completedCommandIdx = " + this.completedCommandIdx);
        System.out.println("numErrors = " + this.errorCount);
        System.out.println("============");
        
    }
    
    // No longer a listener event
    private void commandQueued(GcodeCommand command) {
        this.outgoingQueueIdx++;
        dispatchCommandQueued(command);
    }

    // No longer a listener event
    private void fileStreamComplete(String filename, boolean success) {
        this.messageForConsole("\n**** Finished sending file. ****\n\n");
        this.streamStop = System.currentTimeMillis();
        this.isStreaming = false;
        this.flushSendQueues();
        dispatchStreamComplete(filename, success);        
    }
    
    @Override
    public void commandSent(String commandStr) {
        this.sentIdx++;
        
        if (this.isStreamingFile()) {
            this.numCommandsSent++;
        }
        
        GcodeCommand command = this.commands.get(this.awaitingResponseIdx++);

        // Jump over skipped commands
        while (command.isSkipped()) {
            command = this.commands.get(this.awaitingResponseIdx++);
            this.sentIdx++;
        }

        //GcodeCommand c = this.outgoingQueue.remove();
        if (!command.isSkipped())
            command.setSent(true);
        
        if (!command.getCommandString().equals(commandStr)) {
            this.errorMessageForConsole("Command <"+command.getCommandString()+
                    "> does not equal expected command <"+commandStr+">");
        }

        //this.awaitingResponseQueue.add(c);
        
        dispatchCommandSent(command);
    }
    
    /**
     * Notify controller that the next command has completed with response.
     */
    public void commandComplete(String response) throws UnexpectedCommand {
        if (this.commands.size() == (this.completedCommandIdx + 1)) {
            throw new UnexpectedCommand();
        }
        
        GcodeCommand command = this.commands.get(++this.completedCommandIdx);

        while (command.isSkipped() && (this.completedCommandIdx < this.commands.size())) {
            command = this.commands.get(++this.completedCommandIdx);
        }

        command.setResponse(response);
        this.commandComplete(command);
    }
    
    /**
     * Internal command complete has extra handling for skipped command case.
     */
    private void commandComplete(GcodeCommand command) throws UnexpectedCommand {
        GcodeCommand c = command;
        
        // If the command wasn't sent, it was skipped and should be ignored
        // from the remaining queues.
        if (!command.isSkipped()) {
            this.numCommandsCompleted++;

            if (this.completedCommandIdx >= this.outgoingQueueIdx) {
                throw new UnexpectedCommand();
                //throw new Exception("Attempting to complete a command that "
                //        + "doesn't exist: <" + command.toString() + ">");
            }
            
            // Peek to see if the next one is a comment, and skip skipped commands
            while (this.isStreamingFile() && (this.completedCommandIdx + 1) < this.commands.size()) {
                GcodeCommand next = this.commands.get(this.completedCommandIdx + 1);
                if (next != null) {
                    if (next.hasComment()) {
                        dispatchCommandCommment(next.getComment());
                    }

                    if (next.isSkipped()) {
                        this.completedCommandIdx++;
                    } else {
                        break;
                    }
                }
            }
        } else {
            if (this.isStreamingFile()) {
                this.numCommandsSkipped++;
            }
        }
        
        dispatchCommandComplete(c);
        
        if (this.isStreamingFile() &&
                this.commands.size() <= this.outgoingQueueIdx &&
                this.completedCommandIdx == (this.commands.size() - 1)) {
            String streamName = "queued commands";
            if (this.gcodeFile != null) {
                streamName = this.gcodeFile.getName();
            }
            
            boolean isSuccess = (this.errorCount == 0);
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
    @Override
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
}
