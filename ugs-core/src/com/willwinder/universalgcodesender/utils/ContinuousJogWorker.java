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
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.services.JogService;

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
    private float a;
    private float b;
    private float c;
    private boolean isRunning = false;
    private boolean jogCanceled = true;

    public ContinuousJogWorker(BackendAPI backendAPI, JogService jogService) {
        this.jogService = jogService;
        this.backendAPI = backendAPI;
        this.x = 0f;
        this.y = 0f;
        this.z = 0f;
        this.a = 0f;
        this.b = 0f;
        this.c = 0f;

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

    /**
     * puts one jog command in the buffer that will take JOG_COMMAND_INTERVAL ms to execute (excluding accelleration)
     * 
     * This function calculates the feedrate and magnitude of an individual jog command based
     * on the algorithm described here: https://github.com/gnea/grbl/wiki/Grbl-v1.1-Jogging
     * The algorithm has been simplified by reducing consideration for:
     *    dt > v^2 / (2 * a * (N-1))
     * Instead, we make some assumptions that are expected for good jog performance:
     *  1) The latency from when we send a jog command to GRBL and receive the "ok" back is less than 10ms (typically 1-7ms).
     *  2) The Jog feedrate and machine acceleration are such that the machine can accelerate to it's full jog rate within
     *     the length of the command buffer (N=15 for regular GRBL).
     *  3) The command buffer isn't larger than N=15 (if it is, we'll get increasing lag in response to direction/rate changes on the joystick)
     * command to take 10ms to execute excluding acceleration time.
     *
     * Note: the jog command total feedrate may exceed the set feedrate if moving in more than one axis at the same time. The max rate
     * in any 1 axis will never exceed the jog feedrate.
     *
     */
    private void sendJogCommand() {
        final UnitUtils.Units units = jogService.getUnits();
        final double jogVectorLength = Math.sqrt((x * x) + (y * y) + (z * z) + (a * a) + (b * b) + (c * c));
        final double speedFactor = jogVectorLength; //FIXME? Double.min(jogVectorLength, 1.0); // caps jog speed at 100% (1.0) of maxFeedRate
        final double maxFeedRate = jogService.getFeedRate() / 60.0; // maximum jog feed rate in units per second
        final double v = maxFeedRate * speedFactor; // scaled jog feed rate in units per second
        final double dt = 0.010; // minimum dt in ms for grbl command latency
        final double s = v * dt; // s = distance in units that this jog command should travel
        final double scaleFactor = s / jogVectorLength; // determine scaleFactor required to scale jogVectorLength to s

        PartialPosition.Builder builder = PartialPosition.builder().setUnits(units);
        setAxisIfNotZero(builder, Axis.X, x * scaleFactor);
        setAxisIfNotZero(builder, Axis.Y, y * scaleFactor);
        setAxisIfNotZero(builder, Axis.Z, z * scaleFactor);
        setAxisIfNotZero(builder, Axis.A, a * scaleFactor);
        setAxisIfNotZero(builder, Axis.B, b * scaleFactor);
        setAxisIfNotZero(builder, Axis.C, c * scaleFactor);
        jogService.adjustManualLocation(builder.build(), speedFactor);
    }

    private void setAxisIfNotZero(PartialPosition.Builder builder, Axis axis, double value) {
        if (value != 0 && backendAPI.getController().getCapabilities().hasAxis(axis)) {
            builder.setValue(axis, value);
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
        this.setDirection(x, y, z, 0, 0, 0);
    }

    /**
     * Sets the direction to jog in a value between 1.0 to -1.0.
     * The value 0.5 would mean that that axis would move half the step distance.
     * Zero means that the axis shouldn't be moved at all.
     *
     * @param x the direction to move (1.0 to -1.0)
     * @param y the direction to move (1.0 to -1.0)
     * @param z the direction to move (1.0 to -1.0)
     * @param a the direction to move (1.0 to -1.0)
     * @param b the direction to move (1.0 to -1.0)
     * @param c the direction to move (1.0 to -1.0)
     */
    public void setDirection(float x, float y, float z, float a, float b, float c) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof CommandEvent && ((CommandEvent) event).getCommandEventType() == CommandEventType.COMMAND_COMPLETE) {
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
            case A:
                a = value;
                break;
            case B:
                b = value;
                break;
            case C:
                c = value;
                break;
        }
    }

    public void update() {
        if (x != 0 || y != 0 || z != 0 || a != 0 || b != 0 || c != 0) {
            start();
        } else {
            stop();
        }
    }
}
