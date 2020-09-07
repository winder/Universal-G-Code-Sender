/*
    Copyright 2019 Will Winder

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
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;

/**
 * G2Core Control layer.
 *
 * @author Joacim Breiler
 */
public class G2CoreController extends TinyGController {

    private static final String STATUS_REPORT_CONFIG = "{sr:{posx:t, posy:t, posz:t, mpox:t, mpoy:t, mpoz:t, plan:t, vel:t, unit:t, stat:t, dist:t, admo:t, frmo:t, coor:t}}";

    public G2CoreController() {
        super();
    }

    public G2CoreController(ICommunicator communicator) {
        super(communicator);
    }

    @Override
    protected void openCommAfterEvent() {
    }

    @Override
    public void softReset() throws Exception {
        this.comm.sendByteImmediately(TinyGUtils.COMMAND_KILL_JOB);
        this.comm.sendByteImmediately(TinyGUtils.COMMAND_QUEUE_FLUSH);
        this.comm.sendByteImmediately((byte) '\n');

        setCurrentState(UGSEvent.ControlState.COMM_DISCONNECTED);
        controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                .setState(ControllerState.DISCONNECTED)
                .build();

        dispatchStatusString(controllerStatus);

        sendInitCommands();
    }

    protected void handleReadyResponse(String response, JsonObject jo) {
        if (TinyGUtils.isTinyGVersion(jo)) {
            firmwareVersionNumber = TinyGUtils.getVersion(jo);
            firmwareVersion = "G2Core " + firmwareVersionNumber;
        }

        capabilities.addCapability(CapabilitiesConstants.RETURN_TO_ZERO);
        capabilities.addCapability(CapabilitiesConstants.JOGGING);
        capabilities.addCapability(CapabilitiesConstants.CONTINUOUS_JOGGING);
        capabilities.addCapability(CapabilitiesConstants.HOMING);
        capabilities.addCapability(CapabilitiesConstants.FIRMWARE_SETTINGS);
        capabilities.addCapability(CapabilitiesConstants.OVERRIDES);
        capabilities.removeCapability(CapabilitiesConstants.SETUP_WIZARD);

        setCurrentState(COMM_IDLE);
        dispatchConsoleMessage(MessageType.INFO, "[ready] " + response + "\n");

        try {
            comm.sendByteImmediately(TinyGUtils.COMMAND_ENQUIRE_STATUS);
        } catch (Exception e) {
            dispatchConsoleMessage(MessageType.ERROR, "Couldn't enquire for controller status\n");
        }
    }

    protected void sendInitCommands() {
        // Enable JSON mode
        // 0=text mode, 1=JSON mode
        comm.queueCommand(new GcodeCommand("{ej:1}"));

        // Configure status reports
        comm.queueCommand(new GcodeCommand(STATUS_REPORT_CONFIG));

        // JSON verbosity
        // 0=silent, 1=footer, 2=messages, 3=configs, 4=linenum, 5=verbose
        comm.queueCommand(new GcodeCommand("{jv:4}"));

        // Queue report verbosity
        // 0=off, 1=filtered, 2=verbose
        comm.queueCommand(new GcodeCommand("{qv:0}"));

        // Status report verbosity
        // 0=off, 1=filtered, 2=verbose
        comm.queueCommand(new GcodeCommand("{sv:1}"));

        // Request firmware settings
        comm.queueCommand(new GcodeCommand("$$"));

        // Enable feed overrides
        comm.queueCommand(new GcodeCommand("{mfoe:1}"));
        comm.queueCommand(new GcodeCommand("{mtoe:1}"));
        comm.queueCommand(new GcodeCommand("{ssoe:1}"));

        // Activate motors by default
        comm.queueCommand(new GcodeCommand("{xam:1, yam:1, zam:1}"));

        // Request initial status report
        comm.queueCommand(new GcodeCommand("{sr:n}"));

        comm.streamCommands();

        // Refresh the status update
        setStatusUpdateRate(getStatusUpdateRate());
    }

    @Override
    protected void cancelSendAfterEvent() throws Exception {
        // Canceling the job on the controller (which will also flush the buffer)
        comm.sendByteImmediately(TinyGUtils.COMMAND_KILL_JOB);

        // Work around for clearing the sent buffer size
        comm.cancelSend();

        // We will end up in an alarm state, clear the alarm
        killAlarmLock();
    }
}
