package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.gcode.path.*;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class SimplePocketTest {

    @Test
    public void pocketShouldNotExceedTheGeometry() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = -10;
        int depthPerPass = 1;

        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(geometrySize, geometrySize));

        SimplePocket simplePocket = new SimplePocket(rectangle);
        simplePocket.setTargetDepth(targetDepth);
        simplePocket.setDepthPerPass(depthPerPass);
        simplePocket.setToolDiameter(toolRadius * 2);
        simplePocket.setStepOver(1);
        simplePocket.setSafeHeight(safeHeight);

        List<Segment> segmentList = simplePocket.toGcodePath().getSegments();

        Segment firstSegment = segmentList.get(0);
        assertEquals("The first segment should move to safe height", safeHeight, firstSegment.point.getAxis(Axis.Z), 0.1);
        assertFalse("The first segment should not move X", firstSegment.point.hasAxis(Axis.X));
        assertFalse("The first segment should not move Y", firstSegment.point.hasAxis(Axis.Y));

        Segment secondSegment = segmentList.get(1);
        assertEquals("The second segment should move to safe height", safeHeight, firstSegment.point.getAxis(Axis.Z), 0.1);
        assertEquals("The second segment should move to first X position", safeHeight, secondSegment.point.getAxis(Axis.X), toolRadius);
        assertEquals("The second segment should move to first Y position", safeHeight, secondSegment.point.getAxis(Axis.Y), toolRadius);

        // Make sure that we don't move outside the boundary of the geometry
        segmentList.stream()
                .filter(segment -> segment.type == SegmentType.LINE || segment.type == SegmentType.POINT)
                .forEach(segment -> {
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().getAxis(Axis.X), segment.getPoint().getAxis(Axis.X) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().getAxis(Axis.Y), segment.getPoint().getAxis(Axis.Y) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().getAxis(Axis.X), segment.getPoint().getAxis(Axis.X) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().getAxis(Axis.Y), segment.getPoint().getAxis(Axis.Y) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) < 0);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) >= targetDepth);
                });

        List<Segment> drillOperations = segmentList.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .collect(Collectors.toList());
        assertEquals("There should be a number of drill operations when making a pocket", Math.abs(targetDepth / depthPerPass), drillOperations.size(), 0.1);

        PartialPosition point = drillOperations.get(drillOperations.size() - 1).getPoint();
        assertEquals("Last operation should reach the target depth", targetDepth, point.getAxis(Axis.Z), 0.1);
    }
}