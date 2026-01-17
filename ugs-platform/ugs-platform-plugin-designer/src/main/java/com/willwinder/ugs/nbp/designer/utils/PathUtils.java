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
package com.willwinder.ugs.nbp.designer.utils;

import com.github.weisj.jsvg.geometry.util.ReversePathIterator;
import com.willwinder.ugs.nbp.designer.model.path.Segment;
import static com.willwinder.ugs.nbp.designer.model.path.SegmentType.fromPathIteratorType;
import static com.willwinder.universalgcodesender.utils.MathUtils.isEqual;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PathUtils {
    public static final double EPS = 1e-4;

    /**
     * Creates a Path2D from a path iterator
     *
     * @param pathIterator the path iterator to generate the shape from
     * @return a path 2d shape
     */
    public static Path2D toPath2D(PathIterator pathIterator) {
        Path2D path = new Path2D.Double();
        double[] c = new double[6];

        while (!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(c);

            switch (type) {
                case PathIterator.SEG_MOVETO -> path.moveTo(c[0], c[1]);

                case PathIterator.SEG_LINETO -> path.lineTo(c[0], c[1]);

                case PathIterator.SEG_QUADTO -> path.quadTo(
                        c[0], c[1],
                        c[2], c[3]
                );

                case PathIterator.SEG_CUBICTO -> path.curveTo(
                        c[0], c[1],
                        c[2], c[3],
                        c[4], c[5]
                );

                case PathIterator.SEG_CLOSE -> path.closePath();
            }

            pathIterator.next();
        }

        return path;
    }

    /**
     * Creates a path given a list of segments
     *
     * @param segments a list of segments
     * @return a Path2D shape
     */
    public static Path2D toPath2D(List<Segment> segments) {
        Path2D path = new Path2D.Double();

        for (Segment seg : segments) {
            switch (seg.getType()) {
                case MOVE_TO -> path.moveTo(seg.getPoint(0).getX(), seg.getPoint(0).getY());

                case LINE_TO -> path.lineTo(seg.getPoint(0).getX(), seg.getPoint(0).getY());

                case QUAD_TO -> path.quadTo(
                        seg.getPoint(0).getX(), seg.getPoint(0).getY(),
                        seg.getPoint(1).getX(), seg.getPoint(1).getY()
                );

                case CUBIC_TO -> path.curveTo(
                        seg.getPoint(0).getX(), seg.getPoint(0).getY(),
                        seg.getPoint(1).getX(), seg.getPoint(1).getY(),
                        seg.getPoint(2).getX(), seg.getPoint(2).getY()
                );

                case CLOSE -> path.closePath();
            }
        }

        return path;
    }

    /**
     * Breaks down a shape into its segments
     *
     * @param shape the shape to break down
     * @return a list of segments
     */
    public static List<Segment> getSegments(Shape shape) {
        return getSegments(shape.getPathIterator(null));
    }

    /**
     * Breaks down a path iterator into its segments
     *
     * @param pathIterator the path iterator to break down
     * @return a list of segments
     */
    public static List<Segment> getSegments(PathIterator pathIterator) {
        List<Segment> segments = new ArrayList<>();

        double[] coords = new double[6];
        Point2D start;
        Point2D last = null;

        while (!pathIterator.isDone()) {
            start = last;
            int type = pathIterator.currentSegment(coords);
            Point2D[] points = new Point2D[0];
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1])
                    };
                    last = start = new Point2D.Double(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1])
                    };
                    last = new Point2D.Double(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1]), // ctrl
                            new Point2D.Double(coords[2], coords[3])  // end
                    };
                    last = new Point2D.Double(coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1]), // ctrl
                            new Point2D.Double(coords[2], coords[3]), // ctrl
                            new Point2D.Double(coords[4], coords[5]) // end
                    };
                    last = new Point2D.Double(coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    segments.add(new Segment(fromPathIteratorType(type), last, last, new Point2D[0]));
                    start = null;
                    break;

                default:
                    points = null;
            }

            if (start != null && points != null) {
                segments.add(new Segment(fromPathIteratorType(type), start, last, points));
            }
            pathIterator.next();
        }
        return segments;
    }


    /**
     * Reverses the path direction by iterating through all segments in reverse
     *
     * @param path the path to reverse
     * @return the reversed path
     */
    public static Path2D reversePath(Path2D path) {
        return toPath2D(new ReversePathIterator(path.getPathIterator(null)));
    }

    /**
     * Returns the first point in a path
     *
     * @param path the path to get the first point from
     * @return the first point
     */
    public static Point2D getStartPoint(Path2D path) {
        PathIterator it = path.getPathIterator(null);
        double[] c = new double[6];
        while (!it.isDone()) {
            if (it.currentSegment(c) == PathIterator.SEG_MOVETO) {
                return new Point2D.Double(c[0], c[1]);
            }
            it.next();
        }
        throw new IllegalStateException("Path has no MOVETO");
    }

    /**
     * Returns the last point in a path
     *
     * @param path the path to get the last point from
     * @return the last point
     */
    public static Point2D getEndPoint(Path2D path) {
        PathIterator it = path.getPathIterator(null);
        double[] c = new double[6];
        double x = 0, y = 0;
        while (!it.isDone()) {
            int t = it.currentSegment(c);
            switch (t) {
                case PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    x = c[0];
                    y = c[1];
                }
                case PathIterator.SEG_QUADTO -> {
                    x = c[2];
                    y = c[3];
                }
                case PathIterator.SEG_CUBICTO -> {
                    x = c[4];
                    y = c[5];
                }
            }
            it.next();
        }
        return new Point2D.Double(x, y);
    }

    private static void appendPath(Path2D target, Path2D source) {
        PathIterator it = source.getPathIterator(null);
        double[] c = new double[6];
        boolean skipMove = true;

        while (!it.isDone()) {
            int t = it.currentSegment(c);
            switch (t) {
                case PathIterator.SEG_MOVETO -> {
                    if (!skipMove) {
                        target.moveTo(c[0], c[1]);
                    }
                }
                case PathIterator.SEG_LINETO -> target.lineTo(c[0], c[1]);
                case PathIterator.SEG_QUADTO -> target.quadTo(c[0], c[1], c[2], c[3]);
                case PathIterator.SEG_CUBICTO -> target.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
                case PathIterator.SEG_CLOSE -> target.closePath();
            }
            skipMove = false;
            it.next();
        }
    }

    /**
     * Joins a list of paths making a continuous path if it can, or else it will start a new path.
     *
     * @param paths   a list of paths to join
     * @param epsilon the largest difference between two points before joining them
     * @return a new path
     */
    public static Optional<Path2D> joinPaths(List<Path2D> paths, double epsilon) {
        List<Path2D> remaining = new ArrayList<>(paths.stream()
                .filter(PathUtils::hasDrawableSegments)
                .toList());

        if (remaining.isEmpty()) {
            return Optional.empty();
        }

        Path2D result = new Path2D.Double();
        while (!remaining.isEmpty()) {

            // Start a new subpath
            Path2D current = remaining.remove(0);
            Point2D start = getStartPoint(current);
            Point2D end = getEndPoint(current);
            result.append(current, false);

            boolean progress;
            do {
                progress = false;

                for (int i = 0; i < remaining.size(); i++) {
                    Path2D p = remaining.get(i);

                    Point2D ps = getStartPoint(p);
                    Point2D pe = getEndPoint(p);

                    if (isEqual(end, ps, epsilon)) {
                        appendPath(result, p);
                        end = pe;
                    } else if (isEqual(end, pe, epsilon)) {
                        Path2D r = reversePath(p);
                        appendPath(result, r);
                        end = getEndPoint(r);
                    } else {
                        continue;
                    }

                    remaining.remove(i);
                    progress = true;
                    break;
                }
            } while (progress);

            // Close only the current subpath if applicable
            if (isEqual(start, end, epsilon)) {
                result.closePath();
            }
        }

        return Optional.of(result);
    }

    /**
     * Checks if the path has a drawable elements
     *
     * @param path the path to check
     * @return true if it has any drawable elements
     */
    protected static boolean hasDrawableSegments(Path2D path) {
        PathIterator it = path.getPathIterator(null);
        double[] c = new double[6];
        while (!it.isDone()) {
            int t = it.currentSegment(c);

            switch (t) {
                case PathIterator.SEG_LINETO,
                     PathIterator.SEG_QUADTO,
                     PathIterator.SEG_CUBICTO -> {
                    return true; // produces geometry
                }
            }

            it.next();
        }

        return false;
    }

    /**
     * Generates a hash code for a point, rounding off to account for precision errors
     *
     * @param point a point to generate the hash code for
     * @return a hash code
     */
    public static int hashCode(Point2D point) {
        double value = point.getY();
        double value1 = point.getX();
        return Long.hashCode(Math.round(value1 / PathUtils.EPS)) + Long.hashCode(Math.round(value / PathUtils.EPS));
    }

    /**
     * Generates a hash code for a point, rounding off points to account for precision errors
     *
     * @param path the path to generate the hash code for
     * @return a hash code
     */
    public static int hashCode(Path2D path) {
        PathIterator it = path.getPathIterator(null);
        double[] coords = new double[6];

        int hash = 1;

        while (!it.isDone()) {
            int segmentType = it.currentSegment(coords);
            hash = 31 * hash + segmentType;

            // Each segment type uses a fixed number of coordinates
            int numberOfCoords = getNumberOfCoordsInSegmentType(segmentType);
            for (int i = 0; i < numberOfCoords; i++) {
                long bits = Long.hashCode(Math.round(coords[i] / PathUtils.EPS));
                hash = 31 * hash + Long.hashCode(bits);
            }

            it.next();
        }

        return hash;
    }

    /**
     * Returns the number of coords for a segment type
     *
     * @param segmentType the segment type to get coords for.
     * @return number of coors
     */
    public static int getNumberOfCoordsInSegmentType(int segmentType) {
        return switch (segmentType) {
            case PathIterator.SEG_MOVETO,
                 PathIterator.SEG_LINETO -> 2;
            case PathIterator.SEG_QUADTO -> 4;
            case PathIterator.SEG_CUBICTO -> 6;
            case PathIterator.SEG_CLOSE -> 0;
            default -> throw new IllegalArgumentException("Unknown segment type");
        };
    }

    public static long quantize(double v) {
        return Math.round(v / PathUtils.EPS);
    }

    public static boolean isClosed(Shape shape) {
        PathIterator it = shape.getPathIterator(null);
        double[] c = new double[6];

        Point2D start = null;
        Point2D last = null;

        while (!it.isDone()) {
            int type = it.currentSegment(c);
            if (type == PathIterator.SEG_CLOSE) {
                return true;
            }

            switch (type) {
                case PathIterator.SEG_MOVETO -> {
                    start = new Point2D.Double(c[0], c[1]);
                    last = start;
                }
                case PathIterator.SEG_LINETO -> last = new Point2D.Double(c[0], c[1]);
                case PathIterator.SEG_QUADTO -> last = new Point2D.Double(c[2], c[3]);
                case PathIterator.SEG_CUBICTO -> last = new Point2D.Double(c[4], c[5]);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }

            it.next();
        }

        // Implicit closure check
        return start != null && last != null && isEqual(start, last, EPS);
    }
}
