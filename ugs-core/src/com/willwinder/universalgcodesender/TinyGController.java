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

import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.communicator.TinyGCommunicator;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.tinyg.TinyGFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.ICommandCreator;
import com.willwinder.universalgcodesender.firmware.tinyg.TinyGGcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.firmware.tinyg.commands.TinyGGcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_IDLE;

/**
 * TinyG Control layer, coordinates all aspects of control.
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class TinyGController extends AbstractController {
    private static final Logger LOGGER = Logger.getLogger(TinyGController.class.getSimpleName());
    private static final String NOT_SUPPORTED_YET = "Not supported yet.";
    private static final String STATUS_REPORT_CONFIG = "{sr:{posx:t, posy:t, posz:t, mpox:t, mpoy:t, mpoz:t, plan:t, vel:t, unit:t, stat:t, dist:t, frmo:t, coor:t}}";
    private static final double LATEST_TINYG_FIRMWARE_VERSION = 0.97;

    protected final Capabilities capabilities;
    private final TinyGFirmwareSettings firmwareSettings;
    protected ControllerStatus controllerStatus;
    protected String firmwareVersion;
    protected double firmwareVersionNumber;
    private int statusUpdateRate;

    public TinyGController() {
        this(new TinyGCommunicator());
    }

    public TinyGController(ICommunicator communicator) {
        super(communicator, new TinyGGcodeCommandCreator());
        capabilities = new Capabilities();

        firmwareSettings = new TinyGFirmwareSettings(this);
        communicator.addListener(firmwareSettings);

        controllerStatus = new ControllerStatus(ControllerState.DISCONNECTED, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(0, 0, 0, UnitUtils.Units.MM));
        firmwareVersion = "TinyG unknown version";
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
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public IFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    @Override
    protected void closeCommBeforeEvent() {
        // Not needed yet
    }

    @Override
    protected void closeCommAfterEvent() {
        // Not needed yet
    }

    @Override
    protected void openCommAfterEvent() {
        try {
            this.comm.sendByteImmediately(TinyGUtils.COMMAND_RESET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void cancelSendBeforeEvent() throws Exception {
        pauseStreaming();
    }

    @Override
    public void jogMachine(PartialPosition distance, double feedRate) throws Exception {
        // Fetch the current coordinate units in which the machine is running
        UnitUtils.Units targetUnits = getCurrentGcodeState().getUnits();

        // We need to convert to these units as we can not change the units in one command in TinyG
        double scale = UnitUtils.scaleUnits(distance.getUnits(), targetUnits);
        String commandString = GcodeUtils.generateMoveCommand("G91G1", feedRate * scale, distance.getPositionIn(targetUnits));

        GcodeCommand command = createCommand(commandString);
        command.setTemporaryParserModalChange(true);
        sendCommandImmediately(command);
        restoreParserModalState();
    }

    @Override
    public void jogMachineTo(final PartialPosition position, final double feedRate) throws Exception {
        // Fetch the current coordinate units in which the machine is running
        UnitUtils.Units targetUnits = getCurrentGcodeState().getUnits();

        // We need to convert to these units as we can not change the units in one command in TinyG
        double scale = UnitUtils.scaleUnits(position.getUnits(), targetUnits);
        PartialPosition positionInTargetUnits = position.getPositionIn(targetUnits);
        String commandString = GcodeUtils.generateMoveToCommand("G90G1", positionInTargetUnits, feedRate * scale);

        GcodeCommand command = createCommand(commandString);
        command.setTemporaryParserModalChange(true);
        sendCommandImmediately(command);
        restoreParserModalState();
    }

    @Override
    protected void cancelSendAfterEvent() throws Exception {
        // Canceling the job on the controller (which will also flush the buffer)
        comm.sendByteImmediately(TinyGUtils.COMMAND_PAUSE);
        comm.sendByteImmediately(TinyGUtils.COMMAND_QUEUE_FLUSH);

        // Work around for clearing the sent buffer size
        comm.cancelSend();
    }

    @Override
    protected void pauseStreamingEvent() throws Exception {
        comm.sendByteImmediately(TinyGUtils.COMMAND_PAUSE);
    }

    @Override
    protected void resumeStreamingEvent() throws Exception {
        comm.sendByteImmediately(TinyGUtils.COMMAND_RESUME);
    }

    @Override
    protected Boolean isIdleEvent() {
        return getCommunicatorState() == COMM_IDLE || getCommunicatorState() == COMM_CHECK;
    }

    @Override
    protected void rawResponseHandler(String response) {
        JsonObject jo;

        try {
            jo = TinyGUtils.jsonToObject(response);
        } catch (Exception ignored) {
            // Some TinyG responses aren't JSON, those will end up here.
            this.dispatchConsoleMessage(MessageType.VERBOSE, response + "\n");
            return;
        }

        if (TinyGUtils.isRestartingResponse(jo)) {
            this.dispatchConsoleMessage(MessageType.INFO, "[restarting] " + response + "\n");
        } else if (TinyGUtils.isReadyResponse(jo)) {
            handleReadyResponse(response, jo);
        } else if (jo.has("ack")) {
            // TODO what do we do with ack=false, or if we don't get any response at all?
            dispatchConsoleMessage(MessageType.INFO, "[ack] " + response + "\n");
            sendInitCommands();
        } else if (TinyGUtils.isStatusResponse(jo)) {
            updateControllerStatus(jo);
            dispatchConsoleMessage(MessageType.INFO, response + "\n");
            checkStreamFinished();
        } else if (TinyGGcodeCommand.isOkErrorResponse(response)) {
            if (jo.get("r").getAsJsonObject().has(TinyGUtils.FIELD_STATUS_REPORT)) {
                updateControllerStatus(jo.get("r").getAsJsonObject());
            }

            if (getActiveCommand().isPresent()) {
                try {
                    commandComplete(response);
                } catch (Exception e) {
                    this.dispatchConsoleMessage(MessageType.ERROR, Localization.getString("controller.error.response")
                            + " <" + response + ">: " + e.getMessage());
                }
            }

            this.dispatchConsoleMessage(MessageType.INFO, response + "\n");
        } else if (TinyGGcodeCommand.isQueueReportResponse(response)) {
            LOGGER.log(Level.FINE, "Queue buffer usage: " + jo.get("qr").getAsString());
        } else if (TinyGGcodeCommand.isRecieveQueueReportResponse(response)) {
            LOGGER.log(Level.FINE, "Receive queue buffer usage: " + jo.get("rx").getAsString());
        } else {
            // Display any unhandled messages
            this.dispatchConsoleMessage(MessageType.INFO, "[unhandled message] " + response + "\n");
        }
    }

    protected void handleReadyResponse(String response, JsonObject jo) {
        if (TinyGUtils.isTinyGVersion(jo)) {
            firmwareVersionNumber = TinyGUtils.getVersion(jo);
            firmwareVersion = "TinyG " + firmwareVersionNumber;
        }

        if (firmwareVersionNumber > LATEST_TINYG_FIRMWARE_VERSION) {
            dispatchConsoleMessage(MessageType.ERROR, String.format(Localization.getString("tinyg.exception.unknownVersion"), firmwareVersionNumber)  + "\n");
            return;
        }

        capabilities.addCapability(CapabilitiesConstants.X_AXIS);
        capabilities.addCapability(CapabilitiesConstants.Y_AXIS);
        capabilities.addCapability(CapabilitiesConstants.Z_AXIS);
        capabilities.addCapability(CapabilitiesConstants.RETURN_TO_ZERO);
        capabilities.addCapability(CapabilitiesConstants.JOGGING);
        capabilities.removeCapability(CapabilitiesConstants.CONTINUOUS_JOGGING);
        capabilities.addCapability(CapabilitiesConstants.HOMING);
        capabilities.addCapability(CapabilitiesConstants.FIRMWARE_SETTINGS);
        capabilities.removeCapability(CapabilitiesConstants.OVERRIDES);
        capabilities.removeCapability(CapabilitiesConstants.SETUP_WIZARD);

        setCurrentState(COMM_IDLE);
        dispatchConsoleMessage(MessageType.INFO, "[ready] " + response + "\n");
        sendInitCommands();
    }

    private void updateControllerStatus(JsonObject jo) {
        // Save the old state
        ControllerState previousState = controllerStatus.getState();
        CommunicatorState previousControlState = getControlState(previousState);

        // Update the internal state
        List<String> gcodeList = TinyGUtils.convertStatusReportToGcode(jo);
        gcodeList.forEach(gcode -> updateParserModalState(getCommandCreator().createCommand(gcode)));

        // Notify our listeners about the new status
        controllerStatus = parseControllerStatus(jo);
        dispatchStatusString(controllerStatus);

        // Notify state change to our listeners
        CommunicatorState newControlState = getControlState(controllerStatus.getState());
        if (!previousControlState.equals(newControlState)) {
            LOGGER.log(Level.FINE, "Changing state from " + previousControlState + " to " + newControlState);
            setCurrentState(newControlState);
        }
    }

    /**
     * Parse the controller status response and return the current controller status
     * @param jo a json object with the controller status
     * @return the new current controller status
     */
    protected ControllerStatus parseControllerStatus(JsonObject jo) {
        return TinyGUtils.updateControllerStatus(controllerStatus, jo);
    }

    protected void sendInitCommands() {
        ICommandCreator commandCreator = getCommandCreator();
        // Enable JSON mode
        // 0=text mode, 1=JSON mode
        comm.queueCommand(commandCreator.createCommand("{ej:1}"));

        // Configure status reports
        comm.queueCommand(commandCreator.createCommand(STATUS_REPORT_CONFIG));

        // JSON verbosity
        // 0=silent, 1=footer, 2=messages, 3=configs, 4=linenum, 5=verbose
        comm.queueCommand(commandCreator.createCommand("{jv:4}"));

        // Queue report verbosity
        // 0=off, 1=filtered, 2=verbose
        comm.queueCommand(commandCreator.createCommand("{qv:0}"));

        // Status report verbosity
        // 0=off, 1=filtered, 2=verbose
        comm.queueCommand(commandCreator.createCommand("{sv:1}"));

        // Request initial status report
        comm.queueCommand(commandCreator.createCommand("{sr:n}"));

        comm.streamCommands();

        // Refresh the status update
        setStatusUpdateRate(getStatusUpdateRate());
    }

    @Override
    public void updateParserModalState(GcodeCommand command) {
        // Prevent internal TinyG commands to update the parser modal state
        if (!command.getCommandString().startsWith("{")) {
            super.updateParserModalState(command);
        }
    }

    @Override
    public void performHomingCycle() throws Exception {
        sendCommandImmediately(getCommandCreator().createCommand("G28.2 Z0 X0 Y0"));
    }

    @Override
    public void resetCoordinatesToZero() throws Exception {
        String command = TinyGUtils.generateResetCoordinatesToZeroCommand(controllerStatus, getCurrentGcodeState());
        sendCommandImmediately(getCommandCreator().createCommand(command));
    }

    @Override
    public void killAlarmLock() throws Exception {
        sendCommandImmediately(getCommandCreator().createCommand(TinyGUtils.COMMAND_KILL_ALARM_LOCK));
    }

    @Override
    public void toggleCheckMode() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void viewParserState() throws Exception {
        if (this.isCommOpen()) {
            sendCommandImmediately(getCommandCreator().createCommand(TinyGUtils.COMMAND_STATUS_REPORT));
        }
    }

    @Override
    public void requestStatusReport() throws Exception {
        viewParserState();
    }

    @Override
    public void softReset() throws Exception {
        comm.cancelSend();
        comm.sendByteImmediately(TinyGUtils.COMMAND_RESET);

        setCurrentState(CommunicatorState.COMM_DISCONNECTED);
        controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                .setState(ControllerState.CONNECTING)
                .build();

        dispatchStatusString(controllerStatus);
    }


    @Override
    public void setWorkPosition(PartialPosition axisPosition) throws Exception {
        String command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, getCurrentGcodeState(), axisPosition);
        sendCommandImmediately(getCommandCreator().createCommand(command));
    }

    @Override
    protected void isReadyToStreamCommandsEvent() {
        // Not needed yet
    }

    @Override
    protected void isReadyToSendCommandsEvent() {
        // Not needed yet
    }

    @Override
    public boolean getStatusUpdatesEnabled() {
        return false;
    }

    @Override
    public void setStatusUpdatesEnabled(boolean enabled) {
    }

    @Override
    public int getStatusUpdateRate() {
        return statusUpdateRate;
    }

    @Override
    public void setStatusUpdateRate(int statusUpdateRate) {
        this.statusUpdateRate = statusUpdateRate;
        comm.queueCommand(getCommandCreator().createCommand("{si:" + getStatusUpdateRate() + "}"));
    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {
        ControllerStatus.OverridePercents currentOverrides = controllerStatus.getOverrides();
        Optional<GcodeCommand> gcodeCommand = TinyGUtils.createOverrideCommand(getCommandCreator(), currentOverrides, command);
        if (gcodeCommand.isPresent()) {
            sendCommandImmediately(gcodeCommand.get());
	    }
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
    public CommunicatorState getCommunicatorState() {
        return getControlState(getControllerStatus().getState());
    }

    protected CommunicatorState getControlState(ControllerState controllerState) {
        return ControllerUtils.getCommunicatorState(controllerState, this, comm);
    }
}
