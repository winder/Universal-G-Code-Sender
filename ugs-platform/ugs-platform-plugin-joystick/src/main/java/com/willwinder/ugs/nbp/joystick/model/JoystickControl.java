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
package com.willwinder.ugs.nbp.joystick.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The available buttons and axises that can be read by the service.
 * The controls are divided into two groups:
 *  - Analog - which will read analog values between 1.0 to -1.0
 *  - Digital - which will read digital values such as buttons true/false
 *
 * @author Joacim Breiler
 */
public enum JoystickControl {
    A("platform.plugin.joystick.a", false),
    B("platform.plugin.joystick.b", false),
    X("platform.plugin.joystick.x", false),
    Y("platform.plugin.joystick.y", false),
    BACK("platform.plugin.joystick.back", false),
    SELECT("platform.plugin.joystick.select", false),
    START("platform.plugin.joystick.start", false),
    L1("platform.plugin.joystick.l1", false),
    L2("platform.plugin.joystick.l2", true),
    L3("platform.plugin.joystick.l3", false),
    R1("platform.plugin.joystick.r1", false),
    R2("platform.plugin.joystick.r2", true),
    R3("platform.plugin.joystick.r3", false),
    DPAD_UP("platform.plugin.joystick.padUp", false),
    DPAD_DOWN("platform.plugin.joystick.padDown", false),
    DPAD_LEFT("platform.plugin.joystick.padLeft", false),
    DPAD_RIGHT("platform.plugin.joystick.padRight", false),
    LEFT_X("platform.plugin.joystick.leftX", true),
    LEFT_Y("platform.plugin.joystick.leftY", true),
    RIGHT_X("platform.plugin.joystick.rightX", true),
    RIGHT_Y("platform.plugin.joystick.rightY", true);

    private final String localization;

    private final boolean isAnalog;

    JoystickControl(String localization, boolean isAnalog) {
        this.localization = localization;
        this.isAnalog = isAnalog;
    }

    public String getLocalization() {
        return localization;
    }

    public static List<JoystickControl> getAnalogControls() {
        return Arrays.stream(values())
                .filter(JoystickControl::isAnalog)
                .collect(Collectors.toList());
    }

    public static List<JoystickControl> getDigitalControls() {
        return Arrays.stream(values())
                .filter(joystickControl -> !joystickControl.isAnalog())
                .collect(Collectors.toList());
    }

    public boolean isAnalog() {
        return isAnalog;
    }
}
