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
package com.willwinder.ugs.nbp.designer;

import static com.willwinder.ugs.nbp.designer.PathUtils.EPS;
import static com.willwinder.ugs.nbp.designer.PathUtils.joinPaths;
import static com.willwinder.ugs.nbp.designer.PathUtils.quantize;
import static com.willwinder.ugs.nbp.designer.PathUtils.reversePath;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.model.path.Segment;
import com.willwinder.ugs.nbp.designer.model.path.SegmentEndpoint;
import com.willwinder.ugs.nbp.designer.model.path.SegmentType;
import com.willwinder.universalgcodesender.utils.KDTree;
import static com.willwinder.universalgcodesender.utils.MathUtils.isEqual;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Util class for stitching paths
 *
 * @author Joacim Breiler
 */
public class StitchPathUtils {

    public static List<? extends Entity> stitchEntities(List<Entity> entities) {
        Set<Segment> segments = extractSegments(entities);
        return stitchSegments(segments).stream()
                .map(Path::new)
                .toList();
    }

    static Set<Segment> extractSegments(List<Entity> selection) {
        Set<Segment> segments = new LinkedHashSet<>();
        for (Entity s : selection) {
            segments.addAll(shapeToSegments(s.getShape()));
        }
        return segments;
    }

    /**
     * Breaks a shape into a list of segments.
     *
     * @param shape the shape to break down
     * @return a list of path segments
     */
    protected static List<Segment> shapeToSegments(Shape shape) {
        return pathIteratorToSegments(shape.getPathIterator(null));
    }

    private static List<Segment> pathIteratorToSegments(PathIterator it) {
        List<Segment> segments = new ArrayList<>();

        double[] c = new double[6];

        List<Point2D> points = new ArrayList<>();
        Point2D start = null;
        Point2D last = null;
        SegmentType currentType = null;

        while (!it.isDone()) {
            int t = it.currentSegment(c);
            SegmentType type = SegmentType.fromPathIteratorType(t);

            switch (t) {
                case PathIterator.SEG_MOVETO -> {
                    // finalize previous open segment
                    if (start != null && !isEqual(start, last, EPS)) {
                        segments.add(new Segment(
                                currentType,
                                start,
                                last,
                                points.toArray(Point2D[]::new)
                        ));
                    }

                    points.clear();
                    start = last = new Point2D.Double(c[0], c[1]);
                    currentType = type;
                }

                case PathIterator.SEG_LINETO -> {
                    if (last == null
                            || !isEqual(c[0], last.getX(), EPS)
                            || !isEqual(c[1], last.getY(), EPS)) {

                        last = new Point2D.Double(c[0], c[1]);
                        points.add(last);
                        currentType = type;
                    }
                }

                case PathIterator.SEG_QUADTO -> {
                    Point2D ctrl = new Point2D.Double(c[0], c[1]);
                    Point2D end = new Point2D.Double(c[2], c[3]);
                    points.add(ctrl);
                    points.add(end);
                    last = end;
                    currentType = type;
                }

                case PathIterator.SEG_CUBICTO -> {
                    Point2D ctrl1 = new Point2D.Double(c[0], c[1]);
                    Point2D ctrl2 = new Point2D.Double(c[2], c[3]);
                    Point2D end = new Point2D.Double(c[4], c[5]);
                    points.add(ctrl1);
                    points.add(ctrl2);
                    points.add(end);
                    last = end;
                    currentType = type;
                }

                case PathIterator.SEG_CLOSE -> {
                    segments.add(new Segment(
                            SegmentType.CLOSE,
                            last,
                            last,
                            points.toArray(Point2D[]::new)
                    ));
                    points.clear();
                    start = last = null;
                    currentType = null;
                }
            }

            it.next();
        }

        if (start != null && !isEqual(start, last, EPS)) {
            segments.add(new Segment(
                    currentType,
                    start,
                    last,
                    points.toArray(Point2D[]::new)
            ));
        }

        return segments;
    }

    protected static String canonicalKey(Path2D path) {
        String forward = serialize(path);
        String reversed = serialize(reversePath(path));
        return forward.compareTo(reversed) <= 0 ? forward : reversed;
    }

    private static String serialize(Path2D path) {
        List<double[]> segments = new ArrayList<>();
        PathIterator it = path.getPathIterator(null);
        double[] c = new double[6];

        while (!it.isDone()) {
            int t = it.currentSegment(c);
            int n = PathUtils.getNumberOfCoordsInSegmentType(t);
            double[] data = new double[n + 1];
            data[0] = t;

            for (int i = 0; i < n; i++) {
                data[i + 1] = quantize(c[i]);
            }

            segments.add(data);
            it.next();
        }

        StringBuilder sb = new StringBuilder();
        for (double[] s : segments) {
            for (double v : s) {
                sb.append(v).append(',');
            }
            sb.append(';');
        }
        return sb.toString();
    }

    private static Set<Segment> removeBacktrackingDuplicates(Set<Segment> input) {
        Map<String, Segment> unique = new LinkedHashMap<>();

        for (Segment s : input) {
            String key = canonicalKey(s.path());
            unique.putIfAbsent(key, s);
        }

        return new LinkedHashSet<>(unique.values());
    }

    protected static List<Path2D> stitchSegments(Set<Segment> segments) {
        Deque<Segment> availableSegments = new ArrayDeque<>(removeBacktrackingDuplicates(segments));
        KDTree<SegmentEndpoint> tree = createKdTree(availableSegments);

        List<List<Path2D>> result = new ArrayList<>();
        while (!availableSegments.isEmpty()) {
            List<Path2D> path = new ArrayList<>();
            SegmentEndpoint startSegmentEndpoint = tree.getValues().stream()
                    .filter(e -> availableSegments.contains(e.segment()))
                    .findFirst()
                    .orElse(null);
            if (startSegmentEndpoint == null) {
                break;
            }
            Path2D segmentPath = startSegmentEndpoint.segment().path();
            if (!startSegmentEndpoint.isStart()) {
                segmentPath = reversePath(segmentPath);
            }
            path.add(segmentPath);
            availableSegments.removeIf(startSegmentEndpoint.segment()::equals);

            stitch(startSegmentEndpoint, path, availableSegments, tree, true);
            stitch(startSegmentEndpoint, path, availableSegments, tree, false);
            result.add(path);
        }

        return result.stream()
                .map(s -> joinPaths(s, EPS))
                .toList();
    }

    private static KDTree<SegmentEndpoint> createKdTree(Collection<Segment> segments) {
        KDTree<SegmentEndpoint> tree = new KDTree<>();
        for (Segment s : segments) {
            tree.insert(s.getStartPoint(), new SegmentEndpoint(s.getStartPoint(), true, s));
            tree.insert(s.getLastPoint(), new SegmentEndpoint(s.getLastPoint(), false, s));
        }
        return tree;
    }

    private static void stitch(
            SegmentEndpoint startSegmentEndpoint,
            List<Path2D> path,
            Deque<Segment> availableSegments,
            KDTree<SegmentEndpoint> tree,
            boolean forward
    ) {
        SegmentEndpoint current = startSegmentEndpoint;

        while (true) {
            Point2D startPoint = current.isStart() ? current.segment().getStartPoint() : current.segment().getLastPoint();
            Point2D lastPoint = current.isStart() ? current.segment().getLastPoint() : current.segment().getStartPoint();

            Optional<SegmentEndpoint> next = tree
                    .search(forward ? lastPoint : startPoint, EPS)
                    .stream()
                    .filter(e -> availableSegments.contains(e.segment()))
                    .findFirst();

            if (next.isEmpty()) {
                return;
            }

            current = next.get();
            availableSegments.removeIf(current.segment()::equals);

            Path2D segmentPath = current.segment().path();
            if (!forward || !current.isStart()) {
                segmentPath = reversePath(current.segment().path());
            }

            if (forward) {
                path.add(segmentPath);
            } else {
                path.add(0, segmentPath);
            }
        }
    }
}
