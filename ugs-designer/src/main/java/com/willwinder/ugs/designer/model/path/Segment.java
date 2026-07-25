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
package com.willwinder.ugs.designer.model.path;

import com.willwinder.ugs.designer.utils.PathUtils;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * A segment to be used in an editable path
 *
 * @author Joacim Breiler
 */
public class Segment {
    private final SegmentType type;
    private final Point2D[] points;
    private final Point2D startPoint;
    private final Point2D lastPoint;
    private final Path2D.Double path;

    public Segment(SegmentType type, Point2D startPoint, Point2D lastPoint, Point2D[] points) {
        this.type = type;
        this.points = points;
        this.startPoint = startPoint;
        this.lastPoint = lastPoint;

        path = new Path2D.Double();
        path.moveTo(startPoint.getX(), startPoint.getY());
        switch (type) {
            case LINE_TO -> path.lineTo(lastPoint.getX(), lastPoint.getY());
            case QUAD_TO -> path.quadTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY());
            case CUBIC_TO -> path.curveTo(
                    points[0].getX(), points[0].getY(),
                    points[1].getX(), points[1].getY(),
                    points[2].getX(), points[2].getY());
        }
    }

    public SegmentType getType() {
        return type;
    }

    public Point2D[] getPoints() {
        return points;
    }

    public Point2D getPoint(int index) {
        if (index >= points.length) {
            return new Point2D.Double();
        }
        return points[index];
    }

    public Point2D getLastPoint() {
        return lastPoint;
    }

    public Point2D getStartPoint() {
        return startPoint;
    }

    public Path2D path() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Segment other)) {
            return false;
        }
        return type == other.type &&
                hasSamePosition(startPoint, other.startPoint) &&
                hasSamePosition(lastPoint, other.lastPoint) &&
                hasSamePositions(points, other.points);
    }

    private static boolean hasSamePositions(Point2D[] points, Point2D[] otherPoints) {
        if (points.length != otherPoints.length) {
            return false;
        }

        for (int i = 0; i < points.length; i++) {
            if (!hasSamePosition(points[i], otherPoints[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasSamePosition(Point2D point, Point2D otherPoint) {
        return PathUtils.quantize(point.getX()) == PathUtils.quantize(otherPoint.getX()) &&
                PathUtils.quantize(point.getY()) == PathUtils.quantize(otherPoint.getY());
    }

    /**
     * The hash is generated from the points instead of the path, as the path is generated from the
     * same points but is more expensive to traverse. Points are rounded to account for precision
     * errors.
     */
    @Override
    public int hashCode() {
        int hash = 31 + type.ordinal();
        hash = 31 * hash + PathUtils.hashCode(startPoint);
        hash = 31 * hash + PathUtils.hashCode(lastPoint);
        for (Point2D point : points) {
            hash = 31 * hash + PathUtils.hashCode(point);
        }
        return hash;
    }

    public void setPosition(int pointIndex, Point2D position) {
        if (pointIndex < 0 || pointIndex >= points.length) {
            return;
        }

        points[pointIndex].setLocation(position.getX(), position.getY());

        // Update the last point
        if (type == SegmentType.CUBIC_TO && pointIndex == 2) {
            lastPoint.setLocation(position.getX(), position.getY());
        } else if (type == SegmentType.QUAD_TO && pointIndex == 1) {
            lastPoint.setLocation(position.getX(), position.getY());
        } else if (type == SegmentType.LINE_TO && pointIndex == 0) {
            lastPoint.setLocation(position.getX(), position.getY());
        }
    }
}