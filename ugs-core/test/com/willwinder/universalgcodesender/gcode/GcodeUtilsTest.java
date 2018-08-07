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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class GcodeUtilsTest {

    @Test
    public void generateMoveCommand() {
        String result;

        result = GcodeUtils.generateMoveCommand("G0", 10, 11, 1, 1, 1);
        assertEquals("G0X10Y10Z10F11", result);

        result = GcodeUtils.generateMoveCommand("G0", 10, 11, 1, 1, 1);
        assertEquals("G0X10Y10Z10F11", result);

        result = GcodeUtils.generateMoveCommand("G91G0", 10, 11, 1, 0, 0);
        assertEquals("G91G0X10F11", result);

        result = GcodeUtils.generateMoveCommand("G91G0", 10, 11, 0, -1, -1);
        assertEquals("G91G0Y-10Z-10F11", result);

        result = GcodeUtils.generateMoveCommand("G1", 1.1, 11.1, 1, 0, -1);
        assertEquals("G1X1.1Z-1.1F11.1", result);
    }
}
