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
import static com.willwinder.universalgcodesender.utils.MathUtils.isEqual;
import static com.willwinder.universalgcodesender.utils.MathUtils.liangBarskyClipLine;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

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
                new PartialPosition(10d, 10d, UnitUtils.Units.MM),
                new PartialPosition(5d, 5d, UnitUtils.Units.MM)
        );

        List<PartialPosition> convexHull = MathUtils.generateConvexHull(points);
        assertEquals(3, convexHull.size());
        assertEquals(new PartialPosition(0d, 0d, UnitUtils.Units.MM), convexHull.get(0));
        assertEquals(new PartialPosition(10d, 10d, UnitUtils.Units.MM), convexHull.get(1));
        assertEquals(new PartialPosition(0d, 10d, UnitUtils.Units.MM), convexHull.get(2));
    }

    @Test
    public void isEqualShouldTestTheSimilarityOfDoubleValues() {
        assertTrue(isEqual(1.1, 1.1, 0.0));
        assertTrue(isEqual(1.1, 1.1, 0.001));
        assertTrue(isEqual(1.1, 1.1, 0.1));

        assertFalse(isEqual(1.1001, 1.1, 0.0));
        assertTrue(isEqual(1.1001, 1.1, 0.0001));

        assertFalse(isEqual(1.1001, 1.1, 0.0));
        assertFalse(isEqual(1.1001, 1.1, 0.00001));
        assertTrue(isEqual(1.1001, 1.1, 0.0001));

        assertFalse(isEqual(1.1, 1.1001, 0.0));
        assertFalse(isEqual(1.1, 1.1001, 0.00001));
        assertTrue(isEqual(1.1, 1.1001, 0.0001));

        assertFalse(isEqual(0.9999, 1.0, 0.0));
        assertTrue(isEqual(0.9999, 1.0, 0.01));
        assertTrue(isEqual(0.9999, 1.0, 0.001));
        assertTrue(isEqual(0.9999, 1.0, 0.0001));
        assertFalse(isEqual(0.9999, 1.0, 0.00001));

    }

    @Test
    public void normalizeAngle() {
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI), 0.001);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI * 3), 0.001);
        assertEquals(Math.PI, MathUtils.normalizeAngle(-Math.PI), 0.001);
        assertEquals(0, MathUtils.normalizeAngle(0), 0.001);
    }

    @Test
    public void liangBarskyClipLineShouldClipLines() {
        Rectangle2D.Double box = new Rectangle2D.Double(0, 0, 2, 2);
        Point2D[] result = liangBarskyClipLine(new Point2D.Double(-1, -1), new Point2D.Double(3, 3), box);
        assertArrayEquals(new Point2D[]{new Point2D.Double(0, 0), new Point2D.Double(2, 2)}, result);

        result = liangBarskyClipLine(new Point2D.Double(1, -1), new Point2D.Double(1, 3), box);
        assertArrayEquals(new Point2D[]{new Point2D.Double(1, 0), new Point2D.Double(1, 2)}, result);

        result = liangBarskyClipLine(new Point2D.Double(-1, 1), new Point2D.Double(3, 1), box);
        assertArrayEquals(new Point2D[]{new Point2D.Double(0, 1), new Point2D.Double(2, 1)}, result);

        result = liangBarskyClipLine(new Point2D.Double(-1, 0), new Point2D.Double(3, 0), box);
        assertArrayEquals(new Point2D[]{new Point2D.Double(0, 0), new Point2D.Double(2, 0)}, result);

        result = liangBarskyClipLine(new Point2D.Double(-1, 2), new Point2D.Double(3, 2), box);
        assertArrayEquals(new Point2D[]{new Point2D.Double(0, 2), new Point2D.Double(2, 2)}, result);
    }

    @Test
    public void isEqualShouldCompareTwoPointsWithDelta() {
        assertTrue(isEqual(new Point2D.Double(0d, 0d), new Point2D.Double(0.000001d, 0.000001d), 1e-5d));
        assertFalse(isEqual(new Point2D.Double(0d, 0d), new Point2D.Double(0.000001d, 0.000001d), 1e-6d));
        assertTrue(isEqual(new Point2D.Double(0d, 0d), new Point2D.Double(0, 0), 1e-6d));
    }
}