package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PocketToolPathTest {

    @Test
    public void pocketShouldNotExceedTheGeometry() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = -10;
        int depthPerPass = 1;

        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(geometrySize, geometrySize));

        PocketToolPath simplePocket = new PocketToolPath(rectangle);
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
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) <= 0);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) >= targetDepth);
                });

        List<Segment> drillOperations = segmentList.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .collect(Collectors.toList());
        assertEquals("There should be a number of drill operations when making a pocket", Math.abs((targetDepth - depthPerPass) / depthPerPass), drillOperations.size(), 0.1);

        PartialPosition point = drillOperations.get(drillOperations.size() - 1).getPoint();
        assertEquals("Last operation should reach the target depth", targetDepth, point.getAxis(Axis.Z), 0.1);
    }


    @Test
    public void pocketOnRectangleWithHole() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = -10;
        int depthPerPass = 1;

        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(geometrySize, geometrySize));

        PocketToolPath simplePocket = new PocketToolPath(rectangle);
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
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) <= 0);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) >= targetDepth);
                });

        List<Segment> drillOperations = segmentList.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .collect(Collectors.toList());
        assertEquals("There should be a number of drill operations when making a pocket", Math.abs((targetDepth - depthPerPass) / depthPerPass), drillOperations.size(), 0.1);

        PartialPosition point = drillOperations.get(drillOperations.size() - 1).getPoint();
        assertEquals("Last operation should reach the target depth", targetDepth, point.getAxis(Axis.Z), 0.1);
    }

    @Test
    public void pocketOnTestFileCheckLengths() {
        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(PocketToolPathTest.class.getResourceAsStream("/pocket-test.ugsd")).orElseThrow(RuntimeException::new);

        double toolDiameter = 1;
        double safeHeight = 5;
        double startDepth = -1;
        double targetDepth = -1;
        int depthPerPass = 1;

        double totalLength = 0;
        double totalRapidLength = 0;

        for (Entity entity : design.getEntities()) {
            PocketToolPath simplePocket = new PocketToolPath((Cuttable) entity);
            simplePocket.setTargetDepth(targetDepth);
            simplePocket.setStartDepth(startDepth);
            simplePocket.setDepthPerPass(depthPerPass);
            simplePocket.setToolDiameter(toolDiameter);
            simplePocket.setStepOver(0.5);
            simplePocket.setSafeHeight(safeHeight);

            GcodePath gcodePath = simplePocket.toGcodePath();
            ToolPathStats toolPathStats = ToolPathUtils.getToolPathStats(gcodePath);
            totalLength += toolPathStats.getTotalFeedLength();
            totalRapidLength += toolPathStats.getTotalRapidLength();
        }

        assertTrue("The tool path was " + Math.round(totalLength) + "mm long but should have been shorter", totalLength < 22144);
        assertTrue("The tool path rapids was " + Math.round(totalRapidLength) + "mm long but should have been shorter", totalRapidLength < 676);
    }
}