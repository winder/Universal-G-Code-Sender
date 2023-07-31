/*
    Copyright 2023 Will Winder

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

import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import java.util.List;

import static com.willwinder.universalgcodesender.model.UnitUtils.Units.INCH;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static org.junit.Assert.*;

/**
 * @author Joacim Breiler
 */
public class GcodePreprocessorUtilsTest {
    @Test
    public void generatePointsAlongArcBDringShouldGenerateAnArc() {
        Position start = new Position(0, 0, 0, MM);
        Position end = new Position(10, 0, 0, MM);
        Position center = new Position(5, 0, 0, MM);
        double radius = 5;

        List<Position> points = GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, true, radius, 0, 1, new PlaneFormatter(Plane.XY));
        assertThatPointsAreWithinBoundary(start, end, radius, points);
        assertEquals(17, points.size());
        assertTrue("Coordinates are generated in wrong units", points.stream().allMatch(p -> p.getUnits() == MM));
    }

    @Test
    public void generatePointsAlongArcBDringShouldGenerateAnArcWhenPositionsAreInInches() {
        Position start = new Position(0, 0, 0, UnitUtils.Units.INCH);
        Position end = new Position(10 * UnitUtils.scaleUnits(MM, INCH), 0, 0, UnitUtils.Units.INCH);
        Position center = new Position(5 * UnitUtils.scaleUnits(MM, INCH), 0, 0, UnitUtils.Units.INCH);
        double radius = 5 * UnitUtils.scaleUnits(MM, INCH);

        List<Position> points = GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, true, radius, 0, 1, new PlaneFormatter(Plane.XY));
        assertEquals(17, points.size());
        assertThatPointsAreWithinBoundary(start, end, radius, points);
        assertTrue("Coordinates are generated in wrong units", points.stream().allMatch(p -> p.getUnits() == INCH));
    }

    static void assertThatPointsAreWithinBoundary(Position start, Position end, double radius, List<Position> points) {
        assertTrue(points.stream().allMatch(p -> p.x >= start.x));
        assertTrue(points.stream().allMatch(p -> p.x <= end.x));
        assertTrue(points.stream().allMatch(p -> p.y >= end.y));
        assertTrue(points.stream().allMatch(p -> p.y >= end.y));
        assertTrue(points.stream().allMatch(p -> p.y <= end.y + radius));
        assertTrue(points.stream().allMatch(p -> p.y <= end.y + radius));
    }
}