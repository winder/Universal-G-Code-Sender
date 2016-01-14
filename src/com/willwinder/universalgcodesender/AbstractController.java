/*
 * Abstract Control layer, coordinates all aspects of control.
 */
/*
    Copywrite 2013-2016 Will Winder

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
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public abstract class AbstractController implements SerialCommunicatorListener, IController {;
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
    
    // This metadata needs to be cached instead of looked up from queues and
    // streams, because those sources may be compromised during a cancel.
    private int numCommands = 0;
    private int numCommandsSent = 0;
    private int numCommandsSkipped = 0;
    private int numCommandsCompleted = 0;
    
    // Commands become active after the Communicator notifies us that they have
    // been sent.
    //
    // The concept of queue'd commands is going away, it was never exposed in
    // the GUI anyway.
    //
    // Algorithm:
    //   1) Send all manually queued commands to the Communicator.
    //   2) Queue file stream(s).
    //   3) As commands are sent by the Communicator create a GCodeCommand
    //      (with command number) object and add it to the activeCommands list.
    //   4) As commands are completed remove them from the activeCommand list.
    private ArrayList<GcodeCommand> activeCommands;    // The list of active commands.
    private ArrayList<String>       queuedCommands;    // The list of specially queued commands to be sent.
    private Reader                  rawStreamCommands; // A stream of commands from a newline separated gcode file.
    private GcodeStreamReader       streamCommands;    // The stream of commands to send.
    private int                     errorCount;        // Number of 'error' responses.
    
    // Listeners
    private ArrayList<ControllerListener> listeners;
        
    /**
     * Dependency injection constructor to allow a mock communicator.
     */
    protected AbstractController(AbstractCommunicator comm) {
        this.comm = comm;
        this.comm.setListenAll(this);
        
        activeCommands = new ArrayList<>();
        queuedCommands = new ArrayList<>();
        
        this.listeners = new ArrayList<>();
    }
    
    @Deprecated
    public AbstractController() {
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

    private enum RowStat {
        TOTAL_ROWS,
        ROWS_SENT,
        ROWS_REMAINING
    }

    /**
     * Get one of the row statistics, returns -1 if stat is unavailable.
     * @param stat
     * @return 
     */
    public int getRowStat(RowStat stat) {
        if (this.rawStreamCommands != null) {
            return -1;
        }

        switch (stat) {
            case TOTAL_ROWS:
                return this.numCommands;
                //return streamCommands.getNumRows();
            case ROWS_SENT:
                return this.numCommandsSent;
                //return streamCommands.getNumRows() - streamCommands.getNumRowsRemaining();
            case ROWS_REMAINING:
                return this.numCommands - this.numCommandsCompleted;
                //return streamCommands.getNumRowsRemaining();
            default:
                throw new IllegalStateException("This should be impossible - RowStat default case.");
        }
    }

    @Override
    public int rowsInSend() {
        return getRowStat(RowStat.TOTAL_ROWS);
    }
    
    @Override
    public int rowsSent() {
        return getRowStat(RowStat.ROWS_SENT);
    }
    
    @Override
    public int rowsRemaining() {
        return getRowStat(RowStat.ROWS_REMAINING);
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

        this.sendStringToComm(str);

        this.comm.streamCommands();
    }
    
    /**
     * This is the only place where commands with an expected 'ok'/'error'
     * response are sent to the comm - with the exception of command streams.
     */
    private void sendStringToComm(String command) {
        this.comm.queueStringForComm(command+"\n");
    }
    
    @Override
    public Boolean isReadyToStreamFile() throws Exception {
        isReadyToSendCommandsEvent();
        
        if (this.commOpen == false) {
            throw new Exception("Cannot begin streaming, comm port is not open.");
        }
        if (this.isStreaming) {
            throw new Exception("Already streaming.");
        }
        if (this.comm.areActiveCommands()) {
            throw new Exception("Cannot stream while there are active commands (communicator).");
        }

        return true;
    }

    @Override
    public void queueRawStream(Reader r) {
        this.rawStreamCommands = r;
        updateNumCommands();
    }

    @Override
    public void queueStream(GcodeStreamReader r) {
        this.streamCommands = r;
        updateNumCommands();
    }

    @Override
    public void queueCommand(String str) throws Exception {
        this.queuedCommands.add(str);
        updateNumCommands();
    }
    
    @Override
    public void queueCommands(Iterable<String> commandStrings) throws Exception {
        for (String s : commandStrings) {
            queueCommand(s);
        }
        updateNumCommands();
    }
    
    /**
     * Send all queued commands to comm port.
     * @throws java.lang.Exception
     */
    @Override
    public void beginStreaming() throws Exception {

        this.isReadyToStreamFile();

        // Throw if there's nothing queued.
        if (this.queuedCommands.size() == 0 &&
                this.rawStreamCommands == null &&
                this.streamCommands == null) {
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
        this.numCommandsSent = 0;
        this.numCommandsSkipped = 0;
        this.numCommandsCompleted = 0;
        updateNumCommands();

        // Send all queued commands and streams then kick off the stream.
        try {
            while (this.queuedCommands.size() > 0) {
                this.sendStringToComm(this.queuedCommands.remove(0));
            }
            
            if (this.rawStreamCommands != null) {
                comm.queueRawStreamForComm(this.rawStreamCommands);
            }

            if (this.streamCommands != null) {
                comm.queueStreamForComm(this.streamCommands);
            }

            comm.streamCommands();
        } catch(Exception e) {
            e.printStackTrace();
            this.isStreaming = false;
            this.streamStart = 0;
            this.comm.cancelSend();
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

        
        this.comm.cancelSend();
        
        // If there are no active commands, done streaming. Otherwise wait for
        // them to finish.
        if (!comm.areActiveCommands()) {
            this.isStreaming = false;
        }

        cancelSendAfterEvent();
    }
    
    private synchronized void flushQueuedCommands() {
        // TODO: Special handling for stream necessary?
        this.queuedCommands.clear();
    }

    // Reset send queue and idx's.
    private void flushSendQueues() {
        this.errorCount = 0;
    }

    private void updateNumCommands() {
        numCommands = queuedCommands.size();
        if (streamCommands != null) {
            numCommands += streamCommands.getNumRows();
        }
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
    public void commandSent(GcodeCommand command) {
        if (this.isStreamingFile()) {
            this.numCommandsSent++;
        }
        
        command.setSent(true);
        this.activeCommands.add(command);
        
        if (command.hasComment()) {
            dispatchCommandCommment(command.getComment());
        }
        dispatchCommandSent(command);
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        if (this.isStreamingFile()) {
            this.numCommandsSkipped++;
        }
        
        this.messageForConsole("Skipping command #" + command.getCommandNumber() + "\n");
        command.setResponse("<skipped by application>");
        command.setSkipped(true);
        dispatchCommandSent(command);
        dispatchCommandComplete(command);
        if (command.hasComment()) {
            dispatchCommandCommment(command.getComment());
        }
    }
    
    /**
     * Notify controller that the next command has completed with response and
     * that the stream is complete once the last command has finished.
     */
    public void commandComplete(String response) throws UnexpectedCommand {
        if (this.activeCommands.size() == 0) {
            throw new UnexpectedCommand();
        }
        
        GcodeCommand command = this.activeCommands.remove(0);

        command.setResponse(response);

        //this.commandComplete(command);
        this.numCommandsCompleted++;
        dispatchCommandComplete(command);

        if (!this.comm.areActiveCommands() && (this.activeCommands.size() == 0)) {
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
