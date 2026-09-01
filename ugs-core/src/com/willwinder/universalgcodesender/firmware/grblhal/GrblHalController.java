/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.ControllerException;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.StatusPollTimer;
import com.willwinder.universalgcodesender.communicator.GrblCommunicator;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.firmware.IOverrideManager;
import com.willwinder.universalgcodesender.firmware.grbl.GrblOverrideManager;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GrblProbeCommand;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.CommunicatorState;
import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_IDLE;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GrblFeedbackMessage;
import com.willwinder.universalgcodesender.types.ProbeGcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A controller implementation for grblHAL.
 * <p>
 * grblHAL implements the GRBL 1.1 protocol which means that status reports, feedback messages and
 * real time commands are handled the same way as for GRBL. It does however add a number of
 * extensions, such as enumerable settings, plugins and an extended set of real time commands, which
 * is why it is implemented as a separate controller.
 *
 * @author Joacim Breiler
 */
public class GrblHalController extends AbstractController {
    private static final Logger LOGGER = Logger.getLogger(GrblHalController.class.getName());

    /**
     * The number of status reports to wait for the machine to come to a complete stop when
     * cancelling a stream before issuing a soft reset.
     */
    private static final int CANCEL_ATTEMPTS = 50;

    private final StatusPollTimer positionPollTimer;
    private final GrblHalFirmwareSettings firmwareSettings;
    private final IOverrideManager overrideManager;
    private final GrblHalCommandLogger commandLogger;
    private GrblHalControllerInitializer initializer;
    private final GrblHalCodes codes;

    private Capabilities capabilities = new Capabilities();
    private ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance()
            .setState(ControllerState.DISCONNECTED)
            .setWorkCoord(Position.ZERO)
            .setMachineCoord(Position.ZERO)
            .build();

    private boolean isCanceling = false;
    private int attemptsRemaining;
    private Position lastLocation;

    /**
     * Stores the single step mode while in check mode so that it can be restored when leaving it.
     */
    private boolean temporaryCheckSingleStepMode = false;

    public GrblHalController() {
        this(new GrblCommunicator());
    }

    public GrblHalController(ICommunicator communicator, GrblHalControllerInitializer controllerInitializer) {
        this(communicator);
        this.initializer = controllerInitializer;
    }

    public GrblHalController(ICommunicator communicator) {
        super(communicator, new GrblHalCommandCreator());
        this.positionPollTimer = new StatusPollTimer(this);
        this.firmwareSettings = new GrblHalFirmwareSettings(this);
        this.initializer = new GrblHalControllerInitializer(this);
        this.overrideManager = new GrblOverrideManager(this, communicator, messageService);
        this.codes = new GrblHalCodes();
        this.commandLogger = new GrblHalCommandLogger(messageService, codes, firmwareSettings);
        addListener(commandLogger);
        new GrblHalFirmwareSettingsInterceptor(this, firmwareSettings);
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public GrblHalFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    /**
     * Returns the error and alarm codes that the controller has reported.
     */
    public GrblHalCodes getCodes() {
        return codes;
    }

    @Override
    public IOverrideManager getOverrideManager() {
        return overrideManager;
    }

    @Override
    public void setMessageService(MessageService messageService) {
        super.setMessageService(messageService);
        overrideManager.setMessageService(messageService);
        commandLogger.setMessageService(messageService);
    }

    @Override
    public Boolean openCommPort(ConnectionDriver connectionDriver, String port, int portRate) throws Exception {
        if (isCommOpen()) {
            throw new Exception("Comm port is already open.");
        }

        initializer.reset();
        positionPollTimer.stop();
        comm.connect(connectionDriver, port, portRate);
        setControllerState(ControllerState.CONNECTING);
        messageService.dispatchMessage(MessageType.INFO, "*** Connecting to " + connectionDriver.getProtocol() + port + ":" + portRate + "\n");

        initialize();
        return isCommOpen();
    }

    private void initialize() {
        if (initializer.isInitializing()) {
            LOGGER.info("Already initializing, skipping");
            return;
        }

        if (comm.areActiveCommands()) {
            messageService.dispatchMessage(MessageType.INFO, "*** Canceling current stream\n");
            cancelCommands();
            resetBuffers();
        }

        setControllerState(ControllerState.CONNECTING);
        if (initializer.isInitialized()) {
            return;
        }

        ThreadHelper.invokeLater(() -> {
            positionPollTimer.stop();
            if (!initializer.initialize()) {
                return;
            }

            capabilities = GrblHalUtils.getCapabilities(initializer.getBuildOptions());
            LOGGER.info("Identified controller capabilities: " + capabilities);

            // Toggle the state to force UI update
            setControllerState(ControllerState.CONNECTING);
            positionPollTimer.start();
        });
    }

    @Override
    protected void rawResponseHandler(String response) {
        try {
            if (GrblUtils.isOkResponse(response)) {
                commandComplete();
            } else if (GrblUtils.isOkErrorAlarmResponse(response)) {
                handleErrorOrAlarmResponse(response);
            } else if (GrblHalUtils.isWelcomeMessage(response)) {
                messageService.dispatchMessage(MessageType.VERBOSE, response + "\n");
                initialize();
            } else if (GrblHalUtils.isProbeMessage(response)) {
                Position position = GrblHalUtils.parseProbePosition(response, getFirmwareSettings().getReportingUnits());
                if (position != null) {
                    dispatchProbeCoordinates(position);
                }
            } else if (initializer.isInitialized() && GrblUtils.isGrblStatusString(response)) {
                // Only one poll is sent at a time so don't decrement, reset to zero.
                positionPollTimer.receivedStatus();
                messageService.dispatchMessage(MessageType.VERBOSE, response + "\n");
                handleStatusString(response);
                checkStreamFinished();
            } else if (initializer.isInitialized() && GrblUtils.isGrblFeedbackMessageV1(response)) {
                handleFeedbackMessage(response);
            }
        } catch (Exception e) {
            String message = Localization.getString("controller.error.response") + " <" + response + ">";
            if (e.getMessage() != null) {
                message = message + ": " + e.getMessage();
            }

            LOGGER.log(Level.SEVERE, message, e);
            dispatchConsoleMessage(MessageType.ERROR, message + "\n");
        }
    }

    private void handleErrorOrAlarmResponse(String response) throws UnexpectedCommand {
        if (GrblUtils.isAlarmResponse(response)) {
            controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                    .setState(ControllerState.ALARM)
                    .build();

            dispatchAlarm(GrblUtils.parseAlarmResponse(response));
            dispatchStatusString(controllerStatus);
        }

        // If there is an active command, mark it as completed
        if (getActiveCommand().isPresent()) {
            commandComplete();
        } else {
            String message = String.format(Localization.getString("controller.exception.unexpectedError"),
                    codes.lookupCode(response)).replaceAll("\\.\\.", ".");
            dispatchConsoleMessage(MessageType.INFO, message + "\n");
        }
        checkStreamFinished();
    }

    private void handleFeedbackMessage(String response) {
        GrblFeedbackMessage feedbackMessage = new GrblFeedbackMessage(response);

        // Convert the feedback message to raw commands to update the modal state
        updateParserModalState(getCommandCreator().createCommand(GrblUtils.parseFeedbackMessageV1(response)));
        dispatchConsoleMessage(MessageType.VERBOSE, feedbackMessage + "\n");
        setDistanceModeCode(feedbackMessage.getDistanceMode());
        setUnitsCode(feedbackMessage.getUnits());
    }

    private void handleStatusString(String status) {
        CommunicatorState before = getCommunicatorState();
        ControllerState beforeState = controllerStatus.getState();

        controllerStatus = GrblUtils.getStatusFromStatusStringV1(controllerStatus, status, getFirmwareSettings().getReportingUnits());

        // While dwelling (G4) the controller reports itself as idle even though the program is still
        // running, keep reporting it as running until every command in the stream has completed.
        if (isIdleWhileStreaming()) {
            controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                    .setState(ControllerState.RUN)
                    .build();
        }

        detectAxisCapabilityFromControllerStatus(Axis.A, CapabilitiesConstants.A_AXIS);
        detectAxisCapabilityFromControllerStatus(Axis.B, CapabilitiesConstants.B_AXIS);
        detectAxisCapabilityFromControllerStatus(Axis.C, CapabilitiesConstants.C_AXIS);

        if (beforeState == ControllerState.JOG && controllerStatus.getState() == ControllerState.IDLE) {
            comm.cancelSend();
        }

        // Set and restore the step mode when transitioning from CHECK mode to IDLE.
        if (before == COMM_CHECK && getCommunicatorState() != COMM_CHECK) {
            setSingleStepMode(temporaryCheckSingleStepMode);
        } else if (before != COMM_CHECK && getCommunicatorState() == COMM_CHECK) {
            temporaryCheckSingleStepMode = getSingleStepMode();
            setSingleStepMode(true);
        }

        updateCancelingState();
        dispatchStatusString(controllerStatus);
    }

    /**
     * When a stream is cancelled the machine is first put on hold, once it has come to a complete
     * stop a soft reset is issued to flush the planner buffer.
     */
    private void updateCancelingState() {
        if (!isCanceling) {
            return;
        }

        if (attemptsRemaining > 0 && lastLocation != null) {
            attemptsRemaining--;
            ControllerState state = controllerStatus.getState();
            if (state == ControllerState.IDLE || state == ControllerState.CHECK) {
                isCanceling = false;
            } else if ((state == ControllerState.HOLD || state == ControllerState.DOOR) && lastLocation.equals(controllerStatus.getMachineCoord())) {
                try {
                    issueSoftReset();
                } catch (Exception e) {
                    dispatchConsoleMessage(MessageType.ERROR, e.getMessage() + "\n");
                }
                isCanceling = false;
            }

            if (isCanceling && attemptsRemaining == 0) {
                dispatchConsoleMessage(MessageType.ERROR, Localization.getString("grbl.exception.cancelReset") + "\n");
            }
        }

        lastLocation = new Position(controllerStatus.getMachineCoord());
    }

    private boolean isIdleWhileStreaming() {
        return controllerStatus.getState() == ControllerState.IDLE &&
                isStreaming() &&
                !comm.isPaused() &&
                !allCommandsInStreamCompleted();
    }

    /**
     * Adds the capability for the given axis if the controller reports coordinates for it.
     *
     * @param axis       the axis to check
     * @param capability the capability to add if found
     */
    private void detectAxisCapabilityFromControllerStatus(Axis axis, String capability) {
        boolean hasAxisCoordinate = (controllerStatus.getMachineCoord() != null && !Double.isNaN(controllerStatus.getMachineCoord().get(axis))) ||
                (controllerStatus.getWorkCoord() != null && !Double.isNaN(controllerStatus.getWorkCoord().get(axis)));

        if (!capabilities.hasAxis(axis) && hasAxisCoordinate) {
            capabilities.addCapability(capability);
        }
    }

    @Override
    protected void pauseStreamingEvent() throws Exception {
        comm.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
    }

    @Override
    protected void resumeStreamingEvent() throws Exception {
        comm.sendByteImmediately(GrblUtils.GRBL_RESUME_COMMAND);
    }

    @Override
    protected void closeCommBeforeEvent() {
        positionPollTimer.stop();
    }

    @Override
    protected void closeCommAfterEvent() {
        initializer.reset();
    }

    @Override
    protected void isReadyToStreamCommandsEvent() throws Exception {
        isReadyToSendCommandsEvent();
        if (controllerStatus.getState() == ControllerState.ALARM) {
            throw new Exception(Localization.getString("grbl.exception.Alarm"));
        }
    }

    @Override
    protected void isReadyToSendCommandsEvent() throws ControllerException {
        if (!isCommOpen()) {
            throw new ControllerException(Localization.getString("controller.exception.booting"));
        }
    }

    @Override
    protected void cancelSendBeforeEvent() throws Exception {
        if (controllerStatus.getState() == ControllerState.JOG) {
            dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_JOG_CANCEL_COMMAND));
            comm.sendByteImmediately(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
        } else if (!isPaused()) {
            pauseStreaming();
        }
    }

    @Override
    protected void cancelSendAfterEvent() {
        if (getStatusUpdatesEnabled()) {
            // Trigger the position listener to watch for the machine to stop
            attemptsRemaining = CANCEL_ATTEMPTS;
            isCanceling = true;
            lastLocation = null;
        }
    }

    @Override
    public void cancelJog() throws Exception {
        dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_JOG_CANCEL_COMMAND));
        comm.sendByteImmediately(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
    }

    @Override
    protected Boolean isIdleEvent() {
        return getCommunicatorState() == COMM_IDLE || getCommunicatorState() == COMM_CHECK;
    }

    @Override
    public CommunicatorState getCommunicatorState() {
        return ControllerUtils.getCommunicatorState(controllerStatus.getState(), this, comm);
    }

    @Override
    public void performHomingCycle() throws Exception {
        if (!isCommOpen()) {
            super.performHomingCycle();
            return;
        }

        sendCommandImmediately(createCommand(GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C));
        controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                .setState(ControllerState.HOME)
                .build();
        dispatchStatusString(controllerStatus);
    }

    @Override
    public void resetCoordinatesToZero() throws Exception {
        if (!isCommOpen()) {
            super.resetCoordinatesToZero();
            return;
        }

        sendCommandImmediately(createCommand(GrblUtils.GCODE_RESET_COORDINATES_TO_ZERO_V9));
    }

    @Override
    public void resetCoordinateToZero(Axis axis) throws Exception {
        if (!isCommOpen()) {
            super.resetCoordinateToZero(axis);
            return;
        }

        setWorkPosition(PartialPosition.from(axis, 0.0, getCurrentGcodeState().getUnits()));
    }

    @Override
    public void setWorkPosition(PartialPosition axisPosition) throws Exception {
        if (!isCommOpen()) {
            throw new Exception("Must be connected to set work position");
        }

        Units currentUnits = getCurrentGcodeState().getUnits();
        String gcode = GrblHalUtils.getSetCoordCommand(axisPosition.getPositionIn(currentUnits));
        if (StringUtils.isNotEmpty(gcode)) {
            sendCommandImmediately(createCommand(gcode));
        }
    }

    @Override
    public void killAlarmLock() throws Exception {
        if (!isCommOpen()) {
            super.killAlarmLock();
            return;
        }

        sendCommandImmediately(createCommand(GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND));
    }

    @Override
    public void openDoor() throws Exception {
        if (!isCommOpen()) {
            throw new ControllerException("Not connected to the controller");
        }

        pauseStreaming();
        dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_DOOR_COMMAND));
        comm.sendByteImmediately(GrblUtils.GRBL_DOOR_COMMAND);
    }

    @Override
    public void toggleCheckMode() throws Exception {
        if (!isCommOpen()) {
            super.toggleCheckMode();
            return;
        }

        sendCommandImmediately(createCommand(GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND));
    }

    @Override
    public void viewParserState() throws Exception {
        if (!isCommOpen()) {
            super.viewParserState();
            return;
        }

        sendCommandImmediately(createCommand(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND));
    }

    @Override
    public void requestStatusReport() throws Exception {
        if (!isCommOpen()) {
            throw new ControllerException("Not connected to the controller");
        }

        comm.sendByteImmediately(GrblUtils.GRBL_STATUS_COMMAND);
    }

    @Override
    public void softReset() throws Exception {
        if (!isCommOpen()) {
            return;
        }

        dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_RESET_COMMAND));
        comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
        comm.cancelSend();
    }

    @Override
    public void jogMachine(PartialPosition distance, double feedRate) throws Exception {
        sendCommandImmediately(createCommand("$J=" + GcodeUtils.generateMoveCommand("G91", feedRate, distance)));
    }

    @Override
    public void jogMachineTo(PartialPosition position, double feedRate) throws Exception {
        sendCommandImmediately(createCommand("$J=" + GcodeUtils.generateMoveToCommand("G90", position, feedRate)));
    }

    @Override
    public ProbeGcodeCommand createProbeCommand(PartialPosition distance, UnitValue feedRate) {
        return new GrblProbeCommand(distance, feedRate);
    }

    @Override
    public String getFirmwareVersion() {
        if (isCommOpen()) {
            return initializer.getVersionString();
        }
        return "<" + Localization.getString("controller.log.notconnected") + ">";
    }

    @Override
    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    @Override
    protected void setControllerState(ControllerState controllerState) {
        controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                .setState(controllerState)
                .build();

        dispatchStatusString(controllerStatus);
    }

    @Override
    public boolean getStatusUpdatesEnabled() {
        return positionPollTimer.isEnabled();
    }

    @Override
    public void setStatusUpdatesEnabled(boolean enabled) {
        positionPollTimer.setEnabled(enabled);
    }

    @Override
    public int getStatusUpdateRate() {
        return positionPollTimer.getUpdateInterval();
    }

    @Override
    public void setStatusUpdateRate(int rate) {
        positionPollTimer.setUpdateInterval(rate);
    }
}
