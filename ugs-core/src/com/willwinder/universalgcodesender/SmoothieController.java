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

import com.willwinder.universalgcodesender.firmware.DefaultFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

import java.awt.event.ActionListener;

import static com.willwinder.universalgcodesender.SmoothieUtils.CANCEL_COMMAND;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.*;

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

    private int outstandingPolls = 0;
    private Timer positionPollTimer = null;

    public SmoothieController() {
        this(new SmoothieCommunicator());
    }

    public SmoothieController(ICommunicator communicator) {
        super(communicator);
        capabilities = new Capabilities();
        firmwareSettings = new DefaultFirmwareSettings();
        controllerStatus = new ControllerStatus(StringUtils.EMPTY, ControllerState.UNKNOWN, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(0, 0, 0, UnitUtils.Units.MM));
        commandCreator = new GcodeCommandCreator();
    }

    /**
     * Begin issuing GRBL status request commands.
     */
    private void beginPollingPosition() {
        // Start sending '?' commands if supported and enabled.
        if (this.isReady && this.capabilities != null && this.getStatusUpdatesEnabled()) {
            if (this.positionPollTimer == null) {
                this.positionPollTimer = createPositionPollTimer();
            }

            if (!this.positionPollTimer.isRunning()) {
                this.outstandingPolls = 0;
                this.positionPollTimer.start();
            }
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

    /**
     * Create a timer which will execute GRBL's position polling mechanism.
     */
    private Timer createPositionPollTimer() {
        // Action Listener for GRBL's polling mechanism.
        ActionListener actionListener = actionEvent -> java.awt.EventQueue.invokeLater(() -> {
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
            } catch (Exception ex) {
                dispatchConsoleMessage(MessageType.INFO, Localization.getString("controller.exception.sendingstatus")
                        + " (" + ex.getMessage() + ")\n");
                ex.printStackTrace();
                stopPollingPosition();
            }
        });

        return new Timer(this.getStatusUpdateRate(), actionListener);
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
        comm.sendByteImmediately(CANCEL_COMMAND);
    }

    @Override
    protected void cancelSendAfterEvent() throws Exception {
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
        comm.sendByteImmediately(SmoothieUtils.COMMAND_RESET);

        setCurrentState(UGSEvent.ControlState.COMM_DISCONNECTED);
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
    public void viewParserState() throws Exception {
        comm.queueCommand(new GcodeCommand("$G"));
        comm.streamCommands();
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

                capabilities.addCapability(CapabilitiesConstants.JOGGING);
                capabilities.addCapability(CapabilitiesConstants.RETURN_TO_ZERO);
                controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus).setState(ControllerState.IDLE).build();
                dispatchStatusString(controllerStatus);
                setCurrentState(COMM_IDLE);
                isReady = true;

                viewParserState();
                beginPollingPosition();
                return;
            }

            if(SmoothieUtils.isOkErrorAlarmResponse(response)) {
                dispatchConsoleMessage(MessageType.INFO, response + "\n");
                commandComplete(response);
            } else if(SmoothieUtils.isStatusResponse(response)) {
                outstandingPolls = 0;
                handleStatusResponse(response);
                checkStreamFinished();
            } else if(SmoothieUtils.isParserStateResponse(response)) {
                String parserStateCode = StringUtils.substringBetween(response, "[", "]");
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

        UnitUtils.Units currentUnits = getCurrentGcodeState().units.equals(Code.G20) ? UnitUtils.Units.INCH : UnitUtils.Units.MM;
        controllerStatus = SmoothieUtils.getStatusFromStatusString(controllerStatus, response, currentUnits);
        dispatchStateChange(getControlState());
        dispatchStatusString(controllerStatus);
    }

    @Override
    protected void statusUpdatesEnabledValueChanged(boolean enabled) {

    }

    @Override
    protected void statusUpdatesRateValueChanged(int rate) {

    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {

    }

    @Override
    public Boolean handlesAllStateChangeEvents() {
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
    public void resetCoordinateToZero(final Axis axis) throws Exception {
        // Throw exception
        super.resetCoordinatesToZero();
    }

    @Override
    public void setWorkPosition(PartialPosition axisPosition) throws Exception {
        String command = SmoothieUtils.generateSetWorkPositionCommand(controllerStatus, getCurrentGcodeState(), axisPosition);
        sendCommandImmediately(new GcodeCommand(command));
    }

    @Override
    public void killAlarmLock() throws Exception {
        if(controllerStatus.getState().equals(ControllerState.ALARM)) {
            GcodeCommand command = createCommand(GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND);
            sendCommandImmediately(command);
        } else {
            throw new IllegalStateException("We may only send the unlock command when in " + ControllerState.ALARM + " state");
        }
    }

    @Override
    public UGSEvent.ControlState getControlState() {
        ControllerState state = controllerStatus.getState();
        switch(state) {
            case JOG:
            case RUN:
                return UGSEvent.ControlState.COMM_SENDING;
            case HOLD:
            case DOOR:
                return UGSEvent.ControlState.COMM_SENDING_PAUSED;
            case IDLE:
                if (isStreaming()){
                    return UGSEvent.ControlState.COMM_SENDING_PAUSED;
                } else {
                    return UGSEvent.ControlState.COMM_IDLE;
                }
            case CHECK:
                if (isStreaming() && comm.isPaused()) {
                    return UGSEvent.ControlState.COMM_SENDING_PAUSED;
                } else if (isStreaming() && !comm.isPaused()) {
                    return UGSEvent.ControlState.COMM_SENDING;
                } else {
                    return COMM_CHECK;
                }
            case ALARM:
            default:
                return UGSEvent.ControlState.COMM_IDLE;
        }
    }
}