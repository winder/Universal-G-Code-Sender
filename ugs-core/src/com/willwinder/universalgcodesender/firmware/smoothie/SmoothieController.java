/*
    Copyright 2016-2023 Will Winder

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
package com.willwinder.universalgcodesender.firmware.smoothie;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.StatusPollTimer;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.communicator.SmoothieCommunicator;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.smoothie.commands.GetVersionCommand;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.CommunicatorState;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_DISCONNECTED;
import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_IDLE;

/**
 * Controller implementation for Smoothieware
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class SmoothieController extends AbstractController {

    private final Capabilities capabilities;
    private final IFirmwareSettings firmwareSettings;
    private final StatusPollTimer statusPollTimer;
    private String firmwareVersion = "Unknown";
    private ControllerStatus controllerStatus;
    private boolean isSmoothieReady = false;
    private boolean isReady;

    public SmoothieController() {
        this(new SmoothieCommunicator());
    }

    public SmoothieController(ICommunicator communicator) {
        super(communicator, new SmoothieCommandCreator());
        capabilities = new Capabilities();
        firmwareSettings = new SmoothieFirmwareSettings();
        controllerStatus = new ControllerStatus(ControllerState.DISCONNECTED, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(0, 0, 0, UnitUtils.Units.MM));
        statusPollTimer = new StatusPollTimer(this);
    }

    @Override
    protected Boolean isIdleEvent() {
        return getCommunicatorState() == COMM_IDLE || getCommunicatorState() == COMM_CHECK;
    }

    @Override
    protected void closeCommBeforeEvent() {

    }

    @Override
    protected void closeCommAfterEvent() {

    }

    @Override
    protected void cancelSendBeforeEvent() throws Exception {
        comm.cancelSend();
        comm.sendByteImmediately(SmoothieUtils.RESET_COMMAND);
    }

    @Override
    protected void cancelSendAfterEvent() {
    }

    @Override
    protected void pauseStreamingEvent() throws Exception {
        this.comm.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
    }

    @Override
    protected void resumeStreamingEvent() throws Exception {
        this.comm.sendByteImmediately(GrblUtils.GRBL_RESUME_COMMAND);
    }

    @Override
    public void softReset() throws Exception {
        comm.cancelSend();
        comm.sendByteImmediately(SmoothieUtils.RESET_COMMAND);

        setCurrentState(COMM_DISCONNECTED);
        controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                .setState(ControllerState.DISCONNECTED)
                .build();

        dispatchStatusString(controllerStatus);
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
        if (!this.isReady && !isSmoothieReady) {
            throw new Exception(Localization.getString("controller.exception.booting"));
        }
    }

    @Override
    public void viewParserState() {
        GcodeCommand command = getCommandCreator().createCommand(SmoothieUtils.VIEW_PARSER_STATE_COMMAND);
        comm.queueCommand(command);
        comm.streamCommands();
    }

    @Override
    public void requestStatusReport() throws Exception {
        comm.sendByteImmediately(SmoothieUtils.STATUS_COMMAND);
    }

    @Override
    protected void rawResponseHandler(String response) {
        try {
            if (!isSmoothieReady && response.equalsIgnoreCase("smoothie")) {
                isSmoothieReady = true;
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
                return;
            } else if (isSmoothieReady && !isReady && response.equalsIgnoreCase("ok")) {
                queryVersion();
                return;
            }

            // Clear the ci
            Optional<GcodeCommand> activeCommand = getActiveCommand();
            if (activeCommand.isPresent() && activeCommand.get().isDone()) {
                commandComplete(response);
            }

            if (SmoothieUtils.isStatusResponse(response)) {
                statusPollTimer.receivedStatus();
                handleStatusResponse(response);
                checkStreamFinished();
            } else if (SmoothieUtils.isParserStateResponse(response)) {
                String parserStateCode = StringUtils.substringBetween(response, "[", "]");
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
                updateParserModalState(getCommandCreator().createCommand(parserStateCode));
            } else {
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void queryVersion() {
        ThreadHelper.invokeLater(() -> {
            try {
                GetVersionCommand command = ControllerUtils.sendAndWaitForCompletion(this, new GetVersionCommand());
                firmwareVersion = command.getVersion();
                dispatchConsoleMessage(MessageType.INFO, command.getResponse() + "\n");

                capabilities.addCapability(CapabilitiesConstants.X_AXIS);
                capabilities.addCapability(CapabilitiesConstants.Y_AXIS);
                capabilities.addCapability(CapabilitiesConstants.Z_AXIS);
                capabilities.addCapability(CapabilitiesConstants.JOGGING);
                capabilities.addCapability(CapabilitiesConstants.HOMING);
                capabilities.addCapability(CapabilitiesConstants.RETURN_TO_ZERO);
                controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus).setState(ControllerState.IDLE).build();
                dispatchStatusString(controllerStatus);
                setCurrentState(COMM_IDLE);
                isReady = true;

                viewParserState();
                statusPollTimer.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleStatusResponse(String response) {
        dispatchConsoleMessage(MessageType.VERBOSE, response + "\n");

        UnitUtils.Units currentUnits = getCurrentGcodeState().getUnits();
        controllerStatus = SmoothieUtils.getStatusFromStatusString(controllerStatus, response, currentUnits);
        dispatchStatusString(controllerStatus);
    }

    @Override
    public void sendOverrideCommand(Overrides command) {

    }

    @Override
    public boolean getStatusUpdatesEnabled() {
        return statusPollTimer.isEnabled();
    }

    @Override
    public void setStatusUpdatesEnabled(boolean enabled) {
        statusPollTimer.setEnabled(enabled);
    }

    @Override
    public int getStatusUpdateRate() {
        return statusPollTimer.getUpdateInterval();
    }

    @Override
    public void setStatusUpdateRate(int rate) {
        statusPollTimer.setUpdateInterval(rate);
    }

    @Override
    public void performHomingCycle() throws Exception {
        GcodeCommand command = createCommand(SmoothieUtils.PERFORM_HOMING_CYCLE_COMMAND);
        sendCommandImmediately(command);
        controllerStatus = ControllerStatusBuilder
                .newInstance(controllerStatus)
                .setState(ControllerState.HOME)
                .build();
        dispatchStatusString(controllerStatus);
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public IFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    @Override
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    @Override
    public void setWorkPosition(PartialPosition axisPosition) throws Exception {
        String command = SmoothieUtils.generateSetWorkPositionCommand(controllerStatus, getCurrentGcodeState(), axisPosition);
        sendCommandImmediately(getCommandCreator().createCommand(command));
    }

    @Override
    public void killAlarmLock() throws Exception {
        if (controllerStatus.getState().equals(ControllerState.ALARM)) {
            GcodeCommand command = createCommand(SmoothieUtils.KILL_ALARM_LOCK_COMMAND);
            sendCommandImmediately(command);
        } else {
            throw new IllegalStateException("We may only send the unlock command when in " + ControllerState.ALARM + " state");
        }
    }

    @Override
    public CommunicatorState getCommunicatorState() {
        return ControllerUtils.getCommunicatorState(controllerStatus.getState(), this, comm);
    }

    @Override
    protected void setControllerState(ControllerState controllerState) {
        controllerStatus = ControllerStatusBuilder
                .newInstance(controllerStatus)
                .setState(controllerState)
                .build();
        dispatchStatusString(controllerStatus);
    }
}
