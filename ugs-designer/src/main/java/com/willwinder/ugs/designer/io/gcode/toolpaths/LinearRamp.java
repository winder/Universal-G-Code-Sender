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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Generates the tool path engaging the material by gradually descending along the beginning of a tool
 * path instead of moving straight down into it.
 * <p>
 * A closed tool path is descended along in the direction that it is cut, and is then cut from where
 * the ramp ended, all the way around and back over the ramp again to clear out the material that the
 * ramp left behind.
 * <p>
 * An open tool path can not be extended that way, so it is descended along in the opposite direction:
 * the tool starts a bit into the path and ramps back towards its beginning, from where the path is
 * then cut the way it normally would be, clearing out the ramp as it goes.
 *
 * @author Joacim Breiler
 */
public class LinearRamp {
    /**
     * The angle in degrees that the tool is allowed to descend with. A shallower angle is gentler on
     * the tool but needs a longer stretch of the tool path to descend along.
     */
    private static final double RAMP_ANGLE = 15;

    /**
     * Distances in millimeters shorter than this are treated as the tool already being there
     */
    private static final double TOLERANCE = 0.0001;

    private LinearRamp() {
    }

    /**
     * Creates the tool path descending from one depth of cut to the next along the beginning of the
     * given tool path.
     *
     * @param coordinates the coordinates of the tool path to descend along, at the depth of the cut
     * @param fromDepth   the positive depth in millimeters that the tool starts descending from
     * @param toDepth     the positive depth in millimeters that the tool should end up at
     * @param feedRate    the feed rate in mm/min to descend with
     * @return the ramped tool path, or empty if the tool path can not be ramped into and needs to be
     * plunged straight down into instead
     */
    public static Optional<RampedPath> create(List<PartialPosition> coordinates, double fromDepth, double toDepth, int feedRate) {
        double depthIncrement = toDepth - fromDepth;
        if (coordinates.size() < 2 || depthIncrement <= 0) {
            return Optional.empty();
        }

        Span span = span(coordinates, rampLength(depthIncrement));
        if (span.length() <= 0) {
            return Optional.empty();
        }

        if (isClosed(coordinates)) {
            return Optional.of(rampAlongPath(coordinates, span, fromDepth, toDepth, feedRate));
        }

        return Optional.of(rampTowardsPath(coordinates, span, fromDepth, toDepth, feedRate));
    }

    /**
     * Descends along the beginning of a closed tool path in the direction that it is cut, continuing
     * the cut from where the ramp ended and around until the ramp has been cleared out.
     */
    private static RampedPath rampAlongPath(List<PartialPosition> coordinates, Span span, double fromDepth, double toDepth, int feedRate) {
        List<Segment> segments = descend(span.points(), span.length(), fromDepth, toDepth, feedRate);
        return new RampedPath(coordinates.get(0), segments, cutPathFromRampEnd(coordinates, span, toDepth));
    }

    /**
     * Descends towards the beginning of an open tool path, leaving the path itself to be cut in its
     * entirety afterwards.
     */
    private static RampedPath rampTowardsPath(List<PartialPosition> coordinates, Span span, double fromDepth, double toDepth, int feedRate) {
        List<Point2D> descent = new ArrayList<>(span.points());
        Collections.reverse(descent);

        List<Segment> segments = descend(descent, span.length(), fromDepth, toDepth, feedRate);
        return new RampedPath(position(descent.get(0), toDepth), segments, coordinates);
    }

    /**
     * The rapid down to where the previous pass left off followed by the moves descending along the
     * given points, reaching the target depth as the last of them is reached.
     */
    private static List<Segment> descend(List<Point2D> points, double length, double fromDepth, double toDepth, int feedRate) {
        List<Segment> segments = new ArrayList<>();
        segments.add(new Segment(SegmentType.MOVE, PartialPosition.from(Axis.Z, -fromDepth, UnitUtils.Units.MM)));

        double traveled = 0;
        for (int i = 1; i < points.size(); i++) {
            traveled += points.get(i - 1).distance(points.get(i));
            double depth = Math.min(toDepth, fromDepth + (toDepth - fromDepth) * (traveled / length));
            segments.add(new Segment(SegmentType.LINE, position(points.get(i), depth), null, null, feedRate));
        }

        return segments;
    }

    /**
     * The closed tool path turned to start and end where the ramp ended, so that cutting it clears out
     * the material that the ramp left behind.
     */
    private static List<PartialPosition> cutPathFromRampEnd(List<PartialPosition> coordinates, Span span, double depth) {
        PartialPosition rampEnd = position(span.points().get(span.points().size() - 1), depth);
        int next = Math.min(span.nextIndex(), coordinates.size());

        List<PartialPosition> cutPath = new ArrayList<>();
        cutPath.add(rampEnd);
        cutPath.addAll(coordinates.subList(next, coordinates.size()));
        cutPath.addAll(coordinates.subList(1, next));

        // Descending along the whole path already ends it where the ramp ended
        if (!isSamePoint(cutPath.get(cutPath.size() - 1), rampEnd)) {
            cutPath.add(rampEnd);
        }
        return cutPath;
    }

    /**
     * The distance the tool needs to travel to descend the given depth without exceeding the ramp
     * angle.
     */
    private static double rampLength(double depthIncrement) {
        return depthIncrement / Math.tan(Math.toRadians(RAMP_ANGLE));
    }

    /**
     * Follows the tool path from its beginning until the given length has been covered, ending with a
     * point interpolated to be at exactly that length. Paths that are too short to descend along are
     * used in their entirety, making the ramp steeper than the ramp angle.
     */
    private static Span span(List<PartialPosition> coordinates, double length) {
        List<Point2D> points = new ArrayList<>();
        points.add(toPoint(coordinates.get(0)));

        double covered = 0;
        for (int i = 1; i < coordinates.size(); i++) {
            Point2D from = points.get(points.size() - 1);
            Point2D to = toPoint(coordinates.get(i));
            double distance = from.distance(to);
            if (distance < TOLERANCE) {
                continue;
            }

            if (covered + distance > length + TOLERANCE) {
                points.add(interpolate(from, to, (length - covered) / distance));
                return new Span(points, i, length);
            }

            points.add(to);
            covered += distance;
            if (covered > length - TOLERANCE) {
                return new Span(points, i + 1, covered);
            }
        }

        return new Span(points, coordinates.size(), covered);
    }

    private static boolean isClosed(List<PartialPosition> coordinates) {
        return isSamePoint(coordinates.get(0), coordinates.get(coordinates.size() - 1));
    }

    private static boolean isSamePoint(PartialPosition first, PartialPosition second) {
        return toPoint(first).distance(toPoint(second)) < TOLERANCE;
    }

    private static PartialPosition position(Point2D point, double depth) {
        return new PartialPosition(point.getX(), point.getY(), -depth, UnitUtils.Units.MM);
    }

    private static Point2D interpolate(Point2D from, Point2D to, double ratio) {
        return new Point2D.Double(from.getX() + (to.getX() - from.getX()) * ratio,
                from.getY() + (to.getY() - from.getY()) * ratio);
    }

    private static Point2D toPoint(PartialPosition position) {
        return new Point2D.Double(position.getX(), position.getY());
    }

    /**
     * The tool path engaging the material with a ramp.
     *
     * @param entry    the position to move to before descending
     * @param segments the segments descending to the depth of cut
     * @param cutPath  the coordinates to cut once the ramp is done, starting where the ramp ended
     */
    public record RampedPath(PartialPosition entry, List<Segment> segments, List<PartialPosition> cutPath) {
    }

    /**
     * The stretch at the beginning of a tool path that the tool descends along.
     *
     * @param points    the points of the stretch, the last one being where the ramp ends
     * @param nextIndex the index in the tool path where the cut continues after the ramp
     * @param length    the length of the stretch in millimeters
     */
    private record Span(List<Point2D> points, int nextIndex, double length) {
    }
}
