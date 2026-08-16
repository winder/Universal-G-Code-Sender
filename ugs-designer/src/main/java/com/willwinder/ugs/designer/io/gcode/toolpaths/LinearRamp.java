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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Generates the tool path engaging the material by gradually descending along the beginning of a tool
 * path instead of moving straight down into it.
 * <p>
 * The descent always follows the direction the tool path is cut in.
 * <p>
 * A closed tool path is descended along from where it starts, and is then cut from where the ramp
 * ended, all the way around and back over the ramp again to clear out the material that the ramp left
 * behind. Each pass therefore ends where the next one can begin descending, letting the passes follow
 * each other without lifting the tool clear of the material in between.
 * <p>
 * A tool path that closes a loop and then carries on, the way a pocket moves on to the next ring to
 * clear, is instead descended along the stretch leading up to where the loop starts. The loop and
 * everything after it is then cut exactly as laid out, keeping the moves carrying the tool onwards.
 * <p>
 * An open tool path can not be extended to clear the ramp out, so it is descended towards its
 * beginning: the tool starts a bit into the path and ramps back to where it begins, from where the
 * path is then cut the way it normally would be, clearing out the ramp as it goes.
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
     * <p>
     * A closed tool path that the tool is already standing on is turned to begin where the tool is,
     * letting the descent start right away without lifting the tool clear of the material first.
     *
     * @param coordinates  the coordinates of the tool path to descend along, at the depth of the cut
     * @param toolPosition where the tool was left by the previous pass, or null if it is somewhere else
     * @param fromDepth    the positive depth in millimeters that the tool starts descending from when
     *                     it has to be moved to the tool path first
     * @param toDepth      the positive depth in millimeters that the tool should end up at
     * @param feedRate     the feed rate in mm/min to descend with
     * @return the ramped tool path, or empty if the tool path can not be ramped into and needs to be
     * plunged straight down into instead
     */
    public static Optional<RampedPath> create(List<PartialPosition> coordinates, PartialPosition toolPosition, double fromDepth, double toDepth, int feedRate) {
        if (coordinates.size() < 2) {
            return Optional.empty();
        }

        int loopEnd = indexOfLoopEnd(coordinates);
        if (loopEnd < 0) {
            return rampTowardsPath(coordinates, fromDepth, toDepth, feedRate);
        }

        List<PartialPosition> loop = coordinates.subList(0, loopEnd + 1);
        List<PartialPosition> rest = coordinates.subList(loopEnd + 1, coordinates.size());
        if (!rest.isEmpty()) {
            return rampUpToLoop(loop, rest, fromDepth, toDepth, feedRate);
        }

        // Continuing from where the tool is means the depth it is standing at is known exactly, rather
        // than being the depth the previous pass was expected to have reached
        Optional<List<PartialPosition>> continued = turnedTowardsTool(loop, toolPosition, toDepth);
        if (continued.isPresent()) {
            return rampAlongPath(continued.get(), rest, null, -toolPosition.getZ(), toDepth, feedRate);
        }

        return rampAlongPath(loop, rest, loop.get(0), fromDepth, toDepth, feedRate);
    }

    /**
     * Descends along the stretch of the loop leading up to where it starts, in the direction the loop
     * is cut. The loop and everything following it is then left to be cut exactly as it was laid out,
     * which keeps the moves carrying the tool on to the rest of the path the ones it was laid out with.
     */
    private static Optional<RampedPath> rampUpToLoop(List<PartialPosition> loop, List<PartialPosition> rest, double fromDepth, double toDepth, int feedRate) {
        double rampLength = rampLength(toDepth - fromDepth);
        if (rampLength <= 0) {
            return Optional.empty();
        }

        List<PartialPosition> turned = loop;
        double loopLength = length(loop);
        if (loopLength > rampLength) {
            Span upToLoop = span(loop, loopLength - rampLength);
            turned = turnedTo(loop, rampEnd(upToLoop), upToLoop.nextIndex(), toDepth);
        }

        return rampAlongPath(turned, rest, turned.get(0), fromDepth, toDepth, feedRate);
    }

    /**
     * Descends along the beginning of a closed loop in the direction that it is cut, continuing the cut
     * from where the ramp ended and around until the ramp has been cleared out.
     *
     * @param entry where the tool needs to be moved before descending, or null if it is already there
     */
    private static Optional<RampedPath> rampAlongPath(List<PartialPosition> loop, List<PartialPosition> rest, PartialPosition entry, double fromDepth, double toDepth, int feedRate) {
        Optional<Span> span = span(loop, fromDepth, toDepth);
        if (span.isEmpty()) {
            return Optional.empty();
        }

        List<Segment> segments = descend(span.get().points(), span.get().length(), entry, fromDepth, toDepth, feedRate);
        List<PartialPosition> cutPath = new ArrayList<>(turnedTo(loop, rampEnd(span.get()), span.get().nextIndex(), toDepth));
        cutPath.addAll(rest);
        return Optional.of(new RampedPath(Optional.ofNullable(entry), segments, cutPath));
    }

    /**
     * Descends towards the beginning of an open tool path, leaving the path itself to be cut in its
     * entirety afterwards.
     */
    private static Optional<RampedPath> rampTowardsPath(List<PartialPosition> coordinates, double fromDepth, double toDepth, int feedRate) {
        Optional<Span> span = span(coordinates, fromDepth, toDepth);
        if (span.isEmpty()) {
            return Optional.empty();
        }

        List<Point2D> descent = new ArrayList<>(span.get().points());
        Collections.reverse(descent);

        PartialPosition entry = position(descent.get(0), toDepth);
        List<Segment> segments = descend(descent, span.get().length(), entry, fromDepth, toDepth, feedRate);
        return Optional.of(new RampedPath(Optional.of(entry), segments, coordinates));
    }

    /**
     * The moves descending along the given points, reaching the target depth as the last of them is
     * reached, preceded by a rapid down to where the previous pass left off when the tool had to be
     * moved to the tool path first.
     */
    private static List<Segment> descend(List<Point2D> points, double length, PartialPosition entry, double fromDepth, double toDepth, int feedRate) {
        List<Segment> segments = new ArrayList<>();
        if (entry != null) {
            segments.add(new Segment(SegmentType.MOVE, PartialPosition.from(Axis.Z, -fromDepth, UnitUtils.Units.MM)));
        }

        double traveled = 0;
        for (int i = 1; i < points.size(); i++) {
            traveled += points.get(i - 1).distance(points.get(i));
            double depth = Math.min(toDepth, fromDepth + (toDepth - fromDepth) * (traveled / length));
            segments.add(new Segment(SegmentType.LINE, position(points.get(i), depth), null, null, feedRate));
        }

        return segments;
    }

    /**
     * The closed tool path turned to begin where the tool is standing, or empty when the tool is
     * somewhere else and has to be moved to the tool path first.
     */
    private static Optional<List<PartialPosition>> turnedTowardsTool(List<PartialPosition> coordinates, PartialPosition toolPosition, double depth) {
        if (toolPosition == null || !toolPosition.hasX() || !toolPosition.hasY() || !toolPosition.hasZ()) {
            return Optional.empty();
        }

        Point2D tool = toPoint(toolPosition);
        if (isSamePoint(tool, toPoint(coordinates.get(0)))) {
            return Optional.of(coordinates);
        }

        for (int i = 1; i < coordinates.size(); i++) {
            Point2D from = toPoint(coordinates.get(i - 1));
            Point2D to = toPoint(coordinates.get(i));
            if (Line2D.ptSegDist(from.getX(), from.getY(), to.getX(), to.getY(), tool.getX(), tool.getY()) > TOLERANCE) {
                continue;
            }

            int next = isSamePoint(tool, to) ? i + 1 : i;
            return Optional.of(turnedTo(coordinates, tool, next, depth));
        }

        return Optional.empty();
    }

    /**
     * The closed tool path turned to start and end at the given point, so that cutting it also clears
     * out the material left behind between the start of the path and that point.
     *
     * @param nextIndex the index of the coordinate that follows the point
     */
    private static List<PartialPosition> turnedTo(List<PartialPosition> coordinates, Point2D point, int nextIndex, double depth) {
        PartialPosition start = position(point, depth);
        int next = Math.min(nextIndex, coordinates.size());

        List<PartialPosition> turned = new ArrayList<>();
        turned.add(start);
        turned.addAll(coordinates.subList(next, coordinates.size()));
        turned.addAll(coordinates.subList(1, next));

        // A path that is already turned to end at the point does not need it added again
        if (!isSamePoint(turned.get(turned.size() - 1), start)) {
            turned.add(start);
        }
        return turned;
    }

    private static Point2D rampEnd(Span span) {
        return span.points().get(span.points().size() - 1);
    }

    /**
     * Follows the tool path from its beginning until the tool has traveled far enough to descend from
     * one depth to the other without exceeding the ramp angle, ending with a point interpolated to be
     * at exactly that distance. Paths that are too short to descend along are used in their entirety,
     * making the ramp steeper than the ramp angle.
     *
     * @return the stretch to descend along, or empty when there is nothing to descend
     */
    private static Optional<Span> span(List<PartialPosition> coordinates, double fromDepth, double toDepth) {
        double rampLength = rampLength(toDepth - fromDepth);
        if (rampLength <= 0) {
            return Optional.empty();
        }

        Span span = span(coordinates, rampLength);
        return span.length() > 0 ? Optional.of(span) : Optional.empty();
    }

    /**
     * The distance the tool needs to travel to descend the given depth without exceeding the ramp angle.
     */
    private static double rampLength(double depthIncrement) {
        return depthIncrement / Math.tan(Math.toRadians(RAMP_ANGLE));
    }

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

    /**
     * The index where the tool path first returns to where it started, closing a loop, or -1 when it
     * does not begin with a closed loop. A pocket carries on with the next ring to clear once a loop
     * has been closed, which makes the tool path as a whole look like an open one.
     */
    private static int indexOfLoopEnd(List<PartialPosition> coordinates) {
        for (int i = 1; i < coordinates.size(); i++) {
            if (isSamePoint(coordinates.get(0), coordinates.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static double length(List<PartialPosition> coordinates) {
        double length = 0;
        for (int i = 1; i < coordinates.size(); i++) {
            length += toPoint(coordinates.get(i - 1)).distance(toPoint(coordinates.get(i)));
        }
        return length;
    }

    private static boolean isSamePoint(PartialPosition first, PartialPosition second) {
        return isSamePoint(toPoint(first), toPoint(second));
    }

    private static boolean isSamePoint(Point2D first, Point2D second) {
        return first.distance(second) < TOLERANCE;
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
     * @param entry    the position to move to before descending, or empty when the tool is standing
     *                 where the descent begins and can start it right away
     * @param segments the segments descending to the depth of cut
     * @param cutPath  the coordinates to cut once the ramp is done, starting where the ramp ended
     */
    public record RampedPath(Optional<PartialPosition> entry, List<Segment> segments, List<PartialPosition> cutPath) {
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
