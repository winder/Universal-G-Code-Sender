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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.model.path.Segment;
import com.willwinder.ugs.designer.model.path.SegmentType;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Adds, removes and converts the nodes of a path, and breaks and joins paths at nodes. The
 * subpath holding the node is rebuilt from its nodes and the curve handles on either side of
 * them, so the curve that replaces a removed node keeps the outer handles of the two curves it
 * joins. The start and end node of a closed subpath are one node. Other subpaths are kept
 * exactly as they were.
 */
final class PathNodes {
    private static final double SAME_POSITION = 1e-9;
    /** How many points along each curve are tried when looking for the nearest one. */
    private static final int SAMPLES = 32;
    private static final int REFINEMENTS = 20;

    /**
     * A node with the curve handles going into and out of it, null where the curve on that
     * side is a straight line.
     */
    private static final class Knot {
        private final Point2D point;
        private Point2D in;
        private Point2D out;

        private Knot(Point2D point, Point2D in) {
            this.point = point;
            this.in = in;
        }

        private Knot(Point2D point, Point2D in, Point2D out) {
            this(point, in);
            this.out = out;
        }
    }

    /**
     * A subpath: the segments it spans and its nodes.
     */
    private static final class Subpath {
        private final int firstSegment;
        private int lastSegment;
        private final List<Knot> knots = new ArrayList<>();
        private boolean closed;

        private Subpath(int firstSegment) {
            this.firstSegment = firstSegment;
            this.lastSegment = firstSegment;
        }
    }

    /**
     * A node of a path: the segment it ends, or the move starting its subpath, and its position.
     */
    record NodeAt(int segmentIndex, Point2D position) {
    }

    /**
     * A point on a curve of a path: the segment drawing the curve, how far along it the point
     * is, from 0 at its start to 1 at its end, and where it is.
     */
    record CurvePoint(int segmentIndex, double t, Point2D position) {
    }

    private PathNodes() {
    }

    /**
     * Removes the node that a segment ends, or the move starting its subpath. The node is found
     * by its segment, so of two nodes on the same spot the right one is removed.
     *
     * @param segments     the path's segments, in design coordinates
     * @param segmentIndex the segment the node ends, or the move starting its subpath
     * @param position     the node's position
     * @return the path without the node, or empty when nothing drawable would be left
     */
    static Optional<Path2D> deleteNode(List<Segment> segments, int segmentIndex, Point2D position) {
        return deleteNodes(segments, List.of(new NodeAt(segmentIndex, position)));
    }

    /**
     * Removes several nodes at once, joining the neighbours of each the same way as
     * {@link #deleteNode}. Subpaths left with fewer than two nodes are dropped.
     *
     * @param segments the path's segments, in design coordinates
     * @param nodes    the nodes to remove
     * @return the path without the nodes, or empty when nothing drawable would be left
     */
    static Optional<Path2D> deleteNodes(List<Segment> segments, List<NodeAt> nodes) {
        List<Subpath> subpaths = subpaths(segments);
        Path2D result = new Path2D.Double();
        boolean drawable = false;
        for (Subpath subpath : subpaths) {
            List<NodeAt> inSubpath = nodes.stream()
                    .filter(node -> node.segmentIndex() >= subpath.firstSegment && node.segmentIndex() <= subpath.lastSegment)
                    .toList();
            if (!inSubpath.isEmpty()) {
                removeKnots(subpath, knotIndexes(subpath, inSubpath));
                drawable |= append(result, subpath);
            } else {
                drawable |= subpath.knots.size() > 1;
                for (int i = subpath.firstSegment; i <= subpath.lastSegment; i++) {
                    appendSegment(result, segments.get(i));
                }
            }
        }
        return drawable ? Optional.of(result) : Optional.empty();
    }

    /**
     * Breaks the subpath holding a segment at the node at a position. An open subpath is split
     * in two that both end at the node, a closed one is opened so it starts and ends there.
     *
     * @param segments     the path's segments, in design coordinates
     * @param segmentIndex the segment the node ends, or the move starting its subpath
     * @param position     the node's position
     * @return the broken path, or empty when the node is an end of an open subpath and there is
     * nothing to break
     */
    static Optional<Path2D> breakAtNode(List<Segment> segments, int segmentIndex, Point2D position) {
        Path2D result = new Path2D.Double();
        for (Subpath subpath : subpaths(segments)) {
            if (segmentIndex < subpath.firstSegment || segmentIndex > subpath.lastSegment) {
                for (int i = subpath.firstSegment; i <= subpath.lastSegment; i++) {
                    appendSegment(result, segments.get(i));
                }
                continue;
            }
            List<Knot> knots = subpath.knots;
            Set<Integer> indexes = knotIndexes(subpath, List.of(new NodeAt(segmentIndex, position)));
            if (indexes.isEmpty()) {
                return Optional.empty();
            }
            int index = indexes.iterator().next();
            Knot node = knots.get(index);
            if (subpath.closed) {
                if (knots.size() < 2) {
                    return Optional.empty();
                }
                List<Knot> opened = new ArrayList<>();
                opened.add(new Knot(node.point, null, node.out));
                for (int i = 1; i < knots.size(); i++) {
                    opened.add(knots.get((index + i) % knots.size()));
                }
                opened.add(new Knot(node.point, node.in, null));
                append(result, opened, false);
            } else {
                if (index == 0 || index == knots.size() - 1) {
                    return Optional.empty();
                }
                List<Knot> first = new ArrayList<>(knots.subList(0, index));
                first.add(new Knot(node.point, node.in, null));
                List<Knot> second = new ArrayList<>();
                second.add(new Knot(node.point, null, node.out));
                second.addAll(knots.subList(index + 1, knots.size()));
                append(result, first, false);
                append(result, second, false);
            }
        }
        return Optional.of(result);
    }

    /**
     * Joins two loose ends, the first or last nodes of open subpaths, into one node halfway
     * between them. Ends of two subpaths make one subpath through the joined node, turning one
     * around when needed; the two ends of one subpath close it. Curve handles move along with
     * their node so the curves keep their shape.
     *
     * @param segments the path's segments, in design coordinates
     * @param a        one end
     * @param b        the other end
     * @return the joined path, or empty when the nodes are not two different loose ends or a
     * subpath of only two nodes would be closed onto itself
     */
    static Optional<Path2D> joinNodes(List<Segment> segments, NodeAt a, NodeAt b) {
        List<Subpath> subpaths = subpaths(segments);
        Subpath subpathA = subpathOf(subpaths, a.segmentIndex());
        Subpath subpathB = subpathOf(subpaths, b.segmentIndex());
        if (subpathA == null || subpathB == null || subpathA.closed || subpathB.closed) {
            return Optional.empty();
        }
        int indexA = endIndex(subpathA, a.segmentIndex());
        int indexB = endIndex(subpathB, b.segmentIndex());
        if (indexA < 0 || indexB < 0) {
            return Optional.empty();
        }

        List<Knot> joinedKnots;
        boolean closed;
        if (subpathA == subpathB) {
            List<Knot> knots = subpathA.knots;
            if (indexA == indexB || knots.size() < 3) {
                return Optional.empty();
            }
            Knot first = knots.getFirst();
            Knot last = knots.getLast();
            joinedKnots = new ArrayList<>(knots.subList(1, knots.size() - 1));
            joinedKnots.addFirst(merge(last, first));
            closed = true;
        } else {
            // A runs into the joined node and B runs on from it
            List<Knot> knotsA = indexA == 0 ? reversed(subpathA.knots) : subpathA.knots;
            List<Knot> knotsB = indexB == 0 ? subpathB.knots : reversed(subpathB.knots);
            joinedKnots = new ArrayList<>(knotsA.subList(0, knotsA.size() - 1));
            joinedKnots.add(merge(knotsA.getLast(), knotsB.getFirst()));
            joinedKnots.addAll(knotsB.subList(1, knotsB.size()));
            closed = false;
        }

        Path2D result = new Path2D.Double();
        for (Subpath subpath : subpaths) {
            if (subpath == subpathA) {
                append(result, joinedKnots, closed);
            } else if (subpath != subpathB) {
                for (int i = subpath.firstSegment; i <= subpath.lastSegment; i++) {
                    appendSegment(result, segments.get(i));
                }
            }
        }
        return Optional.of(result);
    }

    /**
     * The point on the path's curves nearest to a point.
     *
     * @param segments the path's segments, in design coordinates
     * @param point    the point to look from
     * @return the nearest point, or empty when the path has no curves
     */
    static Optional<CurvePoint> nearestCurvePoint(List<Segment> segments, Point2D point) {
        CurvePoint nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (Subpath subpath : subpaths(segments)) {
            for (int curve = 0; curve < curveCount(subpath); curve++) {
                Point2D[] p = controlPoints(subpath, curve);
                double bestT = 0;
                double best = Double.POSITIVE_INFINITY;
                for (int i = 0; i <= SAMPLES; i++) {
                    double t = i / (double) SAMPLES;
                    double distance = pointAt(p, t).distance(point);
                    if (distance < best) {
                        best = distance;
                        bestT = t;
                    }
                }
                // Narrow down around the best sample
                double step = 1.0 / SAMPLES;
                for (int i = 0; i < REFINEMENTS; i++) {
                    step /= 2;
                    for (double t : new double[]{bestT - step, bestT + step}) {
                        double clamped = Math.max(0, Math.min(1, t));
                        double distance = pointAt(p, clamped).distance(point);
                        if (distance < best) {
                            best = distance;
                            bestT = clamped;
                        }
                    }
                }
                if (best < nearestDistance) {
                    nearestDistance = best;
                    nearest = new CurvePoint(subpath.firstSegment + 1 + curve, bestT, pointAt(p, bestT));
                }
            }
        }
        return Optional.ofNullable(nearest);
    }

    /**
     * Adds a node on a curve, splitting it in two that together keep the curve's shape.
     *
     * @param segments     the path's segments, in design coordinates
     * @param segmentIndex the segment drawing the curve, the close segment for the line that
     *                     closes a subpath
     * @param t            where on the curve, from 0 at its start to 1 at its end
     * @return the path with the new node, or empty when the segment draws no curve
     */
    static Optional<Path2D> insertNode(List<Segment> segments, int segmentIndex, double t) {
        List<Subpath> subpaths = subpaths(segments);
        Subpath subpath = subpathOf(subpaths, segmentIndex);
        if (subpath == null) {
            return Optional.empty();
        }
        int curve = segmentIndex - subpath.firstSegment - 1;
        if (curve < 0 || curve >= curveCount(subpath)) {
            return Optional.empty();
        }
        split(subpath, curve, t);
        return Optional.of(rebuild(segments, subpaths, Set.of(subpath)));
    }

    /**
     * Adds a node halfway along every curve that runs between two of the given nodes.
     *
     * @param segments the path's segments, in design coordinates
     * @param nodes    the nodes
     * @return the path with the new nodes, or empty when no curve runs between two of the nodes
     */
    static Optional<Path2D> insertNodesBetween(List<Segment> segments, List<NodeAt> nodes) {
        List<Subpath> subpaths = subpaths(segments);
        Set<Subpath> changed = new HashSet<>();
        for (Subpath subpath : subpaths) {
            Set<Integer> selected = knotIndexes(subpath, nodes);
            int count = subpath.knots.size();
            // From the last curve backwards, so splitting one does not move the ones still to split
            for (int curve = curveCount(subpath) - 1; curve >= 0; curve--) {
                if (selected.contains(curve) && selected.contains((curve + 1) % count)) {
                    split(subpath, curve, 0.5);
                    changed.add(subpath);
                }
            }
        }
        return changed.isEmpty() ? Optional.empty() : Optional.of(rebuild(segments, subpaths, changed));
    }

    /**
     * Turns the given nodes into smooth curve nodes. A node with straight lines on both sides
     * gets handles along the line between its neighbours, a third of the way to each, so the
     * path curves smoothly through it. A node already curved on one side gets a handle on the
     * other side pointing straight away from the first. The neighbours get handles a third of
     * the way towards the node where their side of the curve was straight.
     *
     * @param segments the path's segments, in design coordinates
     * @param nodes    the nodes
     * @return the path with curve nodes, or empty when all of them already were
     */
    static Optional<Path2D> makeCurveNodes(List<Segment> segments, List<NodeAt> nodes) {
        List<Subpath> subpaths = subpaths(segments);
        Set<Subpath> changed = new HashSet<>();
        for (Subpath subpath : subpaths) {
            // The nodes' own handles first, so each is shaped by its neighbours as they were
            List<Integer> curved = knotIndexes(subpath, nodes).stream()
                    .filter(index -> makeCurveNode(subpath, index))
                    .toList();
            for (int index : curved) {
                Knot knot = subpath.knots.get(index);
                Knot previous = previous(subpath, index);
                Knot next = next(subpath, index);
                if (previous != null && previous.out == null) {
                    previous.out = towards(previous.point, knot.point, 1.0 / 3);
                }
                if (next != null && next.in == null) {
                    next.in = towards(next.point, knot.point, 1.0 / 3);
                }
            }
            if (!curved.isEmpty()) {
                changed.add(subpath);
            }
        }
        return changed.isEmpty() ? Optional.empty() : Optional.of(rebuild(segments, subpaths, changed));
    }

    /**
     * Turns the given nodes into simple corner nodes, making the curves on both sides of each
     * straight lines.
     *
     * @param segments the path's segments, in design coordinates
     * @param nodes    the nodes
     * @return the path with corner nodes, or empty when all of them already were
     */
    static Optional<Path2D> makeLineNodes(List<Segment> segments, List<NodeAt> nodes) {
        List<Subpath> subpaths = subpaths(segments);
        Set<Subpath> changed = new HashSet<>();
        for (Subpath subpath : subpaths) {
            List<Knot> knots = subpath.knots;
            for (int index : knotIndexes(subpath, nodes)) {
                Knot knot = knots.get(index);
                Knot previous = previous(subpath, index);
                Knot next = next(subpath, index);
                if (previous != null && (knot.in != null || previous.out != null)) {
                    knot.in = null;
                    previous.out = null;
                    changed.add(subpath);
                }
                if (next != null && (knot.out != null || next.in != null)) {
                    knot.out = null;
                    next.in = null;
                    changed.add(subpath);
                }
            }
        }
        return changed.isEmpty() ? Optional.empty() : Optional.of(rebuild(segments, subpaths, changed));
    }

    /**
     * Gives a node the handles it lacks on the sides it has curves on.
     *
     * @return whether the node got a handle
     */
    private static boolean makeCurveNode(Subpath subpath, int index) {
        Knot knot = subpath.knots.get(index);
        Knot previous = previous(subpath, index);
        Knot next = next(subpath, index);
        boolean needsIn = previous != null && knot.in == null;
        boolean needsOut = next != null && knot.out == null;
        if (!needsIn && !needsOut) {
            return false;
        }
        Point2D direction = null;
        if (!needsIn && previous != null) {
            direction = unit(knot.in, knot.point);
        } else if (!needsOut && next != null) {
            direction = unit(knot.point, knot.out);
        }
        if (direction == null) {
            Point2D from = previous != null ? previous.point : knot.point;
            Point2D to = next != null ? next.point : knot.point;
            direction = unit(from, to);
        }
        if (direction == null) {
            return false;
        }
        if (needsIn) {
            double length = knot.point.distance(previous.point) / 3;
            knot.in = new Point2D.Double(knot.point.getX() - direction.getX() * length,
                    knot.point.getY() - direction.getY() * length);
        }
        if (needsOut) {
            double length = knot.point.distance(next.point) / 3;
            knot.out = new Point2D.Double(knot.point.getX() + direction.getX() * length,
                    knot.point.getY() + direction.getY() * length);
        }
        return true;
    }

    /**
     * The unit vector from one point to another, or null when they are the same point.
     */
    private static Point2D unit(Point2D from, Point2D to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double length = Math.hypot(dx, dy);
        return length <= SAME_POSITION ? null : new Point2D.Double(dx / length, dy / length);
    }

    /**
     * The node a curve runs into a node from, or null at the start of an open subpath.
     */
    private static Knot previous(Subpath subpath, int index) {
        List<Knot> knots = subpath.knots;
        if (knots.size() < 2 || (index == 0 && !subpath.closed)) {
            return null;
        }
        return knots.get((index - 1 + knots.size()) % knots.size());
    }

    /**
     * The node a curve runs on to from a node, or null at the end of an open subpath.
     */
    private static Knot next(Subpath subpath, int index) {
        List<Knot> knots = subpath.knots;
        if (knots.size() < 2 || (index == knots.size() - 1 && !subpath.closed)) {
            return null;
        }
        return knots.get((index + 1) % knots.size());
    }

    /**
     * The places in a subpath of the given nodes. A node is found by its segment: the move
     * starting the subpath is the first node, and each segment after it ends the next one. The
     * segment returning to the start of a closed subpath ends the first node.
     */
    private static Set<Integer> knotIndexes(Subpath subpath, List<NodeAt> nodes) {
        Set<Integer> indexes = new TreeSet<>();
        int count = subpath.knots.size();
        for (NodeAt node : nodes) {
            if (node.segmentIndex() < subpath.firstSegment || node.segmentIndex() > subpath.lastSegment) {
                continue;
            }
            int index = node.segmentIndex() - subpath.firstSegment;
            if (index < count) {
                indexes.add(index);
            } else if (index == count && subpath.closed) {
                indexes.add(0);
            }
        }
        return indexes;
    }

    /**
     * How many curves a subpath draws: one between each two nodes, and one more back to the
     * start when it is closed.
     */
    private static int curveCount(Subpath subpath) {
        int count = subpath.knots.size();
        if (count < 2) {
            return 0;
        }
        return subpath.closed ? count : count - 1;
    }

    /**
     * The four control points of a curve of a subpath, a straight line having its handles on
     * its ends.
     */
    private static Point2D[] controlPoints(Subpath subpath, int curve) {
        List<Knot> knots = subpath.knots;
        Knot from = knots.get(curve);
        Knot to = knots.get((curve + 1) % knots.size());
        return new Point2D[]{from.point, from.out != null ? from.out : from.point,
                to.in != null ? to.in : to.point, to.point};
    }

    private static Point2D pointAt(Point2D[] p, double t) {
        double u = 1 - t;
        double a = u * u * u;
        double b = 3 * u * u * t;
        double c = 3 * u * t * t;
        double d = t * t * t;
        return new Point2D.Double(a * p[0].getX() + b * p[1].getX() + c * p[2].getX() + d * p[3].getX(),
                a * p[0].getY() + b * p[1].getY() + c * p[2].getY() + d * p[3].getY());
    }

    /**
     * Splits a curve of a subpath in two at a point along it, adding a node there. A straight
     * line is split into two straight lines, a curve into two curves that trace the same shape.
     */
    private static void split(Subpath subpath, int curve, double t) {
        List<Knot> knots = subpath.knots;
        Knot from = knots.get(curve);
        Knot to = knots.get((curve + 1) % knots.size());
        Knot node;
        if (from.out == null && to.in == null) {
            node = new Knot(towards(from.point, to.point, t), null, null);
        } else {
            Point2D[] p = controlPoints(subpath, curve);
            Point2D a = towards(p[0], p[1], t);
            Point2D b = towards(p[1], p[2], t);
            Point2D c = towards(p[2], p[3], t);
            Point2D ab = towards(a, b, t);
            Point2D bc = towards(b, c, t);
            node = new Knot(towards(ab, bc, t), ab, bc);
            from.out = a;
            to.in = c;
        }
        knots.add(curve + 1, node);
    }

    /**
     * The path with the changed subpaths rebuilt from their nodes and the others copied as
     * they were.
     */
    private static Path2D rebuild(List<Segment> segments, List<Subpath> subpaths, Set<Subpath> changed) {
        Path2D result = new Path2D.Double();
        for (Subpath subpath : subpaths) {
            if (changed.contains(subpath)) {
                append(result, subpath);
            } else {
                for (int i = subpath.firstSegment; i <= subpath.lastSegment; i++) {
                    appendSegment(result, segments.get(i));
                }
            }
        }
        return result;
    }

    private static Subpath subpathOf(List<Subpath> subpaths, int segmentIndex) {
        return subpaths.stream()
                .filter(subpath -> segmentIndex >= subpath.firstSegment && segmentIndex <= subpath.lastSegment)
                .findFirst()
                .orElse(null);
    }

    /**
     * The place of the loose end that a segment ends: the first or the last node of the open
     * subpath, or -1 when it ends a node in between. Found by segment rather than position, as
     * the two ends of a broken closed shape share their position.
     */
    private static int endIndex(Subpath subpath, int segmentIndex) {
        List<Knot> knots = subpath.knots;
        if (knots.size() < 2) {
            return -1;
        }
        if (segmentIndex == subpath.firstSegment) {
            return 0;
        }
        if (segmentIndex == subpath.lastSegment) {
            return knots.size() - 1;
        }
        return -1;
    }

    /**
     * One node halfway between a node that a curve runs into and a node a curve runs on from,
     * each keeping its handle at the same offset.
     */
    private static Knot merge(Knot into, Knot from) {
        Point2D middle = new Point2D.Double((into.point.getX() + from.point.getX()) / 2,
                (into.point.getY() + from.point.getY()) / 2);
        return new Knot(middle, shifted(into.in, into.point, middle), shifted(from.out, from.point, middle));
    }

    private static Point2D shifted(Point2D handle, Point2D from, Point2D to) {
        if (handle == null) {
            return null;
        }
        return new Point2D.Double(handle.getX() + to.getX() - from.getX(), handle.getY() + to.getY() - from.getY());
    }

    /**
     * The nodes in the opposite direction, with the handles into and out of each swapped.
     */
    private static List<Knot> reversed(List<Knot> knots) {
        List<Knot> reversed = new ArrayList<>(knots.size());
        for (int i = knots.size() - 1; i >= 0; i--) {
            Knot knot = knots.get(i);
            reversed.add(new Knot(knot.point, knot.out, knot.in));
        }
        return reversed;
    }

    /**
     * Removes nodes from a subpath, from the last one so the places of the others stay put.
     */
    private static void removeKnots(Subpath subpath, Set<Integer> indexes) {
        List<Integer> descending = new ArrayList<>(indexes);
        descending.sort(Comparator.reverseOrder());
        for (int index : descending) {
            subpath.knots.remove(index);
        }
        // Two nodes cannot enclose anything, so what is left of a closed subpath is opened.
        if (subpath.closed && subpath.knots.size() < 3) {
            subpath.closed = false;
        }
    }

    /**
     * Appends a rebuilt subpath.
     *
     * @return whether it has anything to draw
     */
    private static boolean append(Path2D path, Subpath subpath) {
        return append(path, subpath.knots, subpath.closed);
    }

    private static boolean append(Path2D path, List<Knot> knots, boolean closed) {
        if (knots.size() < 2) {
            return false;
        }
        path.moveTo(knots.getFirst().point.getX(), knots.getFirst().point.getY());
        for (int i = 1; i < knots.size(); i++) {
            appendCurve(path, knots.get(i - 1), knots.get(i));
        }
        if (closed) {
            appendCurve(path, knots.getLast(), knots.getFirst());
            path.closePath();
        }
        return true;
    }

    private static void appendCurve(Path2D path, Knot from, Knot to) {
        if (from.out == null && to.in == null) {
            path.lineTo(to.point.getX(), to.point.getY());
            return;
        }
        Point2D c1 = from.out != null ? from.out : from.point;
        Point2D c2 = to.in != null ? to.in : to.point;
        path.curveTo(c1.getX(), c1.getY(), c2.getX(), c2.getY(), to.point.getX(), to.point.getY());
    }

    private static void appendSegment(Path2D path, Segment segment) {
        Point2D[] p = segment.getPoints();
        switch (segment.getType()) {
            case MOVE_TO -> path.moveTo(p[0].getX(), p[0].getY());
            case LINE_TO -> path.lineTo(p[0].getX(), p[0].getY());
            case QUAD_TO -> path.quadTo(p[0].getX(), p[0].getY(), p[1].getX(), p[1].getY());
            case CUBIC_TO -> path.curveTo(p[0].getX(), p[0].getY(), p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY());
            case CLOSE -> path.closePath();
        }
    }

    /**
     * Splits the segments into subpaths of nodes. Quadratic curves become the equivalent cubic
     * ones so every node has the same kind of handles.
     */
    private static List<Subpath> subpaths(List<Segment> segments) {
        List<Subpath> subpaths = new ArrayList<>();
        Subpath current = null;
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            Point2D[] p = segment.getPoints();
            if (segment.getType() == SegmentType.MOVE_TO || current == null) {
                current = new Subpath(i);
                subpaths.add(current);
                if (segment.getType() == SegmentType.MOVE_TO) {
                    current.knots.add(new Knot(copy(p[0]), null));
                    continue;
                }
            }
            current.lastSegment = i;
            Knot previous = current.knots.isEmpty() ? null : current.knots.getLast();
            switch (segment.getType()) {
                case LINE_TO -> current.knots.add(new Knot(copy(p[0]), null));
                case QUAD_TO -> {
                    Point2D from = previous != null ? previous.point : p[1];
                    if (previous != null) {
                        previous.out = towards(from, p[0], 2.0 / 3);
                    }
                    current.knots.add(new Knot(copy(p[1]), towards(p[1], p[0], 2.0 / 3)));
                }
                case CUBIC_TO -> {
                    if (previous != null) {
                        previous.out = copy(p[0]);
                    }
                    current.knots.add(new Knot(copy(p[2]), copy(p[1])));
                }
                case CLOSE -> close(current);
                default -> {
                }
            }
        }
        return subpaths;
    }

    /**
     * Closes a subpath. When it already ends where it started, those two nodes are one, so the
     * last one is folded into the first.
     */
    private static void close(Subpath subpath) {
        subpath.closed = true;
        List<Knot> knots = subpath.knots;
        if (knots.size() > 1) {
            Knot first = knots.getFirst();
            Knot last = knots.getLast();
            if (first.point.distance(last.point) <= SAME_POSITION) {
                first.in = last.in;
                knots.removeLast();
            }
        }
    }

    private static Point2D towards(Point2D from, Point2D to, double fraction) {
        return new Point2D.Double(from.getX() + (to.getX() - from.getX()) * fraction,
                from.getY() + (to.getY() - from.getY()) * fraction);
    }

    private static Point2D copy(Point2D point) {
        return new Point2D.Double(point.getX(), point.getY());
    }
}
