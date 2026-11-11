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
package com.willwinder.ugs.designer.constraint;

import com.willwinder.ugs.designer.entities.Entity;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The segments making up the outline of an entity, in design coordinates. Closing a sub path
 * counts as a straight segment back to where it started. Segments of zero length are kept, so
 * a segment keeps its index when, for instance, the corners of a rectangle are rounded.
 */
public final class Outline {
    /**
     * One segment of an outline: a line with two points, a quadratic curve with three or a
     * cubic curve with four, starting at the first point and ending at the last.
     */
    public record Segment(Point2D... points) {

        public Point2D start() {
            return points[0];
        }

        public Point2D end() {
            return points[points.length - 1];
        }
    }

    private Outline() {
    }

    public static List<Segment> segments(Entity entity) {
        Shape shape = entity.getShape();
        List<Segment> segments = new ArrayList<>();
        if (shape == null) {
            return segments;
        }
        double[] coordinates = new double[6];
        Point2D current = null;
        Point2D subPathStart = null;
        for (PathIterator iterator = shape.getPathIterator(null); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coordinates);
            switch (type) {
                case PathIterator.SEG_MOVETO -> {
                    current = point(coordinates, 0);
                    subPathStart = current;
                }
                case PathIterator.SEG_LINETO -> current = add(segments, current, point(coordinates, 0));
                case PathIterator.SEG_QUADTO ->
                        current = add(segments, current, point(coordinates, 0), point(coordinates, 1));
                case PathIterator.SEG_CUBICTO ->
                        current = add(segments, current, point(coordinates, 0), point(coordinates, 1), point(coordinates, 2));
                case PathIterator.SEG_CLOSE -> current = add(segments, current, subPathStart);
                default -> {
                }
            }
        }
        return segments;
    }

    /**
     * The distinct vertices of the outline, where its segments start and end.
     */
    public static List<Point2D> vertices(Entity entity) {
        return vertices(segments(entity));
    }

    /**
     * The distinct vertices of the segments, where they start and end.
     */
    public static List<Point2D> vertices(List<Segment> segments) {
        Set<Point2D> vertices = new LinkedHashSet<>();
        for (Segment segment : segments) {
            vertices.add(segment.start());
            vertices.add(segment.end());
        }
        return new ArrayList<>(vertices);
    }

    private static Point2D add(List<Segment> segments, Point2D current, Point2D... rest) {
        if (current == null) {
            return rest[rest.length - 1];
        }
        Point2D[] points = new Point2D[rest.length + 1];
        points[0] = current;
        System.arraycopy(rest, 0, points, 1, rest.length);
        segments.add(new Segment(points));
        return rest[rest.length - 1];
    }

    private static Point2D point(double[] coordinates, int index) {
        return new Point2D.Double(coordinates[index * 2], coordinates[index * 2 + 1]);
    }
}
