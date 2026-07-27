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
package com.willwinder.ugs.designer.io.gcode.path;

import com.willwinder.universalgcodesender.model.PartialPosition;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces runs of straight lines with circular arcs where the lines are within a given tolerance
 * of a circle.
 * <p>
 * Tool paths are generated from flattened geometry, so curves in the source design have already
 * been reduced to short line segments by the time they reach this point. Recovering arcs from those
 * lines produces smaller gcode and lets the controller move along the curve instead of through a
 * series of corners.
 * <p>
 * The tolerance is the largest distance a fitted arc is allowed to deviate from the lines it
 * replaces. It adds to whatever error the geometry stages introduced when flattening, so the
 * flattening precision should be considerably finer than this tolerance.
 */
public class ArcFitter {

    /**
     * Any three points describe a circle exactly, so more than three are needed before a fit says
     * anything about the points actually following a curve.
     */
    private static final int MINIMUM_ARC_POINTS = 5;

    private static final double FULL_CIRCLE = 2 * Math.PI;

    /**
     * How many points that do not fit may be looked past while growing an arc, before the arc is
     * considered finished.
     */
    private static final int MAXIMUM_POINTS_LOOKED_PAST = 4;

    /**
     * How small the fitted determinant may become, relative to its own scale, before the points are
     * treated as describing a line rather than a circle.
     */
    private static final double COLLINEAR_EPSILON = 1e-12;

    /**
     * Arcs are kept to half a circle at most so that their two ends stay far apart.
     * <p>
     * A controller works out how far an arc travels from the angle between its start and its end,
     * and reads an arc ending where it started as a full circle. Coordinates are rounded when
     * written, so the ends of a nearly complete circle can round onto each other and turn the arc
     * into a full circle, or just off each other and turn a full circle into almost no movement at
     * all. Splitting well before that point keeps every arc unambiguous.
     */
    private static final double MAXIMUM_SWEEP = Math.PI;

    /**
     * Slack for accumulated rounding when adding up the steps of an arc, so that an arc landing
     * exactly on the maximum sweep is not rejected for overshooting it.
     */
    private static final double SWEEP_EPSILON = 1e-9;

    private final double tolerance;
    private final double chordBulgeAllowance;

    /**
     * @param tolerance         the largest distance a fitted arc may deviate from the points it
     *                          replaces
     * @param flatnessPrecision the error the given points already carry from having been flattened
     *                          from the original shape
     */
    public ArcFitter(double tolerance, double flatnessPrecision) {
        if (tolerance <= 0) {
            throw new IllegalArgumentException("The arc fitting tolerance must be larger than zero");
        }
        if (flatnessPrecision <= 0) {
            throw new IllegalArgumentException("The flatness precision must be larger than zero");
        }

        this.tolerance = tolerance;
        this.chordBulgeAllowance = Math.max(tolerance, flatnessPrecision);
    }

    /**
     * Converts a run of positions into the segments needed to cut it, using arcs where possible.
     * The first position is treated as the already reached start of the run, so the returned
     * segments describe the movement from the first to the last position.
     *
     * @param points   the positions to move through, in order
     * @param feedRate the feed rate to cut with
     * @return the segments describing the run, never containing the first position on its own
     */
    public List<Segment> fit(List<PartialPosition> points, Integer feedRate) {
        if (!isFittable(points)) {
            return toLines(points, feedRate);
        }

        List<Segment> segments = new ArrayList<>();
        int startIndex = 0;
        while (startIndex < points.size() - 1) {
            Arc arc = findLongestArc(points, startIndex);
            if (arc == null) {
                segments.add(toLine(points.get(startIndex + 1), feedRate));
                startIndex++;
            } else {
                segments.add(Segment.arc(arc.type(), points.get(arc.endIndex()), arc.center(), feedRate));
                startIndex = arc.endIndex();
            }
        }

        return segments;
    }

    private boolean isFittable(List<PartialPosition> points) {
        if (points.size() < MINIMUM_ARC_POINTS) {
            return false;
        }

        PartialPosition first = points.get(0);
        return points.stream().allMatch(p -> p.hasX() && p.hasY() && hasSameDepth(p, first));
    }

    /**
     * Arcs are cut in the XY plane, so a run that changes depth would need a helical arc which is
     * left to be cut as lines.
     */
    private boolean hasSameDepth(PartialPosition point, PartialPosition reference) {
        if (point.hasZ() != reference.hasZ()) {
            return false;
        }

        return !point.hasZ() || Math.abs(point.getZ() - reference.getZ()) < tolerance;
    }

    /**
     * Grows the arc one point at a time for as long as it keeps fitting, so that each arc covers as
     * much of the run as it can.
     * <p>
     * Each added point shifts the fitted circle slightly, which can push a borderline point just
     * outside the tolerance even though a longer run of points fits comfortably. A few points are
     * therefore looked past before giving up. This can not bridge a real corner, since every point
     * in the run still has to sit on the arc that gets returned.
     */
    private Arc findLongestArc(List<PartialPosition> points, int startIndex) {
        Arc longestArc = null;
        int pointsSinceLastFit = 0;
        for (int endIndex = startIndex + MINIMUM_ARC_POINTS - 1; endIndex < points.size(); endIndex++) {
            Arc arc = fitArc(points, startIndex, endIndex);
            if (arc == null) {
                pointsSinceLastFit++;
                if (pointsSinceLastFit > MAXIMUM_POINTS_LOOKED_PAST) {
                    break;
                }
            } else {
                pointsSinceLastFit = 0;
                longestArc = arc;
            }
        }

        return longestArc;
    }

    private Arc fitArc(List<PartialPosition> points, int startIndex, int endIndex) {
        if (isWithinToleranceOfStraightLine(points, startIndex, endIndex)) {
            return null;
        }

        Point2D center = fitCenter(points, startIndex, endIndex);
        if (center == null) {
            return null;
        }

        center = balanceCenterBetweenEnds(center, points.get(startIndex), points.get(endIndex));
        double radius = distanceTo(center, points.get(startIndex));
        if (!isOnCircle(points, startIndex, endIndex, center, radius)) {
            return null;
        }
        if (!coversChordsWithinTolerance(points, startIndex, endIndex, radius)) {
            return null;
        }

        Integer direction = findSweepDirection(points, startIndex, endIndex, center);
        if (direction == null) {
            return null;
        }

        return new Arc(direction > 0 ? SegmentType.CCWARC : SegmentType.CWARC, center, endIndex);
    }

    /**
     * A straight line is both shorter to write and more accurate than an arc with an enormous
     * radius, so nearly collinear points are left alone.
     */
    private boolean isWithinToleranceOfStraightLine(List<PartialPosition> points, int startIndex, int endIndex) {
        PartialPosition start = points.get(startIndex);
        PartialPosition end = points.get(endIndex);
        for (int i = startIndex + 1; i < endIndex; i++) {
            PartialPosition point = points.get(i);
            double distance = Line2D.ptSegDist(start.getX(), start.getY(), end.getX(), end.getY(), point.getX(), point.getY());
            if (distance > tolerance) {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds the center of the circle that best fits all of the points using a least squares fit.
     * Fitting all the points rather than picking three of them averages out the error left behind
     * when the original curve was flattened, and stays well conditioned when the run closes back on
     * itself and its first and last point coincide.
     * <p>
     * The points are centered on their centroid before fitting, which keeps the sums well scaled
     * for geometry that sits far from the origin.
     *
     * @return the center of the fitted circle, or null when the points describe no circle
     */
    private Point2D fitCenter(List<PartialPosition> points, int startIndex, int endIndex) {
        int count = (endIndex - startIndex) + 1;
        double centroidX = 0;
        double centroidY = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            centroidX += points.get(i).getX();
            centroidY += points.get(i).getY();
        }
        centroidX /= count;
        centroidY /= count;

        double sumUu = 0;
        double sumUv = 0;
        double sumVv = 0;
        double sumUuu = 0;
        double sumVvv = 0;
        double sumUvv = 0;
        double sumVuu = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            double u = points.get(i).getX() - centroidX;
            double v = points.get(i).getY() - centroidY;
            sumUu += u * u;
            sumUv += u * v;
            sumVv += v * v;
            sumUuu += u * u * u;
            sumVvv += v * v * v;
            sumUvv += u * v * v;
            sumVuu += v * u * u;
        }

        double determinant = (sumUu * sumVv) - (sumUv * sumUv);
        if (determinant <= COLLINEAR_EPSILON * sumUu * sumVv) {
            return null;
        }

        double halfSumU = (sumUuu + sumUvv) / 2;
        double halfSumV = (sumVvv + sumVuu) / 2;
        double centerU = ((halfSumU * sumVv) - (halfSumV * sumUv)) / determinant;
        double centerV = ((halfSumV * sumUu) - (halfSumU * sumUv)) / determinant;

        return new Point2D.Double(centroidX + centerU, centroidY + centerV);
    }

    /**
     * Moves the center onto the perpendicular bisector of the line between the first and last point,
     * placing both ends of the arc on exactly the same radius.
     * <p>
     * Controllers derive the radius from the start point and the center they are given, and reject
     * the move when the end point does not land on that same radius. Balancing the center here keeps
     * the arc acceptable without having to discard arcs whose ends happen to sit a fraction apart.
     */
    private Point2D balanceCenterBetweenEnds(Point2D center, PartialPosition start, PartialPosition end) {
        double chordX = end.getX() - start.getX();
        double chordY = end.getY() - start.getY();
        double chordLength = Math.hypot(chordX, chordY);
        if (chordLength == 0) {
            return center;
        }

        double midX = (start.getX() + end.getX()) / 2;
        double midY = (start.getY() + end.getY()) / 2;
        double normalX = -chordY / chordLength;
        double normalY = chordX / chordLength;
        double distanceAlongNormal = ((center.getX() - midX) * normalX) + ((center.getY() - midY) * normalY);

        return new Point2D.Double(midX + (distanceAlongNormal * normalX), midY + (distanceAlongNormal * normalY));
    }

    /**
     * The radius is measured from the start of the arc rather than averaged over the points, because
     * that is the radius the controller derives from the center it is given and therefore the radius
     * it actually cuts. Averaging would report a closer fit than the machine will follow.
     */
    private boolean isOnCircle(List<PartialPosition> points, int startIndex, int endIndex, Point2D center, double radius) {
        for (int i = startIndex; i <= endIndex; i++) {
            if (Math.abs(distanceTo(center, points.get(i)) - radius) > tolerance) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks how far the arc strays from the lines it replaces between the points, which the points
     * alone can not tell us.
     * <p>
     * Straight edges often arrive as nothing but their two end points, and any large enough circle
     * passes through a pair of points. Without this check a long straight edge would be replaced by
     * a wide arc bowing away from it, cutting into material that should have been left alone.
     * <p>
     * The bulge is allowed to reach the flatness precision rather than only the fitting tolerance.
     * A curve was flattened into points spaced closely enough to stay within that precision, so a
     * bulge of that size means the points are consistent with having come from a curve of this
     * radius. A bulge far beyond it means the points were spaced out because the shape was straight.
     */
    private boolean coversChordsWithinTolerance(List<PartialPosition> points, int startIndex, int endIndex, double radius) {
        for (int i = startIndex; i < endIndex; i++) {
            double halfChord = distanceBetween(points.get(i), points.get(i + 1)) / 2;
            if (halfChord >= radius) {
                return false;
            }

            double bulge = radius - Math.sqrt((radius * radius) - (halfChord * halfChord));
            if (bulge > chordBulgeAllowance) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that the points progress around the center in one direction without completing more
     * than a full circle, which the radius alone can not tell us. Without this a path that doubles
     * back along the same circle would be mistaken for an arc.
     *
     * @return 1 when the points sweep counter clockwise, -1 when clockwise, null when they do not
     * describe a single sweep
     */
    private Integer findSweepDirection(List<PartialPosition> points, int startIndex, int endIndex, Point2D center) {
        Integer direction = null;
        double totalSweep = 0;
        double previousAngle = angleTo(center, points.get(startIndex));

        for (int i = startIndex + 1; i <= endIndex; i++) {
            double angle = angleTo(center, points.get(i));
            double step = normalizeAngle(angle - previousAngle);
            previousAngle = angle;

            if (step == 0) {
                continue;
            }

            int stepDirection = step > 0 ? 1 : -1;
            if (direction == null) {
                direction = stepDirection;
            } else if (direction != stepDirection) {
                return null;
            }

            totalSweep += Math.abs(step);
            if (totalSweep > MAXIMUM_SWEEP + SWEEP_EPSILON) {
                return null;
            }
        }

        return direction;
    }

    private static double normalizeAngle(double angle) {
        double normalized = angle % FULL_CIRCLE;
        if (normalized > Math.PI) {
            normalized -= FULL_CIRCLE;
        } else if (normalized <= -Math.PI) {
            normalized += FULL_CIRCLE;
        }

        return normalized;
    }

    private static double distanceTo(Point2D center, PartialPosition point) {
        return Math.hypot(point.getX() - center.getX(), point.getY() - center.getY());
    }

    private static double distanceBetween(PartialPosition from, PartialPosition to) {
        return Math.hypot(to.getX() - from.getX(), to.getY() - from.getY());
    }

    private static double angleTo(Point2D center, PartialPosition point) {
        return Math.atan2(point.getY() - center.getY(), point.getX() - center.getX());
    }

    private static List<Segment> toLines(List<PartialPosition> points, Integer feedRate) {
        return points.stream()
                .skip(1)
                .map(point -> toLine(point, feedRate))
                .toList();
    }

    private static Segment toLine(PartialPosition point, Integer feedRate) {
        return new Segment(SegmentType.LINE, point, null, null, feedRate);
    }

    private record Arc(SegmentType type, Point2D center, int endIndex) {
    }
}
