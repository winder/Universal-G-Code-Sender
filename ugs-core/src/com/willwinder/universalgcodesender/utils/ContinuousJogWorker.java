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
import java.lang.System;

/**
 * A continuous jog worker that will send small jog commands at a fixed interval
 * so that it will achieve the jog feed rate set in the
 * {@link JogService#getFeedRate()}.
 * <p>
 * It will attempt to listen to completed commands to determine if a new jog
 * command can be sent.
 * <p>
 * Example usage: ContinuousJogWorker worker = new
 * ContinuousJogWorker(backendAPI, jogService); worker.setDirection(1, 0, 0)
 * worker.start(); worker.stop();
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
    private int outstandingCommands = 0;
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
     * Starts sending continuous jogging commands at a fixed interval. Use
     * {@link #stop()} to stop sending jog commands
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
     * Calculates how long we could theoretically move within the defined jog
     * command interval. This will make a jogging event that's short but long
     * enough to fill the controllers buffer to make the movement smooth.
     * <p>
     * The step size is given using the current units of
     * {@link Settings#getJogFeedRate()}.
     *
     * @return the step size in the units of {@link Settings#getJogFeedRate()}
     */
    private double calculateStepSize() {
        double maxFeedRate = jogService.getFeedRate();
        return (maxFeedRate / 60.0) * (JOG_COMMAND_INTERVAL / 1000.0);
    }

    private void sendContinuousJogCommands() {
        long nextCommandMillis = System.currentTimeMillis();
        isRunning = true;
        final double stepSize = calculateStepSize() / Math.sqrt(Math.abs(x) + Math.abs(y) + Math.abs(z));
        final UnitUtils.Units units = jogService.getUnits();

        // Preload 4 jog commands into the planning buffer...
        for (int i = 0; i < 4; i++) {
            outstandingCommands++;
            jogService.adjustManualLocation(new PartialPosition(x * stepSize, y * stepSize, z * stepSize, units));
        }        

        // At this point, we replace a command in the buffer every jog command interval, with
        // reference to System.currentTimeMillis() to calculate the amount of time to sleep.
        while (isRunning) {
            nextCommandMillis += JOG_COMMAND_INTERVAL;
            // Sleep the jog interval before sending the next command
            try {
                long sleepDuration = nextCommandMillis - System.currentTimeMillis();
                if (sleepDuration > 0) Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                // The timers got interrupted, never mind...
            }

            // Ensure that we only send one command at the time, waiting for it to complete
            if (outstandingCommands == 0) {
                outstandingCommands++;
                jogService.adjustManualLocation(new PartialPosition(x * stepSize, y * stepSize, z * stepSize, units));
            }

        }

        // at this point, isRunning == false, so we cancel the jog.
        // we have to wait for any outstanding commands to be completed
        // before we cancel the jog, in order to handle the race between
        // the jog command and the cancel.
        while (outstandingCommands > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        jogService.cancelJog();
        future = null;
    }

    /**
     * Sets the direction to jog in a value between 1.0 to -1.0. The value 0.5
     * would mean that that axis would move half the step distance. Zero means
     * that the axis shouldn't be moved at all.
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
            if (outstandingCommands > 0) {
                outstandingCommands--;
            }
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
