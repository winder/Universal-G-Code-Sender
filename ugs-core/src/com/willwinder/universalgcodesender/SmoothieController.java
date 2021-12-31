/*
    Copyright 2016-2019 Will Winder

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

import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.smoothie.SmoothieFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.universalgcodesender.model.CommunicatorState.*;

/**
 * Controller implementation for Smoothieware
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class SmoothieController extends AbstractController {

    private final Capabilities capabilities;
    private final IFirmwareSettings firmwareSettings;
    private String firmwareVersion = "Unknown";
    private ControllerStatus controllerStatus;

    private boolean isSmoothieReady = false;
    private boolean isReady;

    private StatusPollTimer statusPollTimer;

    public SmoothieController() {
        this(new SmoothieCommunicator());
    }

    public SmoothieController(ICommunicator communicator) {
        super(communicator);
        capabilities = new Capabilities();
        firmwareSettings = new SmoothieFirmwareSettings();
        controllerStatus = new ControllerStatus(ControllerState.UNKNOWN, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(0, 0, 0, UnitUtils.Units.MM));
        commandCreator = new GcodeCommandCreator();
        statusPollTimer = new StatusPollTimer(this);
    }

    @Override
    protected Boolean isIdleEvent() {
        return getControlState() == COMM_IDLE || getControlState() == COMM_CHECK;
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

        setCurrentState(CommunicatorState.COMM_DISCONNECTED);
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
        comm.queueCommand(new GcodeCommand(SmoothieUtils.VIEW_PARSER_STATE_COMMAND));
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
                comm.queueCommand(new GcodeCommand("version"));
                comm.streamCommands();
                return;
            } else if (isSmoothieReady && !isReady && response.startsWith("Build version:")) {
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
                firmwareVersion = "Smoothie " + StringUtils.substringBetween(response, "Build date:", ",").trim();
                commandComplete(response);

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
                return;
            }

            if(SmoothieUtils.isOkErrorAlarmResponse(response)) {
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
                commandComplete(response);
            } else if(SmoothieUtils.isStatusResponse(response)) {
                statusPollTimer.receivedStatus();
                handleStatusResponse(response);
                checkStreamFinished();
            } else if(SmoothieUtils.isParserStateResponse(response)) {
                String parserStateCode = StringUtils.substringBetween(response, "[", "]");
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
                updateParserModalState(new GcodeCommand(parserStateCode));
            } else {
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleStatusResponse(String response) {
        dispatchConsoleMessage(MessageType.VERBOSE, response + "\n");

        UnitUtils.Units currentUnits = getCurrentGcodeState().getUnits();
        controllerStatus = SmoothieUtils.getStatusFromStatusString(controllerStatus, response, currentUnits);
        dispatchStateChange(getControlState());
        dispatchStatusString(controllerStatus);
    }

    @Override
    protected void statusUpdatesEnabledValueChanged() {
        if (getStatusUpdatesEnabled()) {
            statusPollTimer.stop();
            statusPollTimer.start();
        } else {
            statusPollTimer.stop();
        }
    }

    @Override
    protected void statusUpdatesRateValueChanged() {
        statusPollTimer.stop();
        statusPollTimer.start();
    }

    @Override
    public void sendOverrideCommand(Overrides command) {

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
    public boolean handlesAllStateChangeEvents() {
        return false;
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
        sendCommandImmediately(new GcodeCommand(command));
    }

    @Override
    public void killAlarmLock() throws Exception {
        if(controllerStatus.getState().equals(ControllerState.ALARM)) {
            GcodeCommand command = createCommand(SmoothieUtils.KILL_ALARM_LOCK_COMMAND);
            sendCommandImmediately(command);
        } else {
            throw new IllegalStateException("We may only send the unlock command when in " + ControllerState.ALARM + " state");
        }
    }

    @Override
    public CommunicatorState getControlState() {
        ControllerState state = controllerStatus.getState();
        switch(state) {
            case JOG:
            case RUN:
                return CommunicatorState.COMM_SENDING;
            case HOLD:
            case DOOR:
                return CommunicatorState.COMM_SENDING_PAUSED;
            case IDLE:
                if (isStreaming()){
                    return CommunicatorState.COMM_SENDING_PAUSED;
                } else {
                    return CommunicatorState.COMM_IDLE;
                }
            case CHECK:
                if (isStreaming() && comm.isPaused()) {
                    return CommunicatorState.COMM_SENDING_PAUSED;
                } else if (isStreaming() && !comm.isPaused()) {
                    return CommunicatorState.COMM_SENDING;
                } else {
                    return COMM_CHECK;
                }
            case ALARM:
            default:
                return CommunicatorState.COMM_IDLE;
        }
    }
}
