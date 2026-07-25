package com.willwinder.ugs.designer.utils;

import com.willwinder.ugs.designer.model.path.Segment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class PathUtilsTest {

    @Test
    public void hasDrawableSegmentsWithOneMoveToIsNotDrawable() {
        Path2D path = new Path2D.Double();
        path.moveTo(10, 10);
        assertFalse(PathUtils.hasDrawableSegments(path));
    }

    @Test
    public void hasDrawableSegmentsWithMoveAndCloseIsNotDrawable() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.closePath();
        assertFalse(PathUtils.hasDrawableSegments(path));
    }

    @Test
    public void isClosedWithEmptyPathShouldNotBeClosed() {
        assertFalse(PathUtils.isClosed(new Path2D.Double()));
    }

    @Test
    public void isClosedWithExplicitlyClosedSubPathShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.closePath();

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithExplicitlyClosedPathShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(-1.541622047244094, -4.57409842519685);
        path.lineTo(-1.5415666398366867, -4.574095897346165);
        path.closePath();

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithSubPathEndingAtItsStartPointShouldBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(0, 0);

        assertTrue(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithOpenSubPathShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithAllSubPathsEndingAtTheirStartPointShouldBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(0, 0);
        path.moveTo(20, 0);
        path.lineTo(30, 0);
        path.lineTo(20, 0);

        assertTrue(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithAllSubPathsExplicitlyClosedShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.closePath();
        path.moveTo(20, 0);
        path.lineTo(30, 0);
        path.closePath();

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithAllSubPathsOpenShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.moveTo(20, 0);
        path.lineTo(30, 0);

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithAClosedSubPathFollowedByAnOpenShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.closePath();
        path.moveTo(20, 0);
        path.lineTo(30, 0);

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithAnOpenSubPathFollowedByAClosedShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(20, 0);
        path.lineTo(30, 0);
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.closePath();

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithAnOpenSubPathFollowedByAnImplicitlyClosedShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(20, 0);
        path.lineTo(30, 0);
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(0, 0);

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithSegmentsAfterAClosedSubPathShouldNotBeClosed() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.closePath();
        path.lineTo(30, 30);

        assertFalse(PathUtils.isClosed(path));
    }

    @Test
    public void isClosedWithCurvedSubPathsShouldCheckTheirEndPoints() {
        Path2D closed = new Path2D.Double();
        closed.moveTo(0, 0);
        closed.curveTo(5, 5, 10, 5, 0, 0);

        Path2D open = new Path2D.Double();
        open.moveTo(0, 0);
        open.quadTo(5, 5, 10, 10);

        assertTrue(PathUtils.isClosed(closed));
        assertFalse(PathUtils.isClosed(open));
    }

    @Test
    public void joinPathsWithEmptyListShouldReturnEmptyPath() {
        List<Path2D> paths = new ArrayList<>();
        assertTrue(PathUtils.joinPaths(paths, PathUtils.EPS).isEmpty());
    }

    @Test
    public void joinPathsShouldJoinTwoLines() {
        List<Path2D> paths = new ArrayList<>();
        paths.add(createLine(0, 0, 10, 10));
        paths.add(createLine(10, 10, 20, 20));

        Path2D result = PathUtils.joinPaths(paths, PathUtils.EPS).get();

        List<Segment> segments = PathUtils.getSegments(result.getPathIterator(null));
        assertEquals(3, segments.size());
    }

    @Test
    public void joinPathsShouldJoinTwoLiness() {
        List<Path2D> paths = new ArrayList<>();
        paths.add(createLine(0, 0, 10, 10));
        paths.add(createLine(11, 11, 20, 20));

        Path2D result = PathUtils.joinPaths(paths, PathUtils.EPS).get();

        List<Segment> segments = PathUtils.getSegments(result.getPathIterator(null));
        assertEquals(4, segments.size());
    }

    @Test
    public void joinPathsShouldFilterOutMultipleMoveTo() {
        List<Path2D> paths = new ArrayList<>();
        paths.add(createLine(0, 0, 10, 10));
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        paths.add(path);
        path = new Path2D.Double();
        path.moveTo(0, 0);
        paths.add(path);
        paths.add(createLine(11, 11, 20, 20));
        path = new Path2D.Double();
        path.moveTo(0, 0);
        paths.add(path);

        Path2D result = PathUtils.joinPaths(paths, PathUtils.EPS).get();

        List<Segment> segments = PathUtils.getSegments(result.getPathIterator(null));
        assertEquals(4, segments.size());
    }

    private Path2D createLine(double x0, double y0, double x1, double y1) {
        Path2D path2D = new Path2D.Double();
        path2D.moveTo(x0, y0);
        path2D.lineTo(x1, y1);
        return path2D;
    }
}
