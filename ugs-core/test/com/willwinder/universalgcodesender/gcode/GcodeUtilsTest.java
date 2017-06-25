/*
    Copyright 2016-2017 Will Winder

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
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class GcodeUtilsTest {
    
    /**
     * Test of generateXYZ method, of class GcodeUtils.
     */
    @Test
    public void testGenerateXYZ() {
        System.out.println("generateXYZ");
        String result;

        result = GcodeUtils.generateXYZ("G0", UnitUtils.Units.INCH, "10", "11", 1, 1, 1);
        assertEquals("G20G0X10Y10Z10F11", result);

        result = GcodeUtils.generateXYZ("G0", UnitUtils.Units.MM, "10", "11", 1, 1, 1);
        assertEquals("G21G0X10Y10Z10F11", result);

        result = GcodeUtils.generateXYZ("G91G0", UnitUtils.Units.MM, "10", "11", 1, 0, 0);
        assertEquals("G21G91G0X10F11", result);

        result = GcodeUtils.generateXYZ("G91G0", UnitUtils.Units.MM, "10", "11", 0, -1, -1);
        assertEquals("G21G91G0Y-10Z-10F11", result);
    }
    
}
