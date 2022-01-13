/*
    Copyright 2020-2021 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A continuous jog worker that will send small jog commands at a fixed interval so that
 * it will achieve the jog feed rate set in the {@link JogService#getFeedRate()}.
 * <p>
 * It will attempt to listen to completed commands to determine if a new jog command
 * can be sent.
 * <p>
 * Example usage:
 * ContinuousJogWorker worker = new ContinuousJogWorker(backendAPI, jogService);
 * worker.setDirection(1, 0, 0)
 * worker.start();
 * worker.stop();
 *
 * @author Joacim Breiler
 */
public class ContinuousJogWorker implements UGSEventListener {
    private static final long JOG_COMMAND_INTERVAL = 100;
    private final JogService jogService;
    private final ExecutorService executorService;
    private final BackendAPI backendAPI;
    private float x;
    private float y;
    private float z;
    private boolean isRunning;
    private boolean isWaitingForCommandComplete;
    private Future<?> future;

    public ContinuousJogWorker(BackendAPI backendAPI, JogService jogService) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.jogService = jogService;
        this.backendAPI = backendAPI;
        this.x = 0f;
        this.y = 0f;
        this.z = 0f;

        // Registers itself as a listener for completed commands
        this.backendAPI.addUGSEventListener(this);
    }

    /**
     * Destroys this instance, removing it as a listener from the backend API.
     */
    public void destroy() {
        isRunning = false;
        if (future != null) {
            future.cancel(false);
        }
        backendAPI.removeUGSEventListener(this);
    }

    /**
     * Starts sending continuous jogging commands at a fixed interval.
     * Use {@link #stop()} to stop sending jog commands
     */
    public void start() {
        if (!isRunning && future == null) {
            future = executorService.submit(this::sendContinuousJogCommands);
        }
    }

    /**
     * Stops sending continuous jogging commands
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Calculates how long we could theoretically move within the defined jog command interval.
     * This will make a jogging event that's short but long enough to fill the controllers buffer
     * to make the movement smooth.
     * <p>
     * The step size is given using the current units of {@link Settings#getJogFeedRate()}.
     *
     * @return the step size in the units of {@link Settings#getJogFeedRate()}
     */
    private double calculateStepSize() {
        double maxFeedRate = jogService.getFeedRate();

        // Calculate how long we should be able to move at the given interval and add 20%
        return (maxFeedRate / 60.0) * (JOG_COMMAND_INTERVAL / 1000.0) * 1.2;
    }

    private void sendContinuousJogCommands() {
        try {
            isRunning = true;
            final double stepSize = calculateStepSize();
            final UnitUtils.Units units = jogService.getUnits();
            while (isRunning) {
                // Ensure that we only send one command at the time, waiting for it to complete
                if (!isWaitingForCommandComplete) {
                    isWaitingForCommandComplete = true;
                    jogService.adjustManualLocation(new PartialPosition(x * stepSize, y * stepSize, z * stepSize, units));
                } else {
                    Thread.sleep(JOG_COMMAND_INTERVAL);
                }
            }

            waitForCommandToComplete();
        } catch (InterruptedException e) {
            // The timers got interrupted, never mind...
        }

        jogService.cancelJog();
        future = null;
    }

    private void waitForCommandToComplete() throws InterruptedException {
        while (isWaitingForCommandComplete && isRunning) {
            Thread.sleep(10);
        }
    }

    /**
     * Sets the direction to jog in a value between 1.0 to -1.0.
     * The value 0.5 would mean that that axis would move half the step distance.
     * Zero means that the axis shouldn't be moved at all.
     *
     * @param x the direction to move (1.0 to -1.0)
     * @param y the direction to move (1.0 to -1.0)
     * @param z the direction to move (1.0 to -1.0)
     */
    public void setDirection(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }



    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof CommandEvent && ((CommandEvent) event).getCommandEventType() == CommandEventType.COMMAND_COMPLETE) {
            GcodeCommand command = ((CommandEvent) event).getCommand();
            isWaitingForCommandComplete = false;
            if (command.isError()) {
                stop();
                if (future != null) {
                    future.cancel(false);
                }
            }
        }
    }

    public void setDirection(Axis axis, float value) {
        switch (axis) {
            case X:
                x = value;
                break;
            case Y:
                y = value;
                break;
            case Z:
                z = value;
                break;
        }
    }

    public void update() {
        if (x != 0 || y != 0 || z != 0) {
            start();
        } else {
            stop();
        }
    }
}
