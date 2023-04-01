/*
    Copyright 2013-2023 Will Winder

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

import com.willwinder.universalgcodesender.communicator.ICommunicatorListener;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.ICommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.*;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.Utils.formatter;
import static com.willwinder.universalgcodesender.model.CommunicatorState.*;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static com.willwinder.universalgcodesender.model.UnitUtils.scaleUnits;

/**
 * Abstract Control layer, coordinates all aspects of control.
 *
 * @author wwinder
 */
public abstract class AbstractController implements ICommunicatorListener, IController {
    private static final Logger logger = Logger.getLogger(AbstractController.class.getName());
    private final GcodeParser parser = new GcodeParser();
    private final ConnectionWatchTimer connectionWatchTimer = new ConnectionWatchTimer(this);
    private final ICommandCreator commandCreator;

    // These abstract objects are initialized in concrete class.
    protected final ICommunicator comm;
    protected MessageService messageService = new MessageService();

    // Added value
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);

    // For keeping track of the time spent streaming a file
    private final StopWatch streamStopWatch = new StopWatch();

    // This metadata needs to be cached instead of looked up from queues and
    // streams, because those sources may be compromised during a cancel.
    private final AtomicInteger numCommands = new AtomicInteger(0);
    private final AtomicInteger numCommandsSent =  new AtomicInteger(0);
    private final AtomicInteger numCommandsSkipped = new AtomicInteger(0);
    private final AtomicInteger numCommandsCompleted = new AtomicInteger(0);

    // Commands become active after the Communicator notifies us that they have
    // been sent.
    //
    // Algorithm:
    //   1) Queue file stream(s).
    //   2) As commands are sent by the Communicator create a GCodeCommand
    //      (with command number) object and add it to the activeCommands list.
    //   3) As commands are completed remove them from the activeCommand list.
    private final BlockingDeque<GcodeCommand> activeCommands;    // The list of active commands.
    private IGcodeStreamReader streamCommands;    // The stream of commands to send.

    // Listeners
    private final List<ControllerListener> listeners;

    //Track current mode to restore after jogging
    private String distanceModeCode = null;
    private String unitsCode = null;

    // Maintain the current state given actions performed.
    // Concrete classes with a status field should override getControlState.
    private CommunicatorState currentState = COMM_DISCONNECTED;

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

    @Override
    public void returnToHome(double safetyHeightInMm) throws Exception {
        if (isIdle()) {
            // Convert the safety height to the same units as the current gcode state
            UnitUtils.Units currentUnit = getCurrentGcodeState().getUnits();
            double safetyHeight = safetyHeightInMm * UnitUtils.scaleUnits(MM, currentUnit);

            // If Z is less than zero, raise it before further movement.
            double currentZPosition = getControllerStatus().getWorkCoord().getPositionIn(currentUnit).get(Axis.Z);
            if (currentZPosition < safetyHeight) {
                String moveToSafetyHeightCommand = GcodeUtils.GCODE_RETURN_TO_Z_ZERO_LOCATION;
                if (safetyHeight > 0) {
                    moveToSafetyHeightCommand = GcodeUtils.generateMoveCommand("G90 G0", 0, new PartialPosition(null, null, safetyHeight, currentUnit));
                }
                sendCommandImmediately(createCommand(moveToSafetyHeightCommand));
            }
            sendCommandImmediately(createCommand(GcodeUtils.GCODE_RETURN_TO_XY_ZERO_LOCATION));
            sendCommandImmediately(createCommand(GcodeUtils.GCODE_RETURN_TO_Z_ZERO_LOCATION));
        }
    }

    /**
     * Reset machine coordinates to zero at the current location.
     */
    @Override
    public void resetCoordinatesToZero() throws Exception {
        setWorkPosition(new PartialPosition(0.0, 0.0, 0.0, getCurrentGcodeState().getUnits()));
    }

    /**
     * Reset given machine coordinate to zero at the current location.
     */
    @Override
    public void resetCoordinateToZero(final Axis axis) throws Exception {
        setWorkPosition(PartialPosition.from(axis, 0.0, getCurrentGcodeState().getUnits()));
    }

    @Override
    public void setWorkPosition(PartialPosition axisPosition) throws Exception {
        throw new Exception(Localization.getString("controller.exception.setworkpos"));
    }

    @Override
    public void openDoor() throws Exception {
        throw new Exception(Localization.getString("controller.exception.door"));
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
    public void jogMachine(PartialPosition distance, double feedRate) throws Exception {
        logger.log(Level.INFO, "Adjusting manual location.");

        String commandString = GcodeUtils.generateMoveCommand("G91G1", feedRate, distance);

        GcodeCommand command = createCommand(commandString);
        command.setTemporaryParserModalChange(true);
        sendCommandImmediately(command);
        restoreParserModalState();
    }

    @Override
    public void jogMachineTo(PartialPosition position, double feedRate) throws Exception {
        String commandString = GcodeUtils.generateMoveToCommand("G90G1", position, feedRate);

        GcodeCommand command = createCommand(commandString);
        command.setTemporaryParserModalChange(true);
        sendCommandImmediately(command);
        restoreParserModalState();
    }

    @Override
    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception {
        logger.log(Level.INFO,
                String.format("Probing. axis: %s, feedRate: %s, distance: %s, units: %s",
                    axis, feedRate, distance, units));

        String probePattern = "G38.2 %s%s F%s";
        String probeCommand = String.format(probePattern, axis, formatter.format(distance), formatter.format(feedRate));

        GcodeCommand state = createCommand(GcodeUtils.unitCommand(units) + " G91 G49");
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

    @Override
    public ICommandCreator getCommandCreator() {
        return commandCreator;
    }

    /**
     * Dependency injection constructor to allow a mock communicator.
     */
    protected AbstractController(ICommunicator comm, ICommandCreator commandCreator) {
        this.comm = comm;
        this.comm.addListener(this);

        this.activeCommands = new LinkedBlockingDeque<>();
        this.listeners = Collections.synchronizedList(new ArrayList<>());
        this.commandCreator = commandCreator;
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
    public Boolean openCommPort(ConnectionDriver connectionDriver, String port, int portRate) throws Exception {
        if (isCommOpen()) {
            throw new Exception("Comm port is already open.");
        }

        // No point in checking response, it throws an exception on errors.
        this.comm.connect(connectionDriver, port, portRate);
        this.setCurrentState(COMM_IDLE);
        this.setControllerState(ControllerState.CONNECTING);

        if (isCommOpen()) {
            dispatchConsoleMessage(MessageType.INFO,
                    "**** Connected to " + port + " @ " + portRate + " baud ****\n");
            openCommAfterEvent();
        }

        connectionWatchTimer.start();
        return isCommOpen();
    }

    protected abstract void setControllerState(ControllerState controllerState);

    @Override
    public Boolean closeCommPort() throws Exception {
        // Already closed.
        if (!isCommOpen() && getControllerStatus().getState() == ControllerState.DISCONNECTED) {
            return true;
        }

        this.closeCommBeforeEvent();

        this.dispatchConsoleMessage(MessageType.INFO,"**** Connection closed ****\n");

        this.flushSendQueues();
        this.comm.disconnect();

        this.closeCommAfterEvent();
        this.setControllerState(ControllerState.DISCONNECTED);
        this.connectionWatchTimer.stop();
        return true;
    }

    @Override
    public Boolean isCommOpen() {
        return comm != null && comm.isConnected();
    }

    //// File send metadata ////

    @Override
    public Boolean isStreaming() {
        return isStreaming.get();
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
                return numCommands.get();
            case ROWS_SENT:
                return numCommandsSent.get();
            case ROWS_COMPLETED:
                return numCommandsCompleted.get() + numCommandsSkipped.get();
            case ROWS_REMAINING:
                return numCommands.get() <= 0 ? 0 : numCommands.get() - (numCommandsCompleted.get() + numCommandsSkipped.get());
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
        return Optional.ofNullable(activeCommands.peekFirst());
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

        this.setCurrentState(CommunicatorState.COMM_SENDING);
        this.comm.queueCommand(command);
        this.comm.streamCommands();
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
    public void queueStream(IGcodeStreamReader r) {
        this.streamCommands = r;
        updateNumCommands();
    }

    @Override
    public GcodeCommand createCommand(String gcode) throws Exception {
        return this.commandCreator.createCommand(gcode);
    }

    /**
     * Send all queued commands to comm port.
     * @throws java.lang.Exception
     */
    @Override
    public void beginStreaming() throws Exception {

        this.isReadyToStreamFile();

        // Throw if there's nothing queued.
        if (this.streamCommands == null) {
            throw new Exception("There are no commands queued for streaming.");
        }

        isStreaming.set(true);
        streamStopWatch.reset();
        streamStopWatch.start();
        numCommands.set(0);
        numCommandsSent.set(0);
        numCommandsSkipped.set(0);
        numCommandsCompleted.set(0);
        updateNumCommands();

        // Send all queued commands and streams then kick off the stream.
        try {
            listeners.forEach(ControllerListener::streamStarted);
            if (this.streamCommands != null) {
                comm.queueStreamForComm(this.streamCommands);
            }

            comm.streamCommands();
        } catch(Exception e) {
            isStreaming.set(false);
            this.streamStopWatch.reset();
            this.comm.cancelSend();
            throw e;
        }
    }

    @Override
    public void pauseStreaming() throws Exception {
        this.dispatchConsoleMessage(MessageType.INFO,"\n**** Pausing file transfer. ****\n\n");
        pauseStreamingEvent();
        listeners.forEach(ControllerListener::streamPaused);
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
        listeners.forEach(ControllerListener::streamResumed);
        this.comm.resumeSend();
        this.setCurrentState(COMM_SENDING);

        if (streamStopWatch.isSuspended()) {
            this.streamStopWatch.resume();
        }
    }

    @Override
    public CommunicatorState getCommunicatorState() {
        return this.currentState;
    }

    @Override
    public Boolean isPaused() {
        return getCommunicatorState() == COMM_SENDING_PAUSED;
    }

    @Override
    public Boolean isIdle() {
        try {
            return (getCommunicatorState() == COMM_IDLE || getCommunicatorState() == COMM_CHECK) && isIdleEvent();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void cancelSend() throws Exception {
        this.dispatchConsoleMessage(MessageType.INFO, "\n**** Canceling file transfer. ****\n\n");
        cancelSendBeforeEvent();
        listeners.forEach(ControllerListener::streamCanceled);
        cancelCommands();
        cancelSendAfterEvent();
    }
    
    // Default implementation just calls cancelSend()
    @Override
    public void cancelJog() throws Exception {
        cancelSend();
    }

    @Override
    public void cancelCommands() {
        comm.cancelSend();
        isStreaming.set(false);
        if (streamStopWatch.isStarted()) {
            streamStopWatch.stop();
        }
    }

    @Override
    public void resetBuffers() {
        this.activeCommands.clear();
        this.comm.resetBuffers();
        this.setCurrentState(COMM_IDLE);
    }

    // Reset send queue and idx's.
    private void flushSendQueues() {
        numCommands.set(0);
    }

    private void updateNumCommands() {
        if (streamCommands != null) {
            numCommands.set(streamCommands.getNumRows());
        }
        numCommandsSkipped.set(0);
        numCommandsCompleted.set(0);
        numCommandsSent.set(0);
    }

    // No longer a listener event
    protected void fileStreamComplete(String filename) {
        String duration = Utils.formattedMillis(getSendDuration());
        dispatchConsoleMessage(MessageType.INFO, String.format("%n**** Finished sending file in %s ****%n%n", duration));
        if (streamStopWatch.isStarted()) {
            streamStopWatch.stop();
        }
        isStreaming.set(false);
        dispatchStreamComplete(filename);
    }

    @Override
    public void commandSent(GcodeCommand command) {
        if (this.isStreaming()) {
            numCommandsSent.incrementAndGet();
        }

        command.setSent(true);
        this.activeCommands.add(command);

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
            }
        } catch (Exception ignored) {
            logger.log(Level.SEVERE, "Couldn't set the state to paused.");
        }
    }

    public void checkStreamFinished() {
        ControllerState state = getControllerStatus().getState();
        if (this.isStreaming() &&
                !this.comm.areActiveCommands() &&
                this.comm.numActiveCommands() == 0 &&
                rowsRemaining() <= 0 &&
                (state == ControllerState.CHECK || state == ControllerState.IDLE)) {
            String streamName = "queued commands";
            this.fileStreamComplete(streamName);
        }
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        if (this.isStreaming()) {
            numCommandsSkipped.incrementAndGet();
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
        command.setDone(true);
        dispatchCommandSkipped(command);

        checkStreamFinished();
    }

    /**
     * Notify controller that the next command has completed with response and
     * that the stream is complete once the last command has finished.
     */
    public void commandComplete(String response) throws UnexpectedCommand {
        if (activeCommands.isEmpty()) {
            throw new UnexpectedCommand(
                    Localization.getString("controller.exception.unexpectedCommand"));
        }

        GcodeCommand command = activeCommands.pollFirst();
        updateParserModalState(command);

        numCommandsCompleted.incrementAndGet();

        if (activeCommands.isEmpty()) {
            setCurrentState(COMM_IDLE);
        }

        dispatchCommandComplete(command);
        checkStreamFinished();
    }

    @Override
    public void rawResponseListener(String response) {
        rawResponseHandler(response);
    }

    protected void setCurrentState(CommunicatorState state) {
        this.currentState = state;
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
            // Needs to be removed with thread safe operation,
            // will otherwise result in ConcurrentModifificationException
            this.listeners.removeIf(l -> l.equals(listener));
        }
    }

    protected void dispatchStatusString(ControllerStatus status) {
        listeners.forEach(l -> l.statusStringListener(status));
    }

    protected void dispatchConsoleMessage(MessageType type, String message) {
        if (messageService != null) {
            messageService.dispatchMessage(type, message);
        } else {
            logger.fine("No message service is assigned, so the message could not be delivered: " + type + ": " + message);
        }
    }

    protected void dispatchStreamComplete(String filename) {
        listeners.forEach(l -> l.streamComplete(filename));
    }

    protected void dispatchCommandSkipped(GcodeCommand command) {
        listeners.forEach(l -> l.commandSkipped(command));
    }

    protected void dispatchCommandSent(GcodeCommand command) {
        listeners.forEach(l -> l.commandSent(command));
    }

    protected void dispatchCommandComplete(GcodeCommand command) {
        listeners.forEach(l -> l.commandComplete(command));
    }

    protected void dispatchAlarm(Alarm alarm) {
        listeners.forEach(l -> l.receivedAlarm(alarm));
    }

    protected void dispatchProbeCoordinates(Position p) {
        listeners.forEach(l -> l.probeCoordinates(p));
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
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Problem parsing command: " + command, e);
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
    public ICommunicator getCommunicator() {
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

    @Override
    public IFileService getFileService() {
        throw new RuntimeException("The file service is not available for this type of controller");
    }

    public class UnexpectedCommand extends Exception {
        public UnexpectedCommand(String message) {
            super(message);
        }
    }
}
