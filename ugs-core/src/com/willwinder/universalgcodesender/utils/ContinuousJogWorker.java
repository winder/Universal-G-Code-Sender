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

import com.willwinder.universalgcodesender.listeners.MessageType;
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
    private final JogService jogService;
    private final BackendAPI backendAPI;
    private float x;
    private float y;
    private float z;
    private boolean isRunning = false;
    private boolean jogCanceled = true;

    public ContinuousJogWorker(BackendAPI backendAPI, JogService jogService) {
//        this.executorService = Executors.newSingleThreadExecutor();
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
        jogCanceled = true;
        backendAPI.removeUGSEventListener(this);
    }

    /**
     * Starts sending continuous jogging commands at a fixed interval.
     * Use {@link #stop()} to stop sending jog commands
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            jogCanceled = false;
            sendJogCommand();
        }
    }

    /**
     * Stops sending continuous jogging commands
     */
    public void stop() {
        isRunning = false;
    }

    // puts one jog command in the buffer that will take JOG_COMMAND_INTERVAL ms to execute (excluding accelleration)
    private void sendJogCommand() {
        final UnitUtils.Units units = jogService.getUnits();
        final double jogVectorLength = Math.sqrt((x * x) + (y * y) + (z * z));
        final double speedFactor = jogVectorLength; //FIXME? Double.min(jogVectorLength, 1.0); // caps jog speed at 100% (1.0) of maxFeedRate
        final double maxFeedRate = jogService.getFeedRate() / 60.0; // maximum jog feed rate in units per second
        final double v = maxFeedRate * speedFactor; // scaled jog feed rate in units per second
        final double N = 15; // FIXME: get from controller
        final double stepSize = 1;
        final double a = 20; // 20 in/s^2 accelleration (508mm/s^2) FIXME get from controller in the right units
        final double dtLat = 0.010; // minimum dt in ms for grbl command latency
        final double dtAcc = 0;//Math.pow(v, 2) / (2 * a * (N-1)); // minimum dt in ms to ensure the jog feed rate will be achieved
        final double dt = Double.max(dtLat, dtAcc); // final dt in ms
        final double s = v * dt; // s = distance in units that this jog command should travel
        final double scaleFactor = s / jogVectorLength; // determine scaleFactor required to scale jogVectorLength to s
        jogService.adjustManualLocation(new PartialPosition(x * scaleFactor, y * scaleFactor, z * scaleFactor, units), speedFactor);
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
            if (isRunning) {
                // still running, send the next jog command
                sendJogCommand();
            } else if (!jogCanceled) {
                // a command has completed
                // we've been stopped, so cancel jog commands in the buffer
                jogService.cancelJog();
                jogCanceled = true;
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
