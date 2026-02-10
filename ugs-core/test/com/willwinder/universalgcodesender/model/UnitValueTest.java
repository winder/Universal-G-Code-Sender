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
package com.willwinder.universalgcodesender.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitValueTest {

    public static final double DELTA = 1e-12;

    @Test
    public void convertTo_shouldReturnSameValue_whenSameUnit() {
        UnitValue v = new UnitValue(Unit.MM, 123.456);

        Number converted = v.convertTo(Unit.MM);

        assertEquals(123.456, converted.doubleValue(), DELTA);
    }

    @Test
    public void convertTo_shouldConvertInchToMm() {
        UnitValue oneInch = new UnitValue(Unit.INCH, 1.0);

        Number inMm = oneInch.convertTo(Unit.MM);

        assertEquals(25.4, inMm.doubleValue(), DELTA);
    }

    @Test
    public void convertTo_shouldConvertMmToInch() {
        UnitValue mm = new UnitValue(Unit.MM, 25.4);

        Number inInches = mm.convertTo(Unit.INCH);

        assertEquals(1.0, inInches.doubleValue(), DELTA);
    }

    @Test
    public void convertTo_shouldConvertNegativeMmToInch() {
        UnitValue mm = new UnitValue(Unit.MM, -25.4);

        Number inInches = mm.convertTo(Unit.INCH);

        assertEquals(-1.0, inInches.doubleValue(), DELTA);
    }

    @Test
    public void convertTo_shouldConvertFeetToInch() {
        UnitValue mm = new UnitValue(Unit.FEET, 1);

        Number inInches = mm.convertTo(Unit.INCH);

        assertEquals(12.0, inInches.doubleValue(), DELTA);
    }

    @Test
    public void convertTo_shouldHandleZero() {
        UnitValue zeroMm = new UnitValue(Unit.MM, 0.0);

        Number inInches = zeroMm.convertTo(Unit.INCH);

        assertEquals(0.0, inInches.doubleValue(), DELTA);
    }
}