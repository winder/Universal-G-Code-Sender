/*
    Copyright 2016-2018 Will Winder

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
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
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

        result = GcodeUtils.generateMoveCommand("G0", 10, 11, 1, 1, 1, UnitUtils.Units.MM);
        assertEquals("G21G0X10Y10Z10F11", result);

        result = GcodeUtils.generateMoveCommand("G0", 10, 11, 1, 1, 1, UnitUtils.Units.MM);
        assertEquals("G21G0X10Y10Z10F11", result);

        result = GcodeUtils.generateMoveCommand("G91G0", 10, 11, 1, 0, 0, UnitUtils.Units.INCH);
        assertEquals("G20G91G0X10F11", result);

        result = GcodeUtils.generateMoveCommand("G91G0", 10, 11, 0, -1, -1, UnitUtils.Units.UNKNOWN);
        assertEquals("G91G0Y-10Z-10F11", result);

        result = GcodeUtils.generateMoveCommand("G1", 1.1, 11.1, 1, 0, -1, UnitUtils.Units.MM);
        assertEquals("G21G1X1.1Z-1.1F11.1", result);
    }

    @Test
    public void generateMoveToCommand() {
        String result = GcodeUtils.generateMoveToCommand(new Position(1.0, 2.0, 3.1, UnitUtils.Units.MM), 1000.1);
        assertEquals("G21G90G1X1Y2Z3.1F1000.1", result);

        result = GcodeUtils.generateMoveToCommand(new Position(-1.0, -2.0, -3.1, UnitUtils.Units.MM), 1000.1);
        assertEquals("G21G90G1X-1Y-2Z-3.1F1000.1", result);

        result = GcodeUtils.generateMoveToCommand(new Position(-1.0, -2.0, -3.1, UnitUtils.Units.INCH), 10000);
        assertEquals("G20G90G1X-1Y-2Z-3.1F10000", result);

        result = GcodeUtils.generateMoveToCommand(new Position(-1.0, -2.0, -3.1, UnitUtils.Units.INCH), 0);
        assertEquals("G20G90G1X-1Y-2Z-3.1", result);

        result = GcodeUtils.generateMoveToCommand(new Position(-1.0, -2.0, -3.1, UnitUtils.Units.INCH), -10);
        assertEquals("G20G90G1X-1Y-2Z-3.1", result);
    }

    @Test
    public void formatPartialCoordinates() {
        assertEquals("Y0 Z0", GcodeUtils.formatPartialPosition(new PartialPosition(null, 0.0, 0.0)));
        assertEquals("X0 Z0", GcodeUtils.formatPartialPosition(new PartialPosition(0.0, null, 0.0)));
        assertEquals("X0 Y0", GcodeUtils.formatPartialPosition(new PartialPosition(0.0, 0.0, null)));

        assertEquals("Y10 Z0", GcodeUtils.formatPartialPosition(new PartialPosition(null, 10.0, 0.0)));
        assertEquals("X10 Z0", GcodeUtils.formatPartialPosition(new PartialPosition(10.0, null, 0.0)));
        assertEquals("X0 Y10", GcodeUtils.formatPartialPosition(new PartialPosition(0.0, 10.0, null)));

        assertEquals("Y10 Z-20", GcodeUtils.formatPartialPosition(new PartialPosition(null, 10.0, -20.0)));
        assertEquals("X10 Z-20", GcodeUtils.formatPartialPosition(new PartialPosition(10.0, null, -20.0)));
        assertEquals("X-20 Y10", GcodeUtils.formatPartialPosition(new PartialPosition(-20.0, 10.0, null)));

        assertEquals("Y10.5 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition(null, 10.5, -20.05)));
        assertEquals("X10.5 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition(10.5, null, -20.05)));
        assertEquals("X-20.05 Y10.5", GcodeUtils.formatPartialPosition(new PartialPosition(-20.05, 10.5, null)));

        assertEquals("X5.2 Y10.5 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition(5.2, 10.5, -20.05)));
        assertEquals("X10.5 Y5.2 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition(10.5, 5.2, -20.05)));
        assertEquals("X-20.05 Y10.5 Z5.2", GcodeUtils.formatPartialPosition(new PartialPosition(-20.05, 10.5, 5.2)));

        assertEquals("Y10.5 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition.Builder().setY(10.5).setZ(-20.05).build()));
        assertEquals("X10.5 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition.Builder().setX(10.5).setZ(-20.05).build()));
        assertEquals("X-20.05 Y10.5", GcodeUtils.formatPartialPosition(new PartialPosition.Builder().setY(10.5).setX(-20.05).build()));

        assertEquals("Y10.5 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition.Builder().setValue(Axis.Y,10.5).setValue(Axis.Z,-20.05).build()));
        assertEquals("X10.5 Z-20.05", GcodeUtils.formatPartialPosition(new PartialPosition.Builder().setValue(Axis.X,10.5).setValue(Axis.Z,-20.05).build()));
        assertEquals("X-20.05 Y10.5", GcodeUtils.formatPartialPosition(new PartialPosition.Builder().setValue(Axis.Y,10.5).setValue(Axis.X,-20.05).build()));

    }
}
