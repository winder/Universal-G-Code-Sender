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

import java.util.ArrayList;
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
     * https://www.geeksforgeeks.org/orientation-3-ordered-points/
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

        // Start from leftmost point, keep moving counterclockwise until reach the start point again.
        int leftMostPoint = findLeftMostPointIndex(points);
        int currentPoint = leftMostPoint;
        List<PartialPosition> result = new ArrayList<>();
        do {
            // Add current point to result
            result.add(points.get(currentPoint));

            // Now q is the most counterclockwise with respect to currentPoint.
            // Set currentPoint as q for next iteration, so that q is added to result 'hull'
            currentPoint = findNextOuterPointIndex(points, currentPoint);

        } while (currentPoint != leftMostPoint);  // While we don't come to first point

        return result;
    }

    /**
     * Given a list of points and the starting index, find the most adjacent outer point counter clockwise
     * from the starting point
     *
     * @param points        a list of points
     * @param startingIndex the index of the point to originate from
     * @return the next outer point counter clockwise from the start point
     */
    private static int findNextOuterPointIndex(List<PartialPosition> points, int startingIndex) {
        // Search for a point 'q' such that orientation(currentPoint, i, q) is counterclockwise
        // for all points 'i'. The idea is to keep track of last visited most counterclock-
        // wise point in q. If any point 'i' is more counterclock-wise than q, then update q.
        int q = (startingIndex + 1) % points.size();

        for (int i = 0; i < points.size(); i++) {
            // If i is more counterclockwise than current q, then update q
            if (orientation(points.get(startingIndex), points.get(i), points.get(q)) == COUNTER_CLOCKWISE) {
                q = i;
            }
        }
        return q;
    }

    /**
     * Given a list of points find the point furthest to the left
     *
     * @param points a list of points
     * @return the index of the point
     */
    private static int findLeftMostPointIndex(List<PartialPosition> points) {
        int leftMostPoint = 0;
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).getX() < points.get(leftMostPoint).getX()) {
                leftMostPoint = i;
            }
        }
        return leftMostPoint;
    }

    public static double round(double value, int decimals) {
        double power = Math.pow(10, decimals);
        return Math.round(value * power) / power;
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
}
