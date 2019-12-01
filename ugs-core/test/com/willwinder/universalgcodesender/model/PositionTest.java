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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joacim Breiler
 */
public class PositionTest {

    @Test
    public void getPositionInUnitsShouldConvert() {
        Position position = new Position(1.0, 1.0, 1.0, UnitUtils.Units.INCH);

        Position positionInMM = position.getPositionIn(UnitUtils.Units.MM);
        assertThat(Double.valueOf(positionInMM.get(Axis.X))).isEqualTo(Double.valueOf(25.4));

        Position positionInInches = positionInMM.getPositionIn(UnitUtils.Units.INCH);
        assertThat(Math.round(positionInInches.get(Axis.X))).isEqualTo(1);
    }

    @Test
    public void shouldBeAbleToDoArithmetic() {

        Position p1 = new Position(1, 2, 3, UnitUtils.Units.INCH);
        Position p2 = new Position(4, 5, 6, UnitUtils.Units.INCH);

        Position expected = new Position(5, 7, 9, UnitUtils.Units.INCH);

        assertThat(p1.add(p2)).isEqualTo(expected);
        assertThat(expected.sub(p2)).isEqualTo(p1);
    }

    @Test
    public void shouldBeAbleToDoArithmeticInDifferentUnits() {

        Position p1 = new Position(1, 2, 3, UnitUtils.Units.INCH);
        Position p2 = new Position(4, 5, 6, UnitUtils.Units.MM);

        Position expected = new Position(1.1574803149606299, 2.1968503937007875, 3.236220472440945, UnitUtils.Units.INCH);

        assertThat(p1.add(p2)).isEqualTo(expected);
        assertThat(expected.sub(p2)).isEqualTo(p1);
    }
}
