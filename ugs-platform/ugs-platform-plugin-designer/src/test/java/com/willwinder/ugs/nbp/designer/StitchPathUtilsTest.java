package com.willwinder.ugs.nbp.designer;

import static com.willwinder.ugs.nbp.designer.PathUtils.getEndPoint;
import static com.willwinder.ugs.nbp.designer.PathUtils.getStartPoint;
import static com.willwinder.ugs.nbp.designer.StitchPathUtils.shapeToSegments;
import static com.willwinder.ugs.nbp.designer.StitchPathUtils.stitchSegments;
import com.willwinder.ugs.nbp.designer.model.path.EditablePath;
import com.willwinder.ugs.nbp.designer.model.path.Segment;
import com.willwinder.ugs.nbp.designer.model.path.SegmentType;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StitchPathUtilsTest {
    private static final Point2D POINT_1 = new Point2D.Double(0, 0);
    private static final Point2D POINT_2 = new Point2D.Double(10, 0);
    private static final Point2D POINT_3 = new Point2D.Double(10, 10);
    private static final Point2D POINT_4 = new Point2D.Double(0, 10);

    @Test
    public void stitchSegmentsWithLinesShouldIgnoreDuplicateSegments() {
        Set<Segment> segments = new LinkedHashSet<>();
        segments.add(createLineSegment(POINT_1, POINT_2));
        segments.add(createLineSegment(POINT_2, POINT_1));
        segments.add(createLineSegment(POINT_1, POINT_2));
        segments.add(createLineSegment(POINT_2, POINT_1));

        List<Path2D> result = stitchSegments(segments);
        assertEquals("Expects one shape to be generated", 1, result.size());

        Path2D shape1 = result.get(0);
        assertEquals(POINT_1, getStartPoint(shape1));
        assertEquals(POINT_2, getEndPoint(shape1));

        EditablePath path = EditablePath.fromShape(result.get(0));
        assertEquals("Expects shape to contain one line", 2, path.getSegments().size());
        assertEquals(SegmentType.MOVE_TO, path.getSegments().get(0).getType());
        assertEquals(SegmentType.LINE_TO, path.getSegments().get(1).getType());
    }

    @Test
    public void stitchSegmentsWithShouldCreateAClosedShape() {
        Set<Segment> segments = new HashSet<>();
        segments.add(createLineSegment(POINT_1, POINT_2));
        segments.add(createLineSegment(POINT_2, POINT_3));
        segments.add(createLineSegment(POINT_3, POINT_4));
        segments.add(createLineSegment(POINT_4, POINT_1));

        List<Path2D> result = stitchSegments(segments);
        assertEquals("Expects one shape to be generated", 1, result.size());

        EditablePath path = EditablePath.fromShape(result.get(0));
        assertEquals("Expects shape to contain all lines", 6, path.getSegments().size());
        assertEquals(SegmentType.MOVE_TO, path.getSegments().get(0).getType());
        assertEquals(SegmentType.CLOSE, path.getSegments().get(5).getType());
    }

    @Test
    public void stitchSegmentsWithShouldRemoveDuplicateSegmentsBackAndForth() {
        Set<Segment> segments = new HashSet<>();
        segments.add(createLineSegment(POINT_1, POINT_2));
        segments.add(createLineSegment(POINT_2, POINT_1));

        List<Path2D> result = stitchSegments(segments);
        assertEquals("Expects one shape to be generated", 1, result.size());

        List<Segment> s = shapeToSegments(result.get(0));
        assertEquals("Expects shape to contain all lines", 1, s.size());
    }

    @Test
    public void stitchSegmentsWithLinesShouldCreateOnePath() {
        Set<Segment> segments = new HashSet<>();
        segments.add(createLineSegment(new Point2D.Double(0, 0), new Point2D.Double(0, 100)));
        segments.add(createLineSegment(new Point2D.Double(0, 100), new Point2D.Double(100, 100)));
        segments.add(createLineSegment(new Point2D.Double(100, 100), new Point2D.Double(100, 0)));
        segments.add(createLineSegment(new Point2D.Double(100, 0), new Point2D.Double(0, 0)));

        List<Path2D> result = stitchSegments(segments);
        assertEquals("Expects one shape to be generated", 1, result.size());

        EditablePath path = EditablePath.fromShape(result.get(0));
        assertEquals("Expects shape to contain all lines", 6, path.getSegments().size());
        assertEquals(SegmentType.MOVE_TO, path.getSegments().get(0).getType());
        assertEquals(SegmentType.CLOSE, path.getSegments().get(5).getType());
    }

    @Test
    public void stitchSegmentsOutOfOrderShouldCreateOnePath() {
        Set<Segment> segments = new HashSet<>();
        segments.add(createLineSegment(new Point2D.Double(0, 100), new Point2D.Double(0, 0)));
        segments.add(createLineSegment(new Point2D.Double(0, 100), new Point2D.Double(100, 100)));
        segments.add(createLineSegment(new Point2D.Double(100, 0), new Point2D.Double(100, 100)));
        segments.add(createLineSegment(new Point2D.Double(100, 0), new Point2D.Double(0, 0)));

        List<Path2D> result = stitchSegments(segments);
        assertEquals("Expects one shape to be generated", 1, result.size());

        EditablePath path = EditablePath.fromShape(result.get(0));
        assertEquals("Expects shape to contain all lines", 6, path.getSegments().size());
        assertEquals(SegmentType.MOVE_TO, path.getSegments().get(0).getType());
        assertEquals(SegmentType.CLOSE, path.getSegments().get(5).getType());
    }

    @Test
    public void compareTest() {
        Segment segment1 = createLineSegment(new Point2D.Double(0, 0), new Point2D.Double(10.00001, 10));
        Segment segment2 = createLineSegment(new Point2D.Double(0, 0.00001), new Point2D.Double(10, 10));
        assertEquals(segment1, segment2);
    }

    @Test
    public void canonicalKey() {
        Path2D line1 = createLine(POINT_1, POINT_2);
        Path2D line2 = createLine(POINT_2, POINT_1);
        assertEquals(StitchPathUtils.canonicalKey(line1), StitchPathUtils.canonicalKey(line2));
    }

    private Segment createLineSegment(Point2D point1, Point2D point2) {
        return new Segment(SegmentType.LINE_TO, point1, point2, new Point2D[]{point1, point2});
    }

    private Path2D createLine(Point2D... points) {
        Path2D path = new Path2D.Double();
        boolean first = true;
        for (Point2D point : points) {
            if (first) {
                path.moveTo(point.getX(), point.getY());
                first = false;
            } else {
                path.lineTo(point.getX(), point.getY());
            }

        }
        return path;
    }
}