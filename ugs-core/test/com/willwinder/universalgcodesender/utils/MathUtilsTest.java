/*
    Copyright 2020 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Joacim Breiler
 */
public class MathUtilsTest {

    @Test
    public void orientationWithPointsInClockwiseOrder() {
        PartialPosition p1 = new PartialPosition(0d, 0d, UnitUtils.Units.MM);
        PartialPosition p2 = new PartialPosition(1d, 1d, UnitUtils.Units.MM);
        PartialPosition p3 = new PartialPosition(1d, 0d, UnitUtils.Units.MM);
        int orientation = MathUtils.orientation(p1, p2, p3);
        assertEquals(MathUtils.CLOCKWISE, orientation);
    }

    @Test
    public void orientationWithPointsInCounterClockwiseOrder() {
        PartialPosition p1 = new PartialPosition(0d, 0d, UnitUtils.Units.MM);
        PartialPosition p2 = new PartialPosition(-1d, 1d, UnitUtils.Units.MM);
        PartialPosition p3 = new PartialPosition(-1d, 0d, UnitUtils.Units.MM);
        int orientation = MathUtils.orientation(p1, p2, p3);
        assertEquals(MathUtils.COUNTER_CLOCKWISE, orientation);
    }

    @Test
    public void orientationWithPointsCollinear() {
        PartialPosition p1 = new PartialPosition(0d, 0d, UnitUtils.Units.MM);
        PartialPosition p2 = new PartialPosition(1d, 1d, UnitUtils.Units.MM);
        PartialPosition p3 = new PartialPosition(2d, 2d, UnitUtils.Units.MM);
        int orientation = MathUtils.orientation(p1, p2, p3);
        assertEquals(MathUtils.COLLINEAR, orientation);
    }

    @Test
    public void createConvexHullShouldRemoveInternalPoints() {
        List<PartialPosition> points = Arrays.asList(
                new PartialPosition(0d, 0d, UnitUtils.Units.MM),
                new PartialPosition(0d, 10d, UnitUtils.Units.MM),
                new PartialPosition(10d,10d, UnitUtils.Units.MM),
                new PartialPosition(5d, 5d, UnitUtils.Units.MM)
        );

        List<PartialPosition> convexHull = MathUtils.generateConvexHull(points);
        assertEquals(3, convexHull.size());
        assertEquals(new PartialPosition(0d,0d, UnitUtils.Units.MM), convexHull.get(0));
        assertEquals(new PartialPosition(10d,10d, UnitUtils.Units.MM), convexHull.get(1));
        assertEquals(new PartialPosition(0d,10d, UnitUtils.Units.MM), convexHull.get(2));
    }
}