/*
    Copyright 2017-2018 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.gcode.util.Code;

import java.util.Arrays;

/**
 * An enum with the different work coordinate systems
 *
 * @author wwinder
 */
public enum WorkCoordinateSystem {
    G53(0, Code.G53),
    G54(1, Code.G54),
    G55(2, Code.G55),
    G56(3, Code.G56),
    G57(4, Code.G57),
    G58(5, Code.G58),
    G59(6, Code.G59);

    final int pValue;
    final Code gcode;

    WorkCoordinateSystem(int pValue, Code gcode) {
        this.pValue = pValue;
        this.gcode = gcode;
    }
    
    public int getPValue() {
        return pValue;
    }

    public Code getGcode() {
        return gcode;
    }

    /**
     * Returns the work coordinate system from it's index value
     *
     * @param pValue the coordinate index
     * @return the work coordinate system if found else G54
     */
    public static WorkCoordinateSystem fromPValue(int pValue) {
        return Arrays.stream(values())
                .filter(value -> value.getPValue() == pValue)
                .findFirst()
                .orElse(G54);
    }

    /**
     * Returns the work coordinate system from it's GCode
     *
     * @param code the coordinate gcode
     * @return the work coordinate system if found else G54
     */
    public static WorkCoordinateSystem fromGCode(Code code) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(code.name()) )
                .findFirst()
                .orElse(G54);
    }
}
