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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The current state of the joystick or gamepad. When updating values it will compare
 * with the previous state if the value has changed. Any updates made will flag the state
 * as dirty.
 *
 * @author Joacim Breiler
 */
public class JoystickState implements Serializable {
    private static final long serialVersionUID = -3068755590868647792L;
    private final Map<JoystickButton, Boolean> buttonsMap;
    private final Map<JoystickAxis, Float> axisMap;
    private boolean dirty;
    private String name;

    public JoystickState() {
        buttonsMap = new HashMap<>();
        Arrays.asList(JoystickButton.values()).forEach(joystickButton -> buttonsMap.put(joystickButton, false));

        axisMap = new HashMap<>();
        Arrays.asList(JoystickAxis.values()).forEach(joystickAxis -> axisMap.put(joystickAxis, 0.0F));
    }

    public boolean getButton(JoystickButton button) {
        return buttonsMap.getOrDefault(button, false);
    }

    public void setButton(JoystickButton button, boolean pressed) {
        if (buttonsMap.get(button) != pressed) {
            buttonsMap.put(button, pressed);
            dirty = true;
        }
    }

    public float getAxis(JoystickAxis axis) {
        return axisMap.getOrDefault(axis, 0.0F);
    }

    public void setAxis(JoystickAxis axis, float value) {
        if (axisMap.get(axis) != value) {
            axisMap.put(axis, value);
            dirty = true;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
