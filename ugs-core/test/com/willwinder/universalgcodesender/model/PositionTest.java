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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author wwinder
 * @author Joacim Breiler
 */
public class PositionTest {

    /**
     * Test of equals method, of class Position.
     */
    @Test
    public void testEquals_Object() {
        Position instance = new Position(1, 2, 3, Units.MM);

        Assertions.assertThat(instance.equals(new Position(1, 2, 3, Units.MM)))
                .isTrue();
        Assertions.assertThat(instance.equals(new Position(1, 2, 3, Units.UNKNOWN)))
                .isFalse();

        Assertions.assertThat(instance.equals((Object) new Position(1, 2, 3, Units.MM)))
                .isTrue();
        Assertions.assertThat(instance.equals((Object) new Position(1, 2, 3, Units.UNKNOWN)))
                .isFalse();
    }

    /**
     * Test of isSamePositionIgnoreUnits method, of class Position.
     */
    @Test
    public void testIsSamePositionIgnoreUnits() {
        Position instance = new Position(1, 2, 3, Units.MM);

        Assertions.assertThat(instance.isSamePositionIgnoreUnits(new Position(1, 2, 3, Units.MM)))
                .isTrue();
        Assertions.assertThat(instance.isSamePositionIgnoreUnits(new Position(1, 2, 3, Units.UNKNOWN)))
                .isTrue();
    }

    @Test
    public void getPositionInUnitsShouldConvert() {
        Position position = new Position(1.0, 1.0, 1.0, UnitUtils.Units.INCH);

        Position positionInMM = position.getPositionIn(UnitUtils.Units.MM);
        assertEquals(Double.valueOf(25.4), Double.valueOf(positionInMM.get(Axis.X)));

        Position positionInInches = positionInMM.getPositionIn(UnitUtils.Units.INCH);
        assertEquals(1, Math.round(positionInInches.get(Axis.X)));
    }

    @Test
    public void addShouldAddXYZPosition() {
        Position position1 = new Position(1, 2, 3, Units.MM);
        Position position2 = new Position(3, 2, 1, Units.MM);

        Position result = position1.add(position2);
        assertEquals(4, result.getX(), 0.1);
        assertEquals(4, result.getY(), 0.1);
        assertEquals(4, result.getZ(), 0.1);
        assertEquals(Double.NaN, result.getA(), 0.1);
        assertEquals(Double.NaN, result.getB(), 0.1);
        assertEquals(Double.NaN, result.getC(), 0.1);
        assertEquals(Units.MM, result.getUnits());
    }

    @Test
    public void addShouldAddXYZABCPosition() {
        Position position1 = new Position(1, 2, 3, 4, 5, 6, Units.MM);
        Position position2 = new Position(6, 5, 4, 3, 2, 1, Units.MM);

        Position result = position1.add(position2);
        assertEquals(7, result.getX(), 0.1);
        assertEquals(7, result.getY(), 0.1);
        assertEquals(7, result.getZ(), 0.1);
        assertEquals(7, result.getA(), 0.1);
        assertEquals(7, result.getB(), 0.1);
        assertEquals(7, result.getC(), 0.1);
        assertEquals(Units.MM, result.getUnits());
    }

    @Test
    public void addShouldAddXYZABCPositionInDifferetUnits() {
        Position position1 = new Position(1, 2, 3, 4, 5, 6, Units.MM);
        Position position2 = new Position(6, 5, 4, 3, 2, 1, Units.INCH);

        Position result = position1.add(position2);
        assertEquals(153.4, result.getX(), 0.1);
        assertEquals(129, result.getY(), 0.1);
        assertEquals(104.6, result.getZ(), 0.1);
        assertEquals("Rotational axes should not be converted to inches", 7, result.getA(), 0.1);
        assertEquals("Rotational axes should not be converted to inches", 7, result.getB(), 0.1);
        assertEquals("Rotational axes should not be converted to inches", 7, result.getC(), 0.1);
        assertEquals(Units.MM, result.getUnits());
    }

    @Test
    public void addShouldCreateNewInstance() {
        Position position1 = new Position(1, 2, 3, 4, 5, 6, Units.MM);
        Position position2 = new Position(6, 5, 4, 3, 2, 1, Units.MM);

        Position result = position1.add(position2);
        assertNotEquals(position1.getX(), result.getX(), 0.1);
        assertNotEquals(position1.getY(), result.getY(), 0.1);
        assertNotEquals(position1.getZ(), result.getZ(), 0.1);
        assertNotEquals(position1.getA(), result.getA(), 0.1);
        assertNotEquals(position1.getB(), result.getB(), 0.1);
        assertNotEquals(position1.getC(), result.getC(), 0.1);

        assertNotEquals(position2.getX(), result.getX(), 0.1);
        assertNotEquals(position2.getY(), result.getY(), 0.1);
        assertNotEquals(position2.getZ(), result.getZ(), 0.1);
        assertNotEquals(position2.getA(), result.getA(), 0.1);
        assertNotEquals(position2.getB(), result.getB(), 0.1);
        assertNotEquals(position2.getC(), result.getC(), 0.1);

        assertEquals(Units.MM, result.getUnits());
    }
}
