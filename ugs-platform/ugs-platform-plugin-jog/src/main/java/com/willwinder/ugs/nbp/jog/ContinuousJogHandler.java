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
package com.willwinder.ugs.nbp.jog;

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

/**
 * Handles continuous jogging by starting a new thread and make sure all jog commands
 * are sent before any cancel command is issued.
 *
 * @author Joacim Breiler
 */
public class ContinuousJogHandler implements ControllerListener {
    /**
     * The interval in milliseconds to send jog commands to the controller when
     * continuous jog is activated.
     */
    private static final int JOG_COMMAND_INTERVAL = 100;

    private final JogService jogService;
    private final BackendAPI backend;
    private boolean isRunning;
    private boolean isWaitingForCommandComplete;

    public ContinuousJogHandler(BackendAPI backend, JogService jogService) {
        this.backend = backend;
        this.jogService = jogService;
    }

    /**
     * Starts continuous jogging in the direction by the given button which will continue
     * until stop is called.
     *
     * @param button the button fo the direction to start jogging
     */
    public void start(JogPanelButtonEnum button) {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        isWaitingForCommandComplete = false;
        ThreadHelper.invokeLater(() -> sendContinuousJogCommands(button));
    }

    /**
     * Stops any continuous jogging
     */
    public void stop() {
        isRunning = false;
    }

    private void sendContinuousJogCommands(JogPanelButtonEnum button) {
        try {
            final double stepSize = calculateStepSize();
            while (isRunning) {
                // Ensure that we only send one command at the time, waiting for it to complete
                if (!isWaitingForCommandComplete) {
                    isWaitingForCommandComplete = true;
                    jogService.adjustManualLocation(button.getX(), button.getY(), button.getZ(), stepSize);
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
        while (isWaitingForCommandComplete) {
            Thread.sleep(10);
        }
    }

    /**
     * Calculates how long we could theoretically move within the defined jog command interval.
     * The step size is given using the current units of {@link Settings#getJogFeedRate()}.
     *
     * @return the step size in the units of {@link Settings#getJogFeedRate()}
     */
    private double calculateStepSize() {
        double feedRate = backend.getSettings().getJogFeedRate();

        // Calculate how long we should be able to move at the given interval and add 10%
        return (feedRate / 60) * (JOG_COMMAND_INTERVAL / 1000.0) * 1.1;
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void receivedAlarm(Alarm alarm) {
        stop();
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
