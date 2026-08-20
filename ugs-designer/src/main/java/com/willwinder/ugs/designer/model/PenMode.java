/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.model;

/**
 * How a generated program should put the pen of a plotter down on the paper and lift it again.
 * Plotters are built in very different ways, so the same drawing needs to be posted differently
 * depending on what actually moves the pen.
 */
public enum PenMode {
    /**
     * The pen is carried by the Z axis, so it is lowered to a depth and lifted to the safe height.
     */
    Z_AXIS("Z axis"),

    /**
     * A servo or solenoid is driven from the spindle PWM output, so the pen is moved by changing
     * the commanded spindle speed.
     */
    SPINDLE_SPEED("Spindle speed"),

    /**
     * The pen is moved by commands that are specific to the machine, such as a servo command.
     */
    CUSTOM_COMMAND("Custom commands");

    private final String displayName;

    PenMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * The combo boxes in the tool settings dialogs render their values with {@code toString()}.
     * Settings are persisted using {@code name()}, so this is safe to change.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
