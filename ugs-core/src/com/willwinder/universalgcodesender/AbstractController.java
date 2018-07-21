/*
    Copyright 2013-2018 Will Winder

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

import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;

import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_DISCONNECTED;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_SENDING;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_SENDING_PAUSED;
import static com.willwinder.universalgcodesender.Utils.formatter;
import com.willwinder.universalgcodesender.model.Axis;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static com.willwinder.universalgcodesender.model.UnitUtils.scaleUnits;

/**
 * Abstract Control layer, coordinates all aspects of control.
 *
 * @author wwinder
 */
public abstract class AbstractController implements SerialCommunicatorListener, IController {;
    private static final Logger logger = Logger.getLogger(AbstractController.class.getName());
    private final GcodeParser parser = new GcodeParser();

    // These abstract objects are initialized in concrete class.
    protected final AbstractCommunicator comm;
    protected MessageService messageService;
    protected GcodeCommandCreator commandCreator;

    // Outside influence
    private boolean statusUpdatesEnabled = true;
    private int statusUpdateRate = 200;

    // Added value
    private Boolean isStreaming = false;

    // For keeping track of the time spent streaming a file
    private StopWatch streamStopWatch = new StopWatch();

    // This metadata needs to be cached instead of looked up from queues and
    // streams, because those sources may be compromised during a cancel.
    private int numCommands = 0;
    private int numCommandsSent = 0;
    private int numCommandsSkipped = 0;
    private int numCommandsCompleted = 0;

    // Commands become active after the Communicator notifies us that they have
    // been sent.
    //
    // Algorithm:
    //   1) Send all manually queued commands to the Communicator.
    //   2) Queue file stream(s).
    //   3) As commands are sent by the Communicator create a GCodeCommand
    //      (with command number) object and add it to the activeCommands list.
    //   4) As commands are completed remove them from the activeCommand list.
    private ArrayList<GcodeCommand> queuedCommands;    // The list of specially queued commands to be sent.
    private ArrayList<GcodeCommand> activeCommands;    // The list of active commands.
    private GcodeStreamReader       streamCommands;    // The stream of commands to send.
    private int                     errorCount;        // Number of 'error' responses.

    // Listeners
    private ArrayList<ControllerListener> listeners;

    //Track current mode to restore after jogging
    private String distanceModeCode = null;
    private String unitsCode = null;

    // Maintain the current state given actions performed.
    // Concrete classes with a status field should override getControlState.
    private ControlState currentState = COMM_DISCONNECTED;


    /** API Interface. */

    /**
     * Called to ask controller if it is idle.
     */
    protected abstract Boolean isIdleEvent();
    
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
    abstract protected void cancelSendBeforeEvent() throws Exception;
    abstract protected void cancelSendAfterEvent() throws Exception;
    
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
     * Called prior to streaming commands, separate in case you need to be more
     * restrictive about streaming a file vs. sending a command.
     * throws an exception if not ready.
     */
    abstract protected void isReadyToStreamCommandsEvent() throws Exception;

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
        setWorkPosition(Axis.X, 0);
        setWorkPosition(Axis.Y, 0);
        setWorkPosition(Axis.Z, 0);
    }
    
    /**
     * Reset given machine coordinate to zero at the current location.
     */
    @Override
    public void resetCoordinateToZero(final Axis axis) throws Exception {
        setWorkPosition(axis, 0);
    }

    @Override
    public void setWorkPosition(Axis axis, double position) throws Exception {
        throw new Exception(Localization.getString("controller.exception.setworkpos"));
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

    @Override
    public void jogMachine(int dirX, int dirY, int dirZ, double stepSize,
            double feedRate, UnitUtils.Units units) throws Exception {
        logger.log(Level.INFO, "Adjusting manual location.");

        // Format step size from spinner.
        String formattedStepSize = Utils.formatter.format(stepSize);
        String formattedFeedRate = Utils.formatter.format(feedRate);

        String commandString = GcodeUtils.generateXYZ("G91G1", units,
                formattedStepSize, formattedFeedRate, dirX, dirY, dirZ);

        GcodeCommand command = createCommand(commandString);
        command.setTemporaryParserModalChange(true);
        sendCommandImmediately(command);
        restoreParserModalState();
    }

    @Override
    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception {
        logger.log(Level.INFO, "Probing.");

        String probePattern = "G38.2 %s%s F%s";
        double unitScale = scaleUnits(units, MM);
        String probeCommand = String.format(probePattern, axis, 
                formatter.format(distance * unitScale),
                formatter.format(feedRate * unitScale));

        GcodeCommand state = createCommand("G21 G91 G49");
        state.setTemporaryParserModalChange(true);

        GcodeCommand probe = createCommand(probeCommand);
        probe.setTemporaryParserModalChange(true);

        this.sendCommandImmediately(state);
        this.sendCommandImmediately(probe);

        restoreParserModalState();
    }

    @Override
    public void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception {
        logger.log(Level.INFO, "Probe offset.");

        String offsetPattern = "G43.1 %s%s";
        String offsetCommand = String.format(offsetPattern,
                axis,
                formatter.format(offset * scaleUnits(units, MM)));

        GcodeCommand state = createCommand("G21 G90");
        state.setTemporaryParserModalChange(true);

        this.sendCommandImmediately(state);
        this.sendCommandImmediately(createCommand(offsetCommand));

        restoreParserModalState();
    }
    
    /**
     * Listener event for status update values;
     */
    abstract protected void statusUpdatesEnabledValueChanged(boolean enabled);
    abstract protected void statusUpdatesRateValueChanged(int rate);

    /**
     * Accessible so that it can be configured.
     * @return
     */
    public GcodeCommandCreator getCommandCreator() {
        return commandCreator;
    }

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
        if (this.comm != null) {
            this.comm.setSingleStepMode(enabled);
        }
    }

    @Override
    public boolean getSingleStepMode() {
        if (this.comm != null) {
            return this.comm.getSingleStepMode();
        }
        return false;
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
    public Boolean openCommPort(ConnectionDriver connectionDriver, String port, int portRate) throws Exception {
        if (isCommOpen()) {
            throw new Exception("Comm port is already open.");
        }
        
        // No point in checking response, it throws an exception on errors.
        this.comm.openCommPort(connectionDriver, port, portRate);
        this.setCurrentState(COMM_IDLE);
        
        if (isCommOpen()) {
            this.openCommAfterEvent();

            this.dispatchConsoleMessage(MessageType.INFO,
                    "**** Connected to " + port + " @ " + portRate + " baud ****\n");
        }
                
        return isCommOpen();
    }

    @Override
    public Boolean closeCommPort() throws Exception {
        // Already closed.
        if (isCommOpen() == false) {
            return true;
        }
        
        this.closeCommBeforeEvent();
        
        this.dispatchConsoleMessage(MessageType.INFO,"**** Connection closed ****\n");
        
        // I was noticing odd behavior, such as continuing to send 'ok's after
        // closing and reopening the comm port.
        // Note: The "Configuring-Grbl-v0.8" documentation recommends frequent
        //       soft resets, but also warns that the "startup" block will run
        //       on a reset and startup blocks may include motion commands.
        //this.issueSoftReset();
        this.flushSendQueues();
        this.commandCreator.resetNum();
        this.comm.closeCommPort();

        this.closeCommAfterEvent();
        return true;
    }
    
    @Override
    public Boolean isCommOpen() {
        return comm != null && comm.isCommOpen();
    }
    
    //// File send metadata ////
    
    @Override
    public Boolean isStreaming() {
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
        return streamStopWatch.getTime();
    }

    private enum RowStat {
        TOTAL_ROWS,
        ROWS_SENT,
        ROWS_COMPLETED,
        ROWS_REMAINING
    }

    /**
     * Get one of the row statistics, returns -1 if stat is unavailable.
     * @param stat
     * @return 
     */
    public int getRowStat(RowStat stat) {
        switch (stat) {
            case TOTAL_ROWS:
                return this.numCommands;
            case ROWS_SENT:
                return this.numCommandsSent;
            case ROWS_COMPLETED:
                return this.numCommandsCompleted + this.numCommandsSkipped;
            case ROWS_REMAINING:
                return this.numCommands - (this.numCommandsCompleted + this.numCommandsSkipped);
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
    public int rowsCompleted() {
        return getRowStat(RowStat.ROWS_COMPLETED);
    }
    
    @Override
    public int rowsRemaining() {
        return getRowStat(RowStat.ROWS_REMAINING);
    }

    @Override
    public Optional<GcodeCommand> getActiveCommand() {
        if (activeCommands.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(activeCommands.get(0));
    }

    @Override
    public GcodeState getCurrentGcodeState() {
        return parser.getCurrentState();
    }
    
    /**
     * Creates a gcode command and queues it for send immediately.
     * Note: this is the only place where a string is sent to the comm.
     */
    @Override
    public void sendCommandImmediately(GcodeCommand command) throws Exception {
        isReadyToSendCommandsEvent();
        
        if (!isCommOpen()) {
            throw new Exception("Cannot send command(s), comm port is not open.");
        }

        this.setCurrentState(ControlState.COMM_SENDING);
        this.sendStringToComm(command.getCommandString());
        this.comm.streamCommands();
    }
    
    /**
     * This is the only place where commands with an expected 'ok'/'error'
     * response are sent to the comm - with the exception of command streams.
     */
    private void sendStringToComm(String command) {
        this.comm.queueStringForComm(command + "\n");
    }
    
    @Override
    public Boolean isReadyToReceiveCommands() throws Exception {
        if (!isCommOpen()) {
            throw new Exception("Comm port is not open.");
        }

        if (this.isStreaming()) {
            throw new Exception("Already streaming.");
        }

        return true;
    }

    @Override
    public Boolean isReadyToStreamFile() throws Exception {
        isReadyToStreamCommandsEvent();
        
        isReadyToReceiveCommands();

        if (this.comm.areActiveCommands()) {
            throw new Exception("Cannot stream while there are active commands: "
                    + comm.activeCommandSummary());
        }

        return true;
    }

    @Override
    public void queueStream(GcodeStreamReader r) {
        this.streamCommands = r;
        updateNumCommands();
    }

    @Override
    public GcodeCommand createCommand(String gcode) throws Exception {
        return this.commandCreator.createCommand(gcode);
    }

    @Override
    public void queueCommand(GcodeCommand command) throws Exception {
        this.queuedCommands.add(command);
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
                this.streamCommands == null) {
            throw new Exception("There are no commands queued for streaming.");
        }
        
        // Grbl's "Configuring-Grbl-v0.8" documentation recommends a soft reset
        // prior to starting a job. But will this cause GRBL to reset all the
        // way to reporting version info? Need to double check that before
        // enabling.
        //this.issueSoftReset();
        
        this.isStreaming = true;
        this.streamStopWatch.reset();
        this.streamStopWatch.start();
        this.numCommands = 0;
        this.numCommandsSent = 0;
        this.numCommandsSkipped = 0;
        this.numCommandsCompleted = 0;
        updateNumCommands();

        // Send all queued commands and streams then kick off the stream.
        try {
            while (this.queuedCommands.size() > 0) {
                this.sendStringToComm(this.queuedCommands.remove(0).getCommandString());
            }

            if (this.streamCommands != null) {
                comm.queueStreamForComm(this.streamCommands);
            }

            comm.streamCommands();
        } catch(Exception e) {
            this.isStreaming = false;
            this.streamStopWatch.reset();
            this.comm.cancelSend();
            throw e;
        }
    }
    
    @Override
    public void pauseStreaming() throws Exception {
        this.dispatchConsoleMessage(MessageType.INFO,"\n**** Pausing file transfer. ****\n\n");
        pauseStreamingEvent();
        this.comm.pauseSend();
        this.setCurrentState(COMM_SENDING_PAUSED);

        if (streamStopWatch.isStarted() && !streamStopWatch.isSuspended()) {
            this.streamStopWatch.suspend();
        }
    }
    
    @Override
    public void resumeStreaming() throws Exception {
        this.dispatchConsoleMessage(MessageType.INFO, "\n**** Resuming file transfer. ****\n\n");
        resumeStreamingEvent();
        this.comm.resumeSend();
        this.setCurrentState(COMM_SENDING);

        if (streamStopWatch.isSuspended()) {
            this.streamStopWatch.resume();
        }
    }
    
    @Override
    public ControlState getControlState() {
        return this.currentState;
    }

    @Override
    public Boolean isPaused() {
        return getControlState() == COMM_SENDING_PAUSED;
    }

    @Override
    public Boolean isIdle() {
        try {
            return (getControlState() == COMM_IDLE || getControlState() == COMM_CHECK) && isIdleEvent();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void cancelSend() throws Exception {
        this.dispatchConsoleMessage(MessageType.INFO, "\n**** Canceling file transfer. ****\n\n");

        cancelSendBeforeEvent();
        
        // Don't clear the command queue, there might be a situation where a
        // send is in progress while the next queue is being built. In which
        // case a cancel would only be expected to cancel the current action
        // to make way for the queued commands.
        //this.prepQueue.clear();
        
        cancelCommands();
        
        // If there are no active commands, done streaming. Otherwise wait for
        // them to finish.
        if (!comm.areActiveCommands()) {
            this.isStreaming = false;
        }

        cancelSendAfterEvent();
    }

    @Override
    public void cancelCommands() {
        flushQueuedCommands();
        this.comm.cancelSend();
    }

    @Override
    public void resetBuffers() {
        this.activeCommands.clear();
        this.comm.resetBuffers();
        this.setCurrentState(COMM_IDLE);
    }
    
    private synchronized void flushQueuedCommands() {
        // TODO: Special handling for stream necessary?
        this.queuedCommands.clear();
    }

    // Reset send queue and idx's.
    private void flushSendQueues() {
        errorCount = 0;
        numCommands = 0;
    }

    private void updateNumCommands() {
        numCommands = queuedCommands.size();
        if (streamCommands != null) {
            numCommands += streamCommands.getNumRows();
        }
        numCommandsSkipped = 0;
        numCommandsCompleted = 0;
        numCommandsSent = 0;
    }
    
    // No longer a listener event
    protected void fileStreamComplete(String filename, boolean success) {

        String duration = 
                com.willwinder.universalgcodesender.Utils.
                        formattedMillis(this.getSendDuration());

        this.dispatchConsoleMessage(MessageType.INFO,"\n**** Finished sending file in "+duration+" ****\n\n");
        this.streamStopWatch.stop();
        this.isStreaming = false;
        dispatchStreamComplete(filename, success);        
    }
    
    @Override
    public void commandSent(GcodeCommand command) {
        if (this.isStreaming()) {
            this.numCommandsSent++;
        }
        
        command.setSent(true);
        this.activeCommands.add(command);
        
        if (command.hasComment()) {
            dispatchCommandCommment(command.getComment());
        }
        dispatchCommandSent(command);
        dispatchConsoleMessage(MessageType.INFO, ">>> " + StringUtils.trimToEmpty(command.getCommandString()) + "\n");
    }

    @Override
    public void communicatorPausedOnError() {
        dispatchConsoleMessage(MessageType.INFO, "**** The communicator has been paused ****\n");
        try {
            // Synchronize the controller <> communicator state.
            if (!this.isStreaming()) {
                this.comm.resumeSend();
            }
            else {
                this.pauseStreaming();
                // In check mode there is no state transition, so we need to manually make the notification.
                this.dispatchStateChange(COMM_SENDING_PAUSED);
            }
        } catch (Exception ignored) {
            logger.log(Level.SEVERE, "Couldn't set the state to paused.");
        }
    }

    public void checkStreamFinished() {
        if (this.isStreaming() &&
                !this.comm.areActiveCommands() &&
                this.comm.numActiveCommands() == 0 &&
                rowsRemaining() <= 0 &&
                (getControlState() == COMM_IDLE || getControlState() == COMM_SENDING_PAUSED)) {
            String streamName = "queued commands";
            boolean isSuccess = (this.errorCount == 0);
            this.fileStreamComplete(streamName, isSuccess);

            // Make sure the GUI gets updated when the file finishes
            this.dispatchStateChange(getControlState());
        }
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        if (this.isStreaming()) {
            this.numCommandsSkipped++;
        }
        
        StringBuilder message = new StringBuilder();
        boolean hasComment = command.hasComment();
        boolean hasCommand = StringUtils.isNotEmpty(command.getCommandString());
        if (!hasComment && !hasCommand) {
            if (StringUtils.isNotEmpty(command.getOriginalCommandString())) {
                message
                        .append("Skipping line: ")
                        .append(command.getOriginalCommandString());
            } else {
                message
                        .append("Skipping blank line #")
                        .append(command.getCommandNumber());
            }
        }
        else if (hasComment && !hasCommand) {
            message
                    .append("Skipping comment-only line: (")
                    .append(command.getComment())
                    .append(")");
        } else {
            message
                    .append("Skipping line: ")
                    .append(command.getCommandString());
            if (command.hasComment()) {
                message
                        .append(" ; ")
                        .append(command.getComment());
            }
        }
        message.append("\n");
        this.dispatchConsoleMessage(MessageType.INFO, message.toString());
        command.setResponse("<skipped by application>");
        command.setSkipped(true);
        dispatchCommandSkipped(command);
        if (command.hasComment()) {
            dispatchCommandCommment(command.getComment());
        }

        checkStreamFinished();
    }
    
    /**
     * Notify controller that the next command has completed with response and
     * that the stream is complete once the last command has finished.
     */
    public void commandComplete(String response) throws UnexpectedCommand {
        if (this.activeCommands.isEmpty()) {
            throw new UnexpectedCommand(
                    Localization.getString("controller.exception.unexpectedCommand"));
        }
        
        GcodeCommand command = this.activeCommands.remove(0);

        command.setResponse(response);

        updateParserModalState(command);

        this.numCommandsCompleted++;

        if (this.activeCommands.isEmpty()) {
            this.setCurrentState(COMM_IDLE);
        }

        dispatchCommandComplete(command);
        checkStreamFinished();
    }

    @Override
    public void rawResponseListener(String response) {
        rawResponseHandler(response);
    }

    protected void setCurrentState(ControlState state) {
        this.currentState = state;
        if (!this.handlesAllStateChangeEvents()) {
            this.dispatchStateChange(state);
        }
    }

    @Override
    public void addListener(ControllerListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeListener(ControllerListener listener) {
        if (this.listeners.contains(listener)) {
            this.listeners.remove(listener);
        }
    }

    protected void dispatchStatusString(ControllerStatus status) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.statusStringListener(status);
            }
        }
    }
    
    protected void dispatchConsoleMessage(MessageType type, String message) {
        if (messageService != null) {
            messageService.dispatchMessage(type, message);
        } else {
            logger.warning("No message service is assigned, so the message could not be delivered: " + type + ": " + message);
        }
    }
    
    protected void dispatchStateChange(ControlState state) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.controlStateChange(state);
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
    
    protected void dispatchCommandSkipped(GcodeCommand command) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandSkipped(command);
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

    protected void dispatchAlarm(Alarm alarm) {
        if (listeners != null) {
            listeners.forEach(l -> l.receivedAlarm(alarm));
        }
    }
    
    protected void dispatchPostProcessData(int numRows) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.postProcessData(numRows);
            }
        }
    }

    protected void dispatchProbeCoordinates(Position p) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.probeCoordinates(p);
            }
        }
    }

    protected String getUnitsCode() {
        return unitsCode;
    }

    protected void setUnitsCode(String unitsCode) {
        if (unitsCode != null) {
            this.unitsCode = unitsCode;
        }
    }

    protected String getDistanceModeCode() {
        return distanceModeCode;
    }

    protected void setDistanceModeCode(String distanceModeCode) {
        if (distanceModeCode != null) {
            this.distanceModeCode = distanceModeCode;
        }
    }

    @Override
    public void updateParserModalState(GcodeCommand command) {
        if (command.isError() || command.isTemporaryParserModalChange()) {
            return;
        }

        try {
          parser.addCommand(command.getCommandString());
          //System.out.println(parser.getCurrentState());
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Problem prasing command.", e);
        }

        String gcode = command.getCommandString().toUpperCase();
        if (gcode.contains("G90")) {
            distanceModeCode = "G90";
        }
        if (gcode.contains("G91")) {
            distanceModeCode = "G91";
        }
        if (gcode.contains("G20")) {
            unitsCode = "G20";
        }
        if (gcode.contains("G21")) {
            unitsCode = "G21";
        }
    }

    @Override
    public AbstractCommunicator getCommunicator() {
        return comm;
    }

    @Override
    public void restoreParserModalState() {
        StringBuilder cmd = new StringBuilder();
        if (getDistanceModeCode() != null) {
            cmd.append(getDistanceModeCode()).append(" ");
        }
        if (getUnitsCode() != null) {
            cmd.append(getUnitsCode()).append(" ");
        }

        try {
            GcodeCommand command = createCommand(cmd.toString());
            command.setTemporaryParserModalChange(true);
            sendCommandImmediately(command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public class UnexpectedCommand extends Exception {
        public UnexpectedCommand(String message) {
            super(message);
        }
    }
}
