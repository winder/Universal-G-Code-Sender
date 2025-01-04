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
import static com.willwinder.universalgcodesender.Utils.roundToNearestStepValue;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.firmware.AbstractOverrideManager;
import com.willwinder.universalgcodesender.firmware.IOverrideManager;
import com.willwinder.universalgcodesender.firmware.OverrideException;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.listeners.OverridePercents;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.services.MessageService;

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
    private static final int RAPID_SPEED_MAX = 100;
    private static final int RAPID_SPEED_MIN = 25;
    private static final int RAPID_SPEED_STEP = 25;
    private static final int RAPID_DEFAULT = 100;
    private MessageService messageService;

    public GrblOverrideManager(IController controller, ICommunicator communicator, MessageService messageService) {
        super(controller, communicator);
        this.messageService = messageService;
    }

    @Override
    protected void adjustRapidOverride(OverridePercents currentOverridePercents) {
        if (currentOverridePercents.rapid() == targetRapidSpeed) {
            return;
        }

        try {
            if (targetRapidSpeed <= 25) {
                sendOverrideCommand(Overrides.CMD_RAPID_OVR_LOW);
            } else if (targetRapidSpeed <= 50) {
                sendOverrideCommand(Overrides.CMD_RAPID_OVR_MEDIUM);
            } else {
                sendOverrideCommand(Overrides.CMD_RAPID_OVR_RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
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

    @Override
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
                messageService.dispatchMessage(MessageType.VERBOSE, ">>> 0x" + String.format("%02X ", realTimeCommand) + "\n");
            } catch (Exception e) {
                throw new OverrideException("Could not send override command", e);
            }
        }
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void resetAll() {
        sendOverrideCommand(Overrides.CMD_RAPID_OVR_RESET);
        sendOverrideCommand(Overrides.CMD_FEED_OVR_RESET);
        sendOverrideCommand(Overrides.CMD_SPINDLE_OVR_RESET);
    }

    @Override
    public int getSliderDefault(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> FEED_DEFAULT;
            case SPINDLE_SPEED -> SPINDLE_DEFAULT;
            case RAPID_SPEED -> RAPID_DEFAULT;
            default -> 0;
        };
    }

    @Override
    public int getSliderTargetValue(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> targetFeedSpeed;
            case SPINDLE_SPEED -> targetSpindleSpeed;
            case RAPID_SPEED -> targetRapidSpeed;
            default -> 0;
        };
    }

    @Override
    public List<OverrideType> getSliderTypes() {
        return List.of(OverrideType.FEED_SPEED, OverrideType.SPINDLE_SPEED);
    }

    @Override
    public List<OverrideType> getToggleTypes() {
        return List.of(OverrideType.SPINDLE_TOGGLE, OverrideType.MIST_TOGGLE, OverrideType.FLOOD_TOGGLE);
    }

    @Override
    public List<OverrideType> getRadioTypes() {
        return List.of(OverrideType.RAPID_SPEED);
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
    public int getRadioDefault(OverrideType type) {
        return getSliderDefault(type);
    }

    @Override
    public void setRadioTarget(OverrideType type, int value) {
        setSliderTarget(type, value);
    }

    @Override
    public int getSliderMax(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> FEED_MAX;
            case SPINDLE_SPEED -> SPINDLE_MAX;
            case RAPID_SPEED -> RAPID_SPEED_MAX;
            default -> 0;
        };
    }

    @Override
    public int getSliderMin(OverrideType type) {
        return switch (type) {
            case FEED_SPEED -> FEED_MIN;
            case SPINDLE_SPEED -> SPINDLE_MIN;
            case RAPID_SPEED -> RAPID_SPEED_MIN;
            default -> 0;
        };
    }

    @Override
    public int getSliderStep(OverrideType type) {
        return switch (type) {
            case FEED_SPEED, SPINDLE_SPEED -> MINOR_STEP;
            case RAPID_SPEED -> RAPID_SPEED_STEP;
            default -> 0;
        };
    }

    @Override
    public void setSliderTarget(OverrideType type, int percent) {
        percent = (int) Math.round(roundToNearestStepValue(percent, getSliderMin(type), getSliderMax(type), getSliderStep(type)));
        if (type == OverrideType.FEED_SPEED) {
            targetFeedSpeed = percent;
        } else if (type == OverrideType.SPINDLE_SPEED) {
            targetSpindleSpeed = percent;
        } else if (type == OverrideType.RAPID_SPEED) {
            if (percent < 50) {
                targetRapidSpeed = 25;
            } else if (percent < 100) {
                targetRapidSpeed = 50;
            } else {
                targetRapidSpeed = 100;
            }
        }

        start();
    }

    @Override
    public List<Integer> getSliderSteps(OverrideType type) {
        if (type == OverrideType.FEED_SPEED || type == OverrideType.SPINDLE_SPEED) {
            return List.of(0, 100, 200);
        }
        return List.of();
    }

    @Override
    public List<Integer> getRadioSteps(OverrideType type) {
        if(type == OverrideType.RAPID_SPEED) {
            return List.of(25, 50, 100);
        }
        return List.of();
    }
}
