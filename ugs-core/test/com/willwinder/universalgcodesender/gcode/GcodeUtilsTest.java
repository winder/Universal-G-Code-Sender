/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author wwinder
 */
public class GcodeUtilsTest {

    @Test
    public void generateMoveCommand() {
        String result;

        result = GcodeUtils.generateMoveCommand("G0", 11, new PartialPosition(10., 10., 10., UnitUtils.Units.MM));
        assertEquals("G21G0X10Y10Z10F11", result);

        result = GcodeUtils.generateMoveCommand("G91G0", 11, new PartialPosition(10., null, null, UnitUtils.Units.INCH));
        assertEquals("G20G91G0X10F11", result);

        result = GcodeUtils.generateMoveCommand("G91G0",  11, new PartialPosition(null, -10., -10., UnitUtils.Units.UNKNOWN));
        assertEquals("G91G0Y-10Z-10F11", result);

        result = GcodeUtils.generateMoveCommand("G1", 11.1, new PartialPosition(1.1, null, -1.1, UnitUtils.Units.MM));
        assertEquals("G21G1X1.1Z-1.1F11.1", result);

        result = GcodeUtils.generateMoveCommand("G1", 0, new PartialPosition(1.1, null, -1.1, UnitUtils.Units.MM));
        assertEquals("G21G1X1.1Z-1.1", result);

        result = GcodeUtils.generateMoveCommand("G0",  11, new PartialPosition(5., 5., 5., UnitUtils.Units.MM));
        assertEquals("G21G0X5Y5Z5F11", result);

        result = GcodeUtils.generateMoveCommand("G0",  11, new PartialPosition(-5., -5., -5., UnitUtils.Units.MM));
        assertEquals("G21G0X-5Y-5Z-5F11", result);
    }

    @Test
    public void generateMoveToCommand() {
        String result = GcodeUtils.generateMoveToCommand("G90", new PartialPosition(1.0, 2.0, 3.1, UnitUtils.Units.MM), 1000.1);
        assertEquals("G21G90X1Y2Z3.1F1000.1", result);

        result = GcodeUtils.generateMoveToCommand("G90", new PartialPosition(-1.0, -2.0, -3.1, UnitUtils.Units.MM), 1000.1);
        assertEquals("G21G90X-1Y-2Z-3.1F1000.1", result);

        result = GcodeUtils.generateMoveToCommand("G90", new PartialPosition(-1.0, -2.0, -3.1, UnitUtils.Units.INCH), 10000);
        assertEquals("G20G90X-1Y-2Z-3.1F10000", result);

        result = GcodeUtils.generateMoveToCommand("G90", new PartialPosition(-1.0, -2.0, -3.1, UnitUtils.Units.INCH), 0);
        assertEquals("G20G90X-1Y-2Z-3.1", result);

        result = GcodeUtils.generateMoveToCommand("G90", new PartialPosition(-1.0, -2.0, -3.1, UnitUtils.Units.INCH), -10);
        assertEquals("G20G90X-1Y-2Z-3.1", result);

        result = GcodeUtils.generateMoveToCommand("G90G1", new PartialPosition(-1.0, -2.0, -3.1, UnitUtils.Units.INCH), -10);
        assertEquals("G20G90G1X-1Y-2Z-3.1", result);
    }
}
