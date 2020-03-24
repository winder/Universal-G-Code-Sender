/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.joystick.service;

import com.willwinder.ugs.nbp.joystick.model.JoystickAxis;
import com.willwinder.ugs.nbp.joystick.model.JoystickButton;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

public class JoystickJogService implements JoystickServiceListener, ControllerListener {
    private static final long JOG_COMMAND_INTERVAL = 100;
    private final JogService jogService;
    private final BackendAPI backendAPI;
    private boolean isRunning;
    private boolean isWaitingForCommandComplete;
    private float x;
    private float y;
    private float z;

    public JoystickJogService() {
        JoystickService joystickService = CentralLookup.getDefault().lookup(JoystickService.class);
        joystickService.addListener(this);

        jogService = CentralLookup.getDefault().lookup(JogService.class);
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addControllerListener(this);
    }

    @Override
    public void onUpdate(JoystickState state) {
        if (state.getButton(JoystickButton.START)) {
            sendFile();
        } else if (state.getButton(JoystickButton.BACK)) {
            cancelSend();
        } else if (state.getButton(JoystickButton.LEFT_BUMPER)) {
            jogService.divideFeedRate();
        } else if (state.getButton(JoystickButton.RIGHT_BUMPER)) {
            jogService.multiplyFeedRate();
        } else if (jogService.canJog()) {
            x = readValue(state, JoystickAxis.LEFT_X, JoystickButton.DPAD_LEFT, JoystickButton.DPAD_RIGHT);
            y = readValue(state, JoystickAxis.LEFT_Y, JoystickButton.DPAD_DOWN, JoystickButton.DPAD_UP);
            z = readValue(state, JoystickAxis.RIGHT_Y, JoystickButton.A, JoystickButton.Y);

            if (x != 0 || y != 0 || z != 0) {
                start();
            } else {
                stop();
            }
        }
    }

    private void cancelSend() {
        try {
            backendAPI.cancel();
        } catch (Exception e) {
            // Never mind
        }
    }

    private void sendFile() {
        try {
            backendAPI.send();
        } catch (Exception e) {
            // Never mind
        }
    }

    private float readValue(JoystickState state, JoystickAxis axis, JoystickButton negativeButton, JoystickButton positiveButton) {
        // Start reading the analog value
        float result = state.getAxis(axis);

        // Then read the digital value
        if (state.getButton(negativeButton)) {
            result = -1;
        } else if (state.getButton(positiveButton)) {
            result = 1;
        }

        return result;
    }

    /**
     * Calculates how long we could theoretically move within the defined jog command interval.
     * The step size is given using the current units of {@link Settings#getJogFeedRate()}.
     *
     * @return the step size in the units of {@link Settings#getJogFeedRate()}
     */
    private double calculateStepSize() {
        double feedRate = jogService.getFeedRate();

        // Calculate how long we should be able to move at the given interval and add 10%
        return (feedRate / 60) * (JOG_COMMAND_INTERVAL / 1000.0) * 1.1;
    }

    /**
     * Starts sending continuous jogging commands
     */
    private void start() {
        if (!isRunning) {
            ThreadHelper.invokeLater(this::sendContinuousJogCommands);
        }
    }

    /**
     * Stops sending continuous jogging commands
     */
    private void stop() {
        isRunning = false;
    }

    private void sendContinuousJogCommands() {
        // Make sure we are not already running
        if (isRunning) {
            return;
        }

        try {
            isRunning = true;
            final double stepSize = calculateStepSize();
            while (isRunning) {
                // Ensure that we only send one command at the time, waiting for it to complete
                if (!isWaitingForCommandComplete) {
                    isWaitingForCommandComplete = true;
                    jogService.adjustManualLocation(x, y, z, stepSize);
                } else {
                    Thread.sleep(JOG_COMMAND_INTERVAL);
                }
            }

            waitForCommandToComplete();
        } catch (InterruptedException e) {
            // The timers got interrupted, never mind...
        }

        jogService.cancelJog();
    }

    private void waitForCommandToComplete() throws InterruptedException {
        while (isWaitingForCommandComplete && isRunning) {
            Thread.sleep(10);
        }
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {

    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void receivedAlarm(Alarm alarm) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {

    }

    @Override
    public void commandComplete(GcodeCommand command) {
        isWaitingForCommandComplete = false;
        if (command.isError() && isRunning) {
            stop();
        }
    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void probeCoordinates(Position p) {

    }

    @Override
    public void statusStringListener(ControllerStatus status) {

    }
}
