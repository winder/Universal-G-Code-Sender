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
 * Constants for defining additional capabilities from {@link CapabilitiesConstants} that a
 * GRBL controller may support. The constants may be added to a {@link Capabilities} object.
 *
 * @author Joacim Breiler
 */
public class GrblCapabilitiesConstants {

    /**
     * A constant for defining if the GRBL controller has special hardware support
     * for jogging. If this isn't enabled jogging will be emulated.
     */
    public static final String HARDWARE_JOGGING = "HARDWARE_JOGGING";

    /**
     * A constant for defining if the GRBL controller supports real time commands.
     */
    public static final String REAL_TIME = "REAL_TIME";

    /**
     * A constant for defining if the status strings are returned in version 1 format
     */
    public static final String V1_FORMAT = "V1_FORMAT";
}
