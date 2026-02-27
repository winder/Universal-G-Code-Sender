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
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A collection of common mathematical operations
 *
 * @author Joacim Breiler
 */
public class MathUtils {

    public static final int COUNTER_CLOCKWISE = -1;
    public static final int CLOCKWISE = 1;
    public static final int COLLINEAR = 0;

    /**
     * To find orientation of ordered triplet (p1, p2, p3).
     * <a href="https://www.geeksforgeeks.org/orientation-3-ordered-points/">orientation-3-ordered-points</a>
     *
     * @param p1 a point in a polygon
     * @param p2 a point in a polygon
     * @param p3 a point in a polygon
     * @return The function returns if the sequence of the points are COLINEAR, CLOCKWISE or COUNTER_CLOCKWISE
     */
    public static int orientation(PartialPosition p1, PartialPosition p2, PartialPosition p3) {
        double value = (p2.getY() - p1.getY()) * (p3.getX() - p2.getX()) -
                (p2.getX() - p1.getX()) * (p3.getY() - p2.getY());

        if (value == 0) {
            return COLLINEAR;
        }
        return (value > 0) ? CLOCKWISE : COUNTER_CLOCKWISE;
    }

    /**
     * Returns a list of points making a convex hull in the X/Y plane of a list of points.
     *
     * @param points a list of points to build a convex hull for
     * @return an ordered list of points creating a hull for all points
     */
    public static List<PartialPosition> generateConvexHull(List<PartialPosition> points) {
        if (points.size() < 3) {
            return Collections.emptyList();
        }

        ConvexHull convexHull = new ConvexHull(points.stream()
                .map(s -> s.getPositionIn(UnitUtils.Units.MM))
                .map(s -> new Coordinate(s.getX(), s.getY()))
                .toArray(Coordinate[]::new), new GeometryFactory());

        return  Arrays.stream(convexHull.getConvexHull().getCoordinates()).map(c -> new PartialPosition(c.x, c.y, UnitUtils.Units.MM)).toList();
    }

    public static double round(double value, int decimals) {
        double power = Math.pow(10, decimals);
        return Math.round(value * power) / power;
    }

    /**
     * Compares two double values if they are equal or very close to each other using a delta threshold.
     *
     * @param d1    a double value
     * @param d2    a double value
     * @param epsilon a decimal delta value with the smallest allowed difference, smaller value means more precision.
     * @return true if they are equal or very close to equal
     */
    public static boolean isEqual(double d1, double d2, double epsilon) {
        if (Double.compare(d1, d2) == 0) {
            return true;
        }

        return Math.abs(d1 - d2) <= epsilon;
    }

    /**
     * Compares if two points are equal or very close to each other using a delta threshold.
     *
     * @param a     a point
     * @param b     a point
     * @param epsilon a decimal delta value with the smallest allowed difference, smaller value means more precision.
     * @return true if they are equal or very close to equal
     */
    public static boolean isEqual(Point2D a, Point2D b, double epsilon) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return dx * dx + dy * dy <= epsilon * epsilon;
    }

    /**
     * Normalizes an angle given in radians to make sure it is in the interval [0,2π]
     *
     * @param angle an angle in radians
     * @return the normalized angle
     */
    public static double normalizeAngle(double angle) {
        double twoPi = 2 * Math.PI;
        angle = angle % twoPi;
        if (angle < 0) {
            angle += twoPi;
        }
        return angle;
    }

    /**
     * Gets the center position given a list of line segments.
     *
     * @param points a list of points
     * @return a new position in the center of the given points
     */
    public static Position getCenter(List<PartialPosition> points) {
        UnitUtils.Units units = points.get(0).getUnits();
        Position minPos = new Position(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, units);
        Position maxPos = new Position(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, units);

        for (PartialPosition point : points) {
            PartialPosition positionInUnits = point.getPositionIn(units);
            minPos.setX(Math.min(minPos.getX(), positionInUnits.getX()));
            minPos.setY(Math.min(minPos.getY(), positionInUnits.getY()));
            minPos.setZ(Math.min(minPos.getZ(), positionInUnits.getZ()));
            maxPos.setX(Math.max(maxPos.getX(), positionInUnits.getX()));
            maxPos.setY(Math.max(maxPos.getY(), positionInUnits.getY()));
            maxPos.setZ(Math.max(maxPos.getZ(), positionInUnits.getZ()));
        }

        return new Position(
                minPos.getX() + ((maxPos.getX() - minPos.getX()) / 2),
                minPos.getY() + ((maxPos.getY() - minPos.getY()) / 2),
                minPos.getZ() + ((maxPos.getZ() - minPos.getZ()) / 2),
                units);
    }

    public static Position getLowerLeftCorner(List<PartialPosition> points) {
        UnitUtils.Units units = points.get(0).getUnits();
        Position minPos = new Position(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, units);

        for (PartialPosition point : points) {
            PartialPosition positionInUnits = point.getPositionIn(units);
            minPos.setX(Math.min(minPos.getX(), positionInUnits.getX()));
            minPos.setY(Math.min(minPos.getY(), positionInUnits.getY()));
            minPos.setZ(Math.min(minPos.getZ(), positionInUnits.getZ()));
        }

        return new Position(
                minPos.getX(),
                minPos.getY(),
                minPos.getZ(),
                units);
    }

    public static Point2D[] liangBarskyClipLine(Point2D point1, Point2D point2, Rectangle2D bounds) {
        double dx = point2.getX() - point1.getX();
        double dy = point2.getY() - point1.getY();

        double t0 = 0.0;
        double t1 = 1.0;

        double[] p = {-dx, dx, -dy, dy};
        double[] q = {point1.getX() - bounds.getMinX(), bounds.getMaxX() - point1.getX(), point1.getY() - bounds.getMinY(), bounds.getMaxY() - point1.getY()};

        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) {
                    return null; // Parallel and outside
                }
                continue; // Parallel and inside → no constraint
            }

            double r = q[i] / p[i];
            if (p[i] < 0) {
                t0 = Math.max(t0, r);
            } else {
                t1 = Math.min(t1, r);
            }
        }

        if (t0 > t1) return null;

        double sx = point1.getX() + t0 * dx;
        double sy = point1.getY() + t0 * dy;
        double ex = point1.getX() + t1 * dx;
        double ey = point1.getY() + t1 * dy;

        return new Point2D[]{new Point2D.Double(sx, sy), new Point2D.Double(ex, ey)};
    }

    /**
     * Clamps a value between a min and max value
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a value between a min and max value
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
