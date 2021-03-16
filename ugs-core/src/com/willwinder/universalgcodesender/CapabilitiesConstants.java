/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender;

/**
 * Constants for defining capabilities that a controller may support. The constants
 * may be added to a {@link Capabilities} object.
 *
 * @author Joacim Breiler
 */
public class CapabilitiesConstants {
    /**
     * A key for identifying if the hardware have support for jogging.
     */
    public static final String JOGGING = "JOGGING";

    /**
     * A key for identifying if the hardware have support for continuous jogging.
     */
    public static final String CONTINUOUS_JOGGING = "CONTINUOUS_JOGGING";

    /**
     * A key for identifying if the hardware have support for overrides
     */
    public static final String OVERRIDES = "OVERRIDES";

    /**
     * A key for identifying if the hardware have support for homing
     */
    public static final String HOMING = "HOMING";

    /**
     * A key for identifying if the hardware have support for hard limits
     */
    public static final String HARD_LIMITS = "HARD_LIMITS";

    /**
     * A key for identifying if the hardware have support for soft limits
     */
    public static final String SOFT_LIMITS = "SOFT_LIMITS";

    /**
     * A key for identifying if the hardware have support for the setup wizard
     */
    public static final String SETUP_WIZARD = "SETUP_WIZARD";

    /**
     * A key for identifying if the firmware has functions for checking the gcode
     */
    public static final String CHECK_MODE = "CHECK_MODE";

    /**
     * A key for identifying if the firmware has support for settings
     */
    public static final String FIRMWARE_SETTINGS = "FIRMWARE_SETTINGS";

    /**
     * A key for identifying if the firmware supports returning to zero
     */
    public static final String RETURN_TO_ZERO = "RETURN_TO_ZERO";

    /**
     * A key for identifying if the firmware supports opening the door
     */
    public static final String OPEN_DOOR = "DOOR_DOOR";

    public static final String X_AXIS = "X_AXIS";
    public static final String Y_AXIS = "Y_AXIS";
    public static final String Z_AXIS = "Z_AXIS";
    public static final String A_AXIS = "A_AXIS";
    public static final String B_AXIS = "B_AXIS";
    public static final String C_AXIS = "C_AXIS";
}
