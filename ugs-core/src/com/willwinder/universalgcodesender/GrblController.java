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

import com.willwinder.universalgcodesender.communicator.GrblCommunicator;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.grbl.GrblCommandCreator;
import com.willwinder.universalgcodesender.firmware.grbl.GrblFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.CommunicatorState;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.GrblFeedbackMessage;
import com.willwinder.universalgcodesender.types.GrblSettingMessage;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import com.willwinder.universalgcodesender.utils.GrblLookups;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_IDLE;

/**
 * GRBL Control layer, coordinates all aspects of control.
 *
 * @author wwinder
 */
public class GrblController extends AbstractController {
    private static final Logger logger = Logger.getLogger(GrblController.class.getName());
    private static final GrblLookups ALARMS = new GrblLookups("alarm_codes");
    private static final GrblLookups ERRORS = new GrblLookups("error_codes");
    private final StatusPollTimer positionPollTimer;
    private final GrblFirmwareSettings firmwareSettings;
    private GrblControllerInitializer initializer;
    private Capabilities capabilities = new Capabilities();
    // Polling state
    private ControllerStatus controllerStatus = new ControllerStatus(ControllerState.DISCONNECTED, new Position(0, 0, 0, Units.MM), new Position(0, 0, 0, Units.MM));
    // Canceling state
    private Boolean isCanceling = false;     // Set for the position polling thread.
    private int attemptsRemaining;
    private Position lastLocation;
    /**
     * For storing a temporary state if using single step mode when entering the state
     * check mode. When leaving check mode the temporary single step mode will be reverted.
     */
    private boolean temporaryCheckSingleStepMode = false;

    public GrblController(ICommunicator communicator, GrblControllerInitializer controllerInitializer) {
        this(communicator);
        this.initializer = controllerInitializer;
    }

    public GrblController(ICommunicator communicator) {
        super(communicator, new GrblCommandCreator());
        this.positionPollTimer = new StatusPollTimer(this);
        this.firmwareSettings = new GrblFirmwareSettings(this);
        this.comm.addListener(firmwareSettings);
        this.initializer = new GrblControllerInitializer(this, messageService);
    }

    public GrblController() {
        this(new GrblCommunicator());
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public IFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    /***********************
     * API Implementation. *
     ***********************/

    private static String lookupCode(String input) {
        if (input.contains(":")) {
            String[] inputParts = input.split(":");
            if (inputParts.length == 2) {
                String code = inputParts[1].trim();
                if (StringUtils.isNumeric(code)) {
                    String[] lookupParts = null;
                    switch (inputParts[0].toLowerCase()) {
                        case "error":
                            lookupParts = ERRORS.lookup(code);
                            break;
                        case "alarm":
                            lookupParts = ALARMS.lookup(code);
                            break;
                        default:
                            return input;
                    }

                    if (lookupParts == null) {
                        return "(" + input + ") An unknown error has occurred";
                    } else {
                        return "(" + input + ") " + lookupParts[2];
                    }
                }
            }
        }

        return input;
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
        if (comm.areActiveCommands()) {
            comm.cancelSend();
        }
        setControllerState(ControllerState.CONNECTING);

        ThreadHelper.invokeLater(() -> {
            positionPollTimer.stop();
            initializer.initialize();
            capabilities = GrblUtils.getGrblStatusCapabilities(initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
            positionPollTimer.start();
        });
    }

    @Override
    protected void rawResponseHandler(String response) {
        String processed = response;
        try {
            boolean verbose = false;

            if (GrblUtils.isOkResponse(response)) {
                this.commandComplete(processed);
            }

            // Error case.
            else if (GrblUtils.isOkErrorAlarmResponse(response)) {
                if (GrblUtils.isAlarmResponse(response)) {
                    //this is not updating the state to Alarm in the GUI, and the alarm is no longer being processed
                    controllerStatus = ControllerStatusBuilder
                            .newInstance(controllerStatus)
                            .setState(ControllerState.ALARM)
                            .build();

                    Alarm alarm = GrblUtils.parseAlarmResponse(response);
                    dispatchAlarm(alarm);
                    dispatchStatusString(controllerStatus);
                }

                // If there is an active command, mark it as completed with error
                Optional<GcodeCommand> activeCommand = this.getActiveCommand();
                if (activeCommand.isPresent()) {
                    String commandString = activeCommand.get().getCommandString();
                    processed = String.format(Localization.getString("controller.exception.sendError"), commandString,
                            lookupCode(response)).replaceAll("\\.\\.", "\\.");
                    if (!commandString.startsWith("$J=")) {
                        // log error to console (unless it's in response to a jog command)
                        this.dispatchConsoleMessage(MessageType.ERROR, processed + "\n");
                    }
                    this.commandComplete(processed);
                } else {
                    processed =
                            String.format(Localization.getString("controller.exception.unexpectedError"),
                                    lookupCode(response)).replaceAll("\\.\\.", "\\.");
                    dispatchConsoleMessage(MessageType.INFO, processed + "\n");
                }
                checkStreamFinished();
                processed = "";
            } else if (GrblUtils.isGrblVersionString(response)) {
                messageService.dispatchMessage(MessageType.VERBOSE, response + "\n");
                initialize();
            } else if (GrblUtils.isGrblProbeMessage(response)) {
                Position p = GrblUtils.parseProbePosition(response, getFirmwareSettings().getReportingUnits());
                if (p != null) {
                    dispatchProbeCoordinates(p);
                }
            }

            else if (GrblUtils.isGrblStatusString(response)) {
                // Only 1 poll is sent at a time so don't decrement, reset to zero.
                positionPollTimer.receivedStatus();

                // Status string goes to verbose console
                verbose = true;

                this.handleStatusString(response);
                this.checkStreamFinished();
            }

            else if (GrblUtils.isGrblFeedbackMessage(response, capabilities)) {
                GrblFeedbackMessage grblFeedbackMessage = new GrblFeedbackMessage(response);
                // Convert feedback message to raw commands to update modal state.
                updateParserModalState(getCommandCreator().createCommand(GrblUtils.parseFeedbackMessage(response, capabilities)));
                dispatchConsoleMessage(MessageType.VERBOSE, grblFeedbackMessage + "\n");
                setDistanceModeCode(grblFeedbackMessage.getDistanceMode());
                setUnitsCode(grblFeedbackMessage.getUnits());
            }

            else if (GrblUtils.isGrblSettingMessage(response)) {
                GrblSettingMessage message = new GrblSettingMessage(response);
                processed = message.toString();
            }

            if (StringUtils.isNotBlank(processed)) {
                if (verbose) {
                    this.dispatchConsoleMessage(MessageType.VERBOSE, processed + "\n");
                } else {
                    this.dispatchConsoleMessage(MessageType.INFO, processed + "\n");
                }
            }
        } catch (Exception e) {
            String message = "";
            if (e.getMessage() != null) {
                message = ": " + e.getMessage();
            }
            message = Localization.getString("controller.error.response")
                    + " <" + processed + ">" + message;

            logger.log(Level.SEVERE, message, e);
            this.dispatchConsoleMessage(MessageType.ERROR, message + "\n");
        }
    }

    @Override
    protected void pauseStreamingEvent() throws Exception {
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
        }
    }

    @Override
    protected void resumeStreamingEvent() throws Exception {
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_RESUME_COMMAND);
        }
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
        if (this.controllerStatus != null && this.controllerStatus.getState() == ControllerState.ALARM) {
            throw new Exception(Localization.getString("grbl.exception.Alarm"));
        }
    }

    @Override
    protected void isReadyToSendCommandsEvent() throws Exception {
        if (!isCommOpen()) {
            throw new Exception(Localization.getString("controller.exception.booting"));
        }
    }

    @Override
    protected void cancelSendBeforeEvent() throws Exception {
        boolean paused = isPaused();
        // The cancel button is left enabled at all times now, but can only be
        // used for some versions of GRBL.
        if (paused && !this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            throw new Exception("Cannot cancel while paused with this version of GRBL. Reconnect to reset GRBL.");
        }

        // If we're canceling a "jog" state
        if (capabilities.hasJogging() && controllerStatus != null &&
                controllerStatus.getState() == ControllerState.JOG) {
            dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_JOG_CANCEL_COMMAND));
            comm.sendByteImmediately(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
        }
        // Otherwise, check if we can get fancy with a soft reset.
        else if (!paused && this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            try {
                this.pauseStreaming();
            } catch (Exception e) {
                // Oh well, was worth a shot.
                System.out.println("Exception while trying to issue a soft reset: " + e.getMessage());
            }
        }
    }

    @Override
    protected void cancelSendAfterEvent() {
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME) && this.getStatusUpdatesEnabled()) {
            // Trigger the position listener to watch for the machine to stop.
            this.attemptsRemaining = 50;
            this.isCanceling = true;
            this.lastLocation = null;
        }
    }

    @Override
    public void cancelJog() throws Exception {
        if (capabilities.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)) {
            dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_JOG_CANCEL_COMMAND));
            comm.sendByteImmediately(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
        } else {
            cancelSend();
        }
    }

    @Override
    protected Boolean isIdleEvent() {
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            return getCommunicatorState() == COMM_IDLE || getCommunicatorState() == COMM_CHECK;
        }
        // Otherwise let the abstract controller decide.
        return true;
    }

    @Override
    public CommunicatorState getCommunicatorState() {
        if (!this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            return super.getCommunicatorState();
        }

        return ControllerUtils.getCommunicatorState(controllerStatus.getState(), this, comm);
    }

    /**
     * Sends the version specific homing cycle to the machine.
     */
    @Override
    public void performHomingCycle() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getHomingCommand(initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                sendCommandImmediately(command);
                controllerStatus = ControllerStatusBuilder
                        .newInstance(controllerStatus)
                        .setState(ControllerState.HOME)
                        .build();
                dispatchStatusString(controllerStatus);
                return;
            }
        }
        // Throw exception
        super.performHomingCycle();
    }

    @Override
    public void resetCoordinatesToZero() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getResetCoordsToZeroCommand(initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.resetCoordinatesToZero();
    }

    @Override
    public void resetCoordinateToZero(final Axis axis) throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getResetCoordToZeroCommand(axis, getCurrentGcodeState().getUnits(), initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.resetCoordinatesToZero();
    }

    @Override
    public void setWorkPosition(PartialPosition axisPosition) throws Exception {
        if (!this.isCommOpen()) {
            throw new Exception("Must be connected to set work position");
        }

        Units currentUnits = getCurrentGcodeState().getUnits();
        PartialPosition position = axisPosition.getPositionIn(currentUnits);
        String gcode = GrblUtils.getSetCoordCommand(position, initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
        if (StringUtils.isNotEmpty(gcode)) {
            GcodeCommand command = createCommand(gcode);
            this.sendCommandImmediately(command);
        }
    }

    @Override
    public void killAlarmLock() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getKillAlarmLockCommand(initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.killAlarmLock();
    }

    @Override
    public void openDoor() throws Exception {
        if (!this.isCommOpen()) {
            throw new RuntimeException("Not connected to the controller");
        }

        pauseStreaming(); // Pause the file stream and stop the time
        dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_DOOR_COMMAND));
        comm.sendByteImmediately(GrblUtils.GRBL_DOOR_COMMAND);
    }

    @Override
    public void toggleCheckMode() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getToggleCheckModeCommand(initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.toggleCheckMode();
    }

    @Override
    public void viewParserState() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getViewParserStateCommand(initializer.getVersion().getVersionNumber(), initializer.getVersion().getVersionLetter());
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.viewParserState();
    }

    @Override
    public void requestStatusReport() throws Exception {
        if (!this.isCommOpen()) {
            throw new RuntimeException("Not connected to the controller");
        }

        comm.sendByteImmediately(GrblUtils.GRBL_STATUS_COMMAND);
    }

    /**
     * If it is supported, a soft reset real-time command will be issued.
     */
    @Override
    public void softReset() throws Exception {
        if (isCommOpen() && capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            dispatchConsoleMessage(MessageType.VERBOSE, String.format(">>> 0x%02x\n", GrblUtils.GRBL_RESET_COMMAND));
            comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
            //Does GRBL need more time to handle the reset?
            comm.cancelSend();
        }
    }

    @Override
    public void jogMachine(PartialPosition distance, double feedRate) throws Exception {
        if (capabilities.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)) {
            String commandString = GcodeUtils.generateMoveCommand("G91", feedRate, distance);
            GcodeCommand command = createCommand("$J=" + commandString);
            sendCommandImmediately(command);
        } else {
            super.jogMachine(distance, feedRate);
        }
    }

    @Override
    public void jogMachineTo(PartialPosition position, double feedRate) throws Exception {
        if (capabilities.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)) {
            String commandString = GcodeUtils.generateMoveToCommand("G90", position, feedRate);
            GcodeCommand command = createCommand("$J=" + commandString);
            sendCommandImmediately(command);
        } else {
            super.jogMachineTo(position, feedRate);
        }
    }

    /************
     * Helpers.
     ************/

    public String getGrblVersion() {
        if (this.isCommOpen()) {
            return initializer.getVersion().toString();
        }
        return "<" + Localization.getString("controller.log.notconnected") + ">";
    }

    @Override
    public String getFirmwareVersion() {
        return getGrblVersion();
    }

    @Override
    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    // No longer a listener event
    private void handleStatusString(final String string) {
        if (this.capabilities == null) {
            return;
        }

        CommunicatorState before = getCommunicatorState();
        ControllerState beforeState = controllerStatus == null ? ControllerState.DISCONNECTED : controllerStatus.getState();

        controllerStatus = GrblUtils.getStatusFromStatusString(
                controllerStatus, string, capabilities, getFirmwareSettings().getReportingUnits());

        // Add extra axis capabilities if the status report contains ABC axes
        detectAxisCapabilityFromControllerStatus(Axis.A, CapabilitiesConstants.A_AXIS);
        detectAxisCapabilityFromControllerStatus(Axis.B, CapabilitiesConstants.B_AXIS);
        detectAxisCapabilityFromControllerStatus(Axis.C, CapabilitiesConstants.C_AXIS);

        // GRBL 1.1 jog complete transition
        if (beforeState == ControllerState.JOG && controllerStatus.getState() == ControllerState.IDLE) {
            this.comm.cancelSend();
        }

        // Set and restore the step mode when transitioning from CHECK mode to IDLE.
        if (before == COMM_CHECK && getCommunicatorState() != COMM_CHECK) {
            setSingleStepMode(temporaryCheckSingleStepMode);
        } else if (before != COMM_CHECK && getCommunicatorState() == COMM_CHECK) {
            temporaryCheckSingleStepMode = getSingleStepMode();
            setSingleStepMode(true);
        }

        // Prior to GRBL v1.1 the GUI is required to keep checking locations
        // to verify that the machine has come to a complete stop after
        // pausing.
        if (isCanceling) {
            if (attemptsRemaining > 0 && lastLocation != null) {
                attemptsRemaining--;
                // If the machine goes into idle, we no longer need to cancel.
                if (controllerStatus.getState() == ControllerState.IDLE || controllerStatus.getState() == ControllerState.CHECK) {
                    isCanceling = false;
                }
                // Otherwise check if the machine is Hold/Queue and stopped.
                else if ((controllerStatus.getState() == ControllerState.HOLD || controllerStatus.getState() == ControllerState.DOOR) && lastLocation.equals(this.controllerStatus.getMachineCoord())) {
                    try {
                        this.issueSoftReset();
                    } catch (Exception e) {
                        this.dispatchConsoleMessage(MessageType.ERROR, e.getMessage() + "\n");
                    }
                    isCanceling = false;
                }
                if (isCanceling && attemptsRemaining == 0) {
                    this.dispatchConsoleMessage(MessageType.ERROR, Localization.getString("grbl.exception.cancelReset") + "\n");
                }
            }
            lastLocation = new Position(this.controllerStatus.getMachineCoord());
        }

        dispatchStatusString(controllerStatus);
    }

    /**
     * Checks the controller status machine and work coordinate if they contain coordinates for the given axis.
     * If found the capability for that axis will be added.
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
    protected void setControllerState(ControllerState controllerState) {
        controllerStatus = ControllerStatusBuilder
                .newInstance(controllerStatus)
                .setState(controllerState)
                .build();

        dispatchStatusString(controllerStatus);
    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {
        Byte realTimeCommand = GrblUtils.getOverrideForEnum(command, capabilities);
        if (realTimeCommand != null) {
            this.dispatchConsoleMessage(MessageType.INFO, String.format(">>> 0x%02x\n", realTimeCommand));
            this.comm.sendByteImmediately(realTimeCommand);
        }
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
