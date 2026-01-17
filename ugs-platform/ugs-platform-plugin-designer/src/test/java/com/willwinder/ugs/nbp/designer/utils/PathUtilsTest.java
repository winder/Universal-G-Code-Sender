package com.willwinder.ugs.nbp.designer.utils;

import com.willwinder.ugs.nbp.designer.model.path.Segment;
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
