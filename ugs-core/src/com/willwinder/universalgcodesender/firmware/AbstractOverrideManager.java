/*
    Copyright 2024 Will Winder

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
package com.willwinder.universalgcodesender.firmware;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.DefaultControllerListener;
import com.willwinder.universalgcodesender.listeners.OverridePercents;
import com.willwinder.universalgcodesender.listeners.OverrideType;

/**
 * An abstract override manager with some default implementation of common functions.
 *
 * @author Joacim Breiler
 */
public abstract class AbstractOverrideManager implements IOverrideManager {
    public static final int SETTLE_TIME_MS = 50;

    protected final IController controller;
    protected final ICommunicator communicator;

    protected int targetFeedSpeed = 100;
    protected int targetSpindleSpeed = 100;
    protected int targetRapidSpeed = 100;

    private boolean isRunning = false;
    private long lastSentCommand = 0;

    protected AbstractOverrideManager(IController controller, ICommunicator communicator) {
        this.controller = controller;
        this.communicator = communicator;
        this.controller.addListener(new DefaultControllerListener() {
            @Override
            public void statusStringListener(ControllerStatus status) {
                onControllerStatus(status);
            }

            @Override
            public void streamComplete() {
                resetAll();
            }

            @Override
            public void streamCanceled() {
                resetAll();
            }

            @Override
            public void streamStarted() {
                resetAll();
            }
        });
    }

    public void onControllerStatus(ControllerStatus controllerStatus) {
        if (!isRunning) {
            targetFeedSpeed = controllerStatus.getOverrides().feed();
            targetSpindleSpeed = controllerStatus.getOverrides().spindle();
            targetRapidSpeed = controllerStatus.getOverrides().rapid();
            return;
        }

        // Wait for the override to settle
        if (lastSentCommand + SETTLE_TIME_MS > System.currentTimeMillis()) {
            return;
        }
        lastSentCommand = System.currentTimeMillis();

        OverridePercents currentOverridePercents = controllerStatus.getOverrides();
        adjustFeedOverride(currentOverridePercents);
        adjustSpindleOverride(currentOverridePercents);
        adjustRapidOverride(currentOverridePercents);

        if (hasSettled()) {
            stop();
        }
    }

    protected abstract void adjustFeedOverride(OverridePercents currentOverridePercents);

    protected abstract void adjustSpindleOverride(OverridePercents currentOverridePercents) ;

    protected abstract void adjustRapidOverride(OverridePercents currentOverridePercents);

    protected abstract int getSpeedMinorStep(OverrideType overrideType);

    protected abstract int getSpeedMajorStep(OverrideType overrideType);

    public boolean isAvailable() {
        if (controller == null || controller.getControllerStatus() == null ||
                controller.getControllerStatus().getState() == null ||
                controller.getCapabilities() == null) {
            return false;
        }

        ControllerState state = controller.getControllerStatus().getState();
        return controller.getCapabilities().hasOverrides() && (state == ControllerState.HOLD || state == ControllerState.IDLE || state == ControllerState.RUN);
    }

    /**
     * Starts sending continuous override commands to achieve the target speeds
     */
    protected void start() {
        if (!isRunning) {
            isRunning = true;
            onControllerStatus(controller.getControllerStatus());
        }
    }

    /**
     * Stops sending override commands
     */
    private void stop() {
        isRunning = false;
    }

    @Override
    public boolean hasSettled() {
        OverridePercents overrides = controller.getControllerStatus().getOverrides();
        return overrides.spindle() == targetSpindleSpeed && overrides.feed() == targetFeedSpeed && overrides.rapid() == targetRapidSpeed;
    }
}
