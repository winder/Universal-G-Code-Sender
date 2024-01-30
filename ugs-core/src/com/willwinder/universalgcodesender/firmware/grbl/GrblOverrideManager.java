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
package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.firmware.AbstractOverrideManager;
import com.willwinder.universalgcodesender.firmware.IOverrideManager;
import com.willwinder.universalgcodesender.firmware.OverrideException;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.model.Overrides;

import java.util.List;

public class GrblOverrideManager extends AbstractOverrideManager implements IOverrideManager {
    private static final int MINOR_STEP = 1;
    private static final int MAJOR_STEP = 10;
    private static final int FEED_MIN = 10;
    private static final int FEED_MAX = 200;
    private static final int FEED_DEFAULT = 100;
    private static final int SPINDLE_MIN = 10;
    private static final int SPINDLE_MAX = 200;
    private static final int SPINDLE_DEFAULT = 100;

    public GrblOverrideManager(IController controller, ICommunicator communicator) {
        super(controller, communicator);
    }

    @Override
    protected int getSpeedMinorStep(OverrideType overrideType) {
        return MINOR_STEP;
    }

    @Override
    protected int getSpeedMajorStep(OverrideType overrideType) {
        return MAJOR_STEP;
    }

    public void sendOverrideCommand(Overrides command) {
        Byte realTimeCommand = GrblUtils.getOverrideForEnum(command, controller.getCapabilities());
        if (realTimeCommand != null) {
            try {
                communicator.sendByteImmediately(realTimeCommand);
            } catch (Exception e) {
                throw new OverrideException("Could not send override command", e);
            }
        }
    }

    @Override
    public int getSpeedDefault(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> FEED_DEFAULT;
            case SPINDLE_SPEED -> SPINDLE_DEFAULT;
            default -> 0;
        };
    }

    @Override
    public int getSpeedTargetValue(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> targetFeedSpeed;
            case SPINDLE_SPEED -> targetSpindleSpeed;
            default -> 0;
        };
    }

    @Override
    public List<OverrideType> getSpeedTypes() {
        return List.of(OverrideType.FEED_SPEED, OverrideType.SPINDLE_SPEED);
    }

    @Override
    public List<OverrideType> getToggleTypes() {
        return List.of(OverrideType.SPINDLE_TOGGLE, OverrideType.MIST_TOGGLE, OverrideType.FLOOD_TOGGLE);
    }

    @Override
    public void toggle(OverrideType type) {
        switch (type) {
            case SPINDLE_TOGGLE -> sendOverrideCommand(Overrides.CMD_TOGGLE_SPINDLE);
            case MIST_TOGGLE -> sendOverrideCommand(Overrides.CMD_TOGGLE_MIST_COOLANT);
            case FLOOD_TOGGLE -> sendOverrideCommand(Overrides.CMD_TOGGLE_FLOOD_COOLANT);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    @Override
    public boolean isToggled(OverrideType overrideType) {
        return switch (overrideType) {
            case MIST_TOGGLE -> controller.getControllerStatus().getAccessoryStates().mist();
            case FLOOD_TOGGLE -> controller.getControllerStatus().getAccessoryStates().flood();
            case SPINDLE_TOGGLE -> (controller.getControllerStatus().getSpindleSpeed() > 0);
            default -> false;
        };
    }

    @Override
    public int getSpeedMax(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> FEED_MAX;
            case SPINDLE_SPEED -> SPINDLE_MAX;
            default -> 0;
        };
    }

    @Override
    public int getSpeedMin(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> FEED_MIN;
            case SPINDLE_SPEED -> SPINDLE_MIN;
            default -> 0;
        };
    }

    @Override
    public int getSpeedStep(OverrideType type) {
        return switch (type) {
            case FEED_SPEED, SPINDLE_SPEED -> MINOR_STEP;
            default -> 0;
        };
    }

}
