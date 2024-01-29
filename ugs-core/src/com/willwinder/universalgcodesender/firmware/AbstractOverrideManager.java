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
import static com.willwinder.universalgcodesender.Utils.roundToNearestStepValue;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.DefaultControllerListener;
import com.willwinder.universalgcodesender.listeners.OverridePercents;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.model.Overrides;

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
        });
    }

    private void onControllerStatus(ControllerStatus controllerStatus) {
        if (!isRunning) {
            targetFeedSpeed = controllerStatus.getOverrides().feed();
            targetSpindleSpeed = controllerStatus.getOverrides().spindle();
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

        if (hasSettled()) {
            stop();
        }
    }

    protected void adjustFeedOverride(OverridePercents currentOverridePercents) {
        if (currentOverridePercents.feed() == targetFeedSpeed) {
            return;
        }

        float currentFeed = currentOverridePercents.feed();
        int majorSteps = (int) ((targetFeedSpeed - currentFeed) / getSpeedMajorStep(OverrideType.FEED_SPEED));
        int minorSteps = (int) ((targetFeedSpeed - currentFeed) / getSpeedMinorStep(OverrideType.FEED_SPEED));

        try {
            if (majorSteps < 0) {
                sendOverrideCommand(Overrides.CMD_FEED_OVR_COARSE_MINUS);
            } else if (majorSteps > 0) {
                sendOverrideCommand(Overrides.CMD_FEED_OVR_COARSE_PLUS);
            } else if (minorSteps < 0) {
                sendOverrideCommand(Overrides.CMD_FEED_OVR_FINE_MINUS);
            } else if (minorSteps > 0) {
                sendOverrideCommand(Overrides.CMD_FEED_OVR_FINE_PLUS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void adjustSpindleOverride(OverridePercents currentOverridePercents) {
        if (currentOverridePercents.spindle() == targetSpindleSpeed) {
            return;
        }

        float currentSpindle = currentOverridePercents.spindle();
        int majorSteps = (int) ((targetSpindleSpeed - currentSpindle) / getSpeedMajorStep(OverrideType.SPINDLE_SPEED));
        int minorSteps = (int) ((targetSpindleSpeed - currentSpindle) / getSpeedMinorStep(OverrideType.SPINDLE_SPEED));

        try {
            if (majorSteps < 0) {
                sendOverrideCommand(Overrides.CMD_SPINDLE_OVR_COARSE_MINUS);
            } else if (majorSteps > 0) {
                sendOverrideCommand(Overrides.CMD_SPINDLE_OVR_COARSE_PLUS);
            } else if (minorSteps < 0) {
                sendOverrideCommand(Overrides.CMD_SPINDLE_OVR_FINE_MINUS);
            } else if (minorSteps > 0) {
                sendOverrideCommand(Overrides.CMD_SPINDLE_OVR_FINE_PLUS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract int getSpeedMinorStep(OverrideType overrideType);

    protected abstract float getSpeedMajorStep(OverrideType overrideType);

    public boolean isAvailable() {
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
        return overrides.spindle() == targetSpindleSpeed && overrides.feed() == targetFeedSpeed;
    }

    @Override
    public void setSpeedTarget(OverrideType type, int percent) {
        percent = (int) Math.round(roundToNearestStepValue(percent, getSpeedMin(type), getSpeedMax(type), getSpeedStep(type)));
        if (type == OverrideType.FEED_SPEED) {
            targetFeedSpeed = percent;
        } else if (type == OverrideType.SPINDLE_SPEED) {
            targetSpindleSpeed = percent;
        }

        start();
    }
}
