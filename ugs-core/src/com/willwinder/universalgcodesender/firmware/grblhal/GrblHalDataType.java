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
package com.willwinder.universalgcodesender.firmware.grblhal;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;

/**
 * The data type of a grblHAL setting as reported by the settings enumeration command $ES.
 * <a href="https://github.com/grblHAL/core/wiki/Report-extensions">Documentation</a>
 *
 * @author Joacim Breiler
 */
public enum GrblHalDataType {
    /**
     * Typically rendered as a check box
     */
    BOOLEAN(0),

    /**
     * Typically rendered as a column of check boxes where the setting format contains one label per
     * bit, starting with bit 0. Labels named "N/A" are unavailable and should not be shown
     */
    BITFIELD(1),

    /**
     * A bitfield where the other bits should be disabled when bit 0 is not set
     */
    EXCLUSIVE_BITFIELD(2),

    /**
     * Typically rendered as radio buttons where the setting format contains one label per value,
     * starting with the value 0. Labels named "N/A" are unavailable and should not be shown
     */
    RADIO_BUTTONS(3),

    /**
     * A bitfield with one bit per axis, bit 0 is the X axis
     */
    AXIS_MASK(4),

    INTEGER(5),
    DECIMAL(6),
    STRING(7),

    /**
     * A string where the data entry could be hidden
     */
    PASSWORD(8),

    /**
     * An IPv4 address as a string in dot notation
     */
    IPV4(9),

    /**
     * A data type that this version of UGS does not know about
     */
    UNKNOWN(-1);

    private final int code;

    GrblHalDataType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static GrblHalDataType fromCode(String code) {
        if (!NumberUtils.isDigits(code)) {
            return UNKNOWN;
        }

        int value = Integer.parseInt(code);
        return Arrays.stream(values())
                .filter(dataType -> dataType.code == value)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
