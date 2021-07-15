package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.*;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimplePocketTest {

    @Test
    public void pocketShouldNotExceedTheGeometry() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = -10;
        int depthPerPass = 1;

        GcodePath path = new GcodePath();
        path.addSegment(SegmentType.MOVE, new NumericCoordinate(0d, 0d, 0d));
        path.addSegment(SegmentType.LINE, new NumericCoordinate(0d, geometrySize, 0d));
        path.addSegment(SegmentType.LINE, new NumericCoordinate(geometrySize, geometrySize, 0d));
        path.addSegment(SegmentType.LINE, new NumericCoordinate(geometrySize, 0d, 0d));
        path.addSegment(SegmentType.LINE, new NumericCoordinate(0d, 0d, 0d));

        SimplePocket simplePocket = new SimplePocket(path);
        simplePocket.setTargetDepth(targetDepth);
        simplePocket.setDepthPerPass(depthPerPass);
        simplePocket.setToolDiameter(toolRadius * 2);
        simplePocket.setStepOver(1);
        simplePocket.setSafeHeight(safeHeight);

        List<Segment> segmentList = simplePocket.toGcodePath().getSegments();

        Segment firstSegment = segmentList.get(0);
        assertEquals("The first segment should move to safe height", safeHeight, firstSegment.point.get(Axis.Z), 0.1);
        assertEquals("The first segment should not move X", Double.NaN, firstSegment.point.get(Axis.X), 0.1);
        assertEquals("The first segment should not move Y", Double.NaN, firstSegment.point.get(Axis.Y), 0.1);

        Segment secondSegment = segmentList.get(1);
        assertEquals("The second segment should move to safe height", safeHeight, firstSegment.point.get(Axis.Z), 0.1);
        assertEquals("The second segment should move to first X position", safeHeight, secondSegment.point.get(Axis.X), toolRadius);
        assertEquals("The second segment should move to first Y position", safeHeight, secondSegment.point.get(Axis.Y), toolRadius);

        // Make sure that we don't move outside the boundary of the geometry
        segmentList.stream()
                .filter(segment -> segment.type == SegmentType.LINE || segment.type == SegmentType.POINT)
                .forEach(segment -> {
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().get(Axis.X) , segment.getPoint().get(Axis.X) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().get(Axis.Y), segment.getPoint().get(Axis.Y) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().get(Axis.X) , segment.getPoint().get(Axis.X) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().get(Axis.Y), segment.getPoint().get(Axis.Y) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().get(Axis.Z), segment.getPoint().get(Axis.Z) < 0);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().get(Axis.Z), segment.getPoint().get(Axis.Z) >= targetDepth);
                });

        List<Segment> drillOperations = segmentList.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .collect(Collectors.toList());
        assertEquals("There should be a number of drill operations when making a pocket", Math.abs(targetDepth/depthPerPass), drillOperations.size(), 0.1);

        Coordinate point = drillOperations.get(drillOperations.size() - 1).getPoint();
        assertEquals("Last operation should reach the target depth", targetDepth, point.get(Axis.Z), 0.1);
    }
}