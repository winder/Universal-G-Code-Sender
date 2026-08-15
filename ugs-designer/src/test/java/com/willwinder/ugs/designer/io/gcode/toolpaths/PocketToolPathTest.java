package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.PlungeType;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.List;

public class PocketToolPathTest {

    @Test
    public void pocketShouldNotExceedTheGeometry() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = 10;
        int depthPerPass = 1;

        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(geometrySize, geometrySize));

        Settings settings = new Settings();
        settings.setSafeHeight(safeHeight);
        settings.setToolStepOver(1);
        settings.setToolDiameter(toolRadius * 2);
        settings.setDepthPerPass(1);

        com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath simplePocket = new com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath(settings, rectangle);
        simplePocket.setTargetDepth(targetDepth);

        List<Segment> segmentList = simplePocket.toGcodePath().getSegments();

        Segment firstSegment = segmentList.get(0);
        assertEquals("The segment should turn on spindle", SegmentType.SEAM, firstSegment.type);
        assertEquals("Default spindle speed is 100%", Integer.valueOf(255), firstSegment.getSpindleSpeed());

        Segment secondSegment = segmentList.get(1);
        assertEquals("The segment should move to safe height", safeHeight, secondSegment.point.getAxis(Axis.Z), 0.1);
        assertFalse("The segment should not move X", secondSegment.point.hasAxis(Axis.X));
        assertFalse("The segment should not move Y", secondSegment.point.hasAxis(Axis.Y));

        Segment thirdSegment = segmentList.get(2);
        assertFalse("The segment should not include height", thirdSegment.point.hasAxis(Axis.Z));
        assertEquals("The segment should move to first X position", safeHeight, thirdSegment.point.getAxis(Axis.X), toolRadius);
        assertEquals("The segment should move to first Y position", safeHeight, thirdSegment.point.getAxis(Axis.Y), toolRadius);

        // Make sure that we don't move outside the boundary of the geometry
        segmentList.stream()
                .filter(segment -> segment.type == SegmentType.LINE || segment.type == SegmentType.POINT)
                .forEach(segment -> {
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().getAxis(Axis.X), segment.getPoint().getAxis(Axis.X) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().getAxis(Axis.Y), segment.getPoint().getAxis(Axis.Y) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().getAxis(Axis.X), segment.getPoint().getAxis(Axis.X) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().getAxis(Axis.Y), segment.getPoint().getAxis(Axis.Y) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) <= 0);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) >= -targetDepth);
                });

        List<Segment> drillOperations = segmentList.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .toList();
        assertEquals("There should be a number of drill operations when making a pocket", Math.abs(targetDepth + depthPerPass) / depthPerPass, drillOperations.size(), 0.1);

        PartialPosition point = drillOperations.get(drillOperations.size() - 1).getPoint();
        assertEquals("Last operation should reach the target depth", -targetDepth, point.getAxis(Axis.Z), 0.1);
    }

    @Test
    public void pocketDirectionClimb() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = 1;

        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(geometrySize, geometrySize));
        rectangle.setDirection(Direction.CLIMB);

        Settings settings = new Settings();
        settings.setSafeHeight(safeHeight);
        settings.setToolStepOver(1);
        settings.setToolDiameter(toolRadius * 2);
        settings.setDepthPerPass(1);


        com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath simplePocket = new com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath(settings, rectangle);
        simplePocket.setTargetDepth(targetDepth);

        List<Segment> segmentList = simplePocket.toGcodePath().getSegments();


        assertEquals(SegmentType.SEAM, segmentList.get(0).getType());
        assertSegment(segmentList.get(1), SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setZ(1d).build(), null);
        assertSegment(segmentList.get(2), SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setX(2.5d).setY(2.5).build(), null);
        assertSegment(segmentList.get(3), SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setZ(1d).build(), null);
        assertSegment(segmentList.get(4), SegmentType.POINT, PartialPosition.builder(UnitUtils.Units.MM).setX(2.5d).setY(2.5).setZ(-0d).build(), null);
        assertSegment(segmentList.get(5), SegmentType.LINE, PartialPosition.builder(UnitUtils.Units.MM).setX(2.5d).setY(7.5).setZ(-0d).build(), 1000);
        assertSegment(segmentList.get(6), SegmentType.LINE, PartialPosition.builder(UnitUtils.Units.MM).setX(7.5d).setY(7.5).setZ(-0d).build(), 1000);
        assertSegment(segmentList.get(7), SegmentType.LINE, PartialPosition.builder(UnitUtils.Units.MM).setX(7.5d).setY(2.5).setZ(-0d).build(), 1000);
    }

    @Test
    public void pocketDirectionConventional() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = 1;

        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(geometrySize, geometrySize));
        rectangle.setDirection(Direction.CONVENTIONAL);
        rectangle.setFeedRate(2000);

        Settings settings = new Settings();
        settings.setSafeHeight(safeHeight);
        settings.setToolStepOver(1);
        settings.setToolDiameter(toolRadius * 2);
        settings.setDepthPerPass(1);


        com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath simplePocket = new com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath(settings, rectangle);
        simplePocket.setTargetDepth(targetDepth);

        List<Segment> segmentList = simplePocket.toGcodePath().getSegments();


        assertEquals(SegmentType.SEAM, segmentList.get(0).getType());
        assertSegment(segmentList.get(1), SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setZ(1d).build(), null);
        assertSegment(segmentList.get(2), SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setX(2.5d).setY(2.5).build(), null);
        assertSegment(segmentList.get(3), SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setZ(1d).build(), null);
        assertSegment(segmentList.get(4), SegmentType.POINT, PartialPosition.builder(UnitUtils.Units.MM).setX(2.5d).setY(2.5).setZ(-0d).build(), null);
        assertSegment(segmentList.get(5), SegmentType.LINE, PartialPosition.builder(UnitUtils.Units.MM).setX(7.5d).setY(2.5).setZ(-0d).build(), 2000);
        assertSegment(segmentList.get(6), SegmentType.LINE, PartialPosition.builder(UnitUtils.Units.MM).setX(7.5d).setY(7.5).setZ(-0d).build(), 2000);
        assertSegment(segmentList.get(7), SegmentType.LINE, PartialPosition.builder(UnitUtils.Units.MM).setX(2.5d).setY(7.5).setZ(-0d).build(), 2000);
    }


    private void assertSegment(Segment segment, SegmentType segmentType, PartialPosition partialPosition, Integer feedSpeed) {
        assertEquals(segmentType, segment.getType());
        assertEquals(UnitUtils.Units.MM, segment.getPoint().getUnits());
        assertEquals(partialPosition, segment.getPoint());
        assertEquals(feedSpeed, segment.getFeedSpeed());
    }


    @Test
    public void pocketOnRectangleWithHole() {
        double toolRadius = 2.5;
        double geometrySize = 10d;
        double safeHeight = 1;
        double targetDepth = 10;
        int depthPerPass = 1;

        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(geometrySize, geometrySize));


        Settings settings = new Settings();
        settings.setToolDiameter(toolRadius * 2);
        settings.setSafeHeight(safeHeight);
        settings.setToolStepOver(1);
        settings.setDepthPerPass(depthPerPass);

        com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath simplePocket = new com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath(settings, rectangle);
        simplePocket.setTargetDepth(targetDepth);

        List<Segment> segmentList = simplePocket.toGcodePath().getSegments();

        Segment firstSegment = segmentList.get(0);
        assertEquals("The segment should turn on spindle", SegmentType.SEAM, firstSegment.type);
        assertEquals("Default spindle speed is 100%", Integer.valueOf(255), firstSegment.getSpindleSpeed());

        Segment secondSegment = segmentList.get(1);
        assertEquals("The first segment should move to safe height", safeHeight, secondSegment.point.getAxis(Axis.Z), 0.1);
        assertFalse("The first segment should not move X", secondSegment.point.hasAxis(Axis.X));
        assertFalse("The first segment should not move Y", secondSegment.point.hasAxis(Axis.Y));

        Segment thirdSegment = segmentList.get(2);
        assertFalse("The segment should not include height", thirdSegment.point.hasAxis(Axis.Z));
        assertEquals("The segment should move to first X position", safeHeight, thirdSegment.point.getAxis(Axis.X), toolRadius);
        assertEquals("The segment should move to first Y position", safeHeight, thirdSegment.point.getAxis(Axis.Y), toolRadius);

        // Make sure that we don't move outside the boundary of the geometry
        segmentList.stream()
                .filter(segment -> segment.type == SegmentType.LINE || segment.type == SegmentType.POINT)
                .forEach(segment -> {
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().getAxis(Axis.X), segment.getPoint().getAxis(Axis.X) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().getAxis(Axis.Y), segment.getPoint().getAxis(Axis.Y) >= toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: X=" + segment.getPoint().getAxis(Axis.X), segment.getPoint().getAxis(Axis.X) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Y=" + segment.getPoint().getAxis(Axis.Y), segment.getPoint().getAxis(Axis.Y) <= geometrySize - toolRadius);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) <= 0);
                    assertTrue("Point was outside boundary of 10x10 shape: Z=" + segment.getPoint().getAxis(Axis.Z), segment.getPoint().getAxis(Axis.Z) >= -targetDepth);
                });

        List<Segment> drillOperations = segmentList.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .toList();
        assertEquals("There should be a number of drill operations when making a pocket", Math.abs(targetDepth + depthPerPass) / depthPerPass, drillOperations.size(), 0.1);

        PartialPosition point = drillOperations.get(drillOperations.size() - 1).getPoint();
        assertEquals("Last operation should reach the target depth", -targetDepth, point.getAxis(Axis.Z), 0.1);
    }

    @Test
    public void pocketOnTestFileCheckLengths() {
        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(PocketToolPathTest.class.getResourceAsStream("/pocket-test.ugsd")).orElseThrow(RuntimeException::new);

        double toolDiameter = 1;
        double safeHeight = 5;
        double startDepth = 1;
        double targetDepth = 1;
        int depthPerPass = 1;

        double totalLength = 0;
        double totalRapidLength = 0;

        Settings settings = new Settings();
        settings.setToolStepOver(0.5);
        settings.setSafeHeight(safeHeight);
        settings.setToolDiameter(toolDiameter);
        settings.setDepthPerPass(depthPerPass);

        for (Entity entity : design.getEntities()) {
            com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath simplePocket = new com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath(settings, (Cuttable) entity);
            simplePocket.setTargetDepth(targetDepth);
            simplePocket.setStartDepth(startDepth);

            GcodePath gcodePath = simplePocket.toGcodePath();
            ToolPathStats toolPathStats = com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.getToolPathStats(gcodePath);
            totalLength += toolPathStats.getTotalFeedLength();
            totalRapidLength += toolPathStats.getTotalRapidLength();
        }

        assertTrue("The tool path was " + Math.round(totalLength) + "mm long but should have been shorter", totalLength < 22144);
        assertTrue("The tool path rapids was " + Math.round(totalRapidLength) + "mm long but should have been shorter", totalRapidLength < 730);
    }

    @Test
    public void toGcodePath_shouldFollowCurvesCloserForFinerCurvePrecision() {
        Settings coarseSettings = new Settings();
        coarseSettings.setFlatnessPrecision(0.1);
        Settings fineSettings = new Settings();
        fineSettings.setFlatnessPrecision(0.005);

        int coarseSegments = pocketOfCircle(coarseSettings).getSize();

        assertTrue("A finer curve precision should keep more of the points describing the curve",
                coarseSegments < pocketOfCircle(fineSettings).getSize());
    }

    @Test
    public void toGcodePath_shouldRampIntoMaterialForEveryDepthOfCutWhenPlungeTypeIsLinearRamp() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(10, 10));
        rectangle.setDirection(Direction.CLIMB);
        rectangle.setPlungeType(PlungeType.LINEAR_RAMP);

        Settings settings = new Settings();
        settings.setToolStepOver(1);
        settings.setToolDiameter(5);
        settings.setDepthPerPass(1);

        PocketToolPath pocket = new PocketToolPath(settings, rectangle);
        pocket.setTargetDepth(2);
        List<Segment> segments = pocket.toGcodePath().getSegments();

        assertTrue("No pass should move straight down into the material",
                segments.stream()
                        .filter(segment -> segment.type == SegmentType.POINT)
                        .allMatch(segment -> segment.point.getZ() == 0));

        // The first pass removing material descends along the first edge of the pocket ring, which
        // starts in the lower left corner and continues along the Y axis
        assertRampsDownTo(segments, -1, 2.5, 6.23);

        // The pass after it carries on from there, descending around the corner of the ring
        assertRampsDownTo(segments, -2, 4.96, 7.5);
    }

    private static void assertRampsDownTo(List<Segment> segments, double depth, double x, double y) {
        List<Segment> pass = segments.stream()
                .filter(segment -> segment.type == SegmentType.LINE)
                .filter(segment -> Math.abs(segment.point.getAxis(Axis.Z) - depth) < 0.01)
                .toList();

        // The descent ends at the ramp angle along the ring
        assertSegmentAt(pass.get(0), x, y, depth);

        // And the ring is cut from there and back over the ramp again to clear it out
        assertSegmentAt(pass.get(pass.size() - 1), x, y, depth);
    }

    @Test
    public void toGcodePath_shouldClearOneAreaAtATimeWhenThePocketIsBrokenUp() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(0, 10);
        path.lineTo(10, 10);
        path.lineTo(10, 0);
        path.close();
        path.moveTo(50, 0);
        path.lineTo(50, 10);
        path.lineTo(60, 10);
        path.lineTo(60, 0);
        path.close();

        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(0.4);
        settings.setDepthPerPass(1);

        PocketToolPath pocket = new PocketToolPath(settings, path);
        pocket.setTargetDepth(2);
        List<Segment> segments = pocket.toGcodePath().getSegments();

        // The first area is cleared all the way to the target depth before the tool moves over to the
        // second one, rather than the tool travelling between the two of them at every depth
        List<Segment> plunges = segments.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .toList();

        assertEquals(6, plunges.size());
        assertPlungeAt(plunges.get(0), 4.5, 0);
        assertPlungeAt(plunges.get(1), 4.5, -1);
        assertPlungeAt(plunges.get(2), 4.5, -2);
        assertPlungeAt(plunges.get(3), 54.5, 0);
        assertPlungeAt(plunges.get(4), 54.5, -1);
        assertPlungeAt(plunges.get(5), 54.5, -2);
    }

    @Test
    public void toGcodePath_shouldRampInTheDirectionThatARingIsCutWhenThePocketHasSeveralRings() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(20, 20));
        rectangle.setDirection(Direction.CLIMB);
        rectangle.setPlungeType(PlungeType.LINEAR_RAMP);

        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(0.4);
        settings.setDepthPerPass(1);
        settings.setArcFitting(false);

        PocketToolPath pocket = new PocketToolPath(settings, rectangle);
        pocket.setTargetDepth(2);
        List<Segment> segments = pocket.toGcodePath().getSegments();

        // The first pass cuts the innermost ring clockwise, starting in its lower left corner
        assertSegmentAt(segments.get(5), 8.5, 11.5, 0);

        // The tool is moved a ramp length back along the ring before descending
        assertEquals(SegmentType.MOVE, segments.get(25).type);
        assertEquals(11.5, segments.get(25).point.getAxis(Axis.X), 0.01);
        assertEquals(9.23, segments.get(25).point.getAxis(Axis.Y), 0.01);

        // Descending follows the same direction the ring is cut in, ending where the ring starts
        assertSegmentAt(segments.get(28), 11.5, 8.5, -0.2);
        assertSegmentAt(segments.get(29), 8.5, 8.5, -1);
        assertSegmentAt(segments.get(30), 8.5, 11.5, -1);

        // And the ring is left to carry on to the next one the way it was laid out
        assertSegmentAt(segments.get(34), 8.5, 8.5, -1);
        assertSegmentAt(segments.get(35), 6.5, 6.5, -1);
    }

    @Test
    public void toGcodePath_shouldClearAreasThatLieCloseTogetherAfterEachOther() {
        Path path = new Path();
        square(path, 0, 0);
        square(path, 100, 0);
        square(path, 0, 20);

        Settings settings = new Settings();
        settings.setToolDiameter(3);
        settings.setDepthPerPass(1);

        PocketToolPath pocket = new PocketToolPath(settings, path);
        pocket.setTargetDepth(1);
        List<Segment> segments = pocket.toGcodePath().getSegments();

        List<Segment> plunges = segments.stream()
                .filter(segment -> segment.type == SegmentType.POINT)
                .toList();

        // The area in the lower left corner is cleared first, then the one right above it, and only
        // then the one on the far side of the design
        assertEquals(6, plunges.size());
        assertPlungeInSquare(plunges.get(0), 0, 0);
        assertPlungeInSquare(plunges.get(1), 0, 0);
        assertPlungeInSquare(plunges.get(2), 0, 20);
        assertPlungeInSquare(plunges.get(3), 0, 20);
        assertPlungeInSquare(plunges.get(4), 100, 0);
        assertPlungeInSquare(plunges.get(5), 100, 0);
    }

    private static void square(Path path, double x, double y) {
        path.moveTo(x, y);
        path.lineTo(x, y + 10);
        path.lineTo(x + 10, y + 10);
        path.lineTo(x + 10, y);
        path.close();
    }

    @Test
    public void toGcodePath_shouldCutEveryDepthOfCutInTheSameDirection() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(20, 20));
        rectangle.setDirection(Direction.CONVENTIONAL);

        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(0.4);
        settings.setDepthPerPass(1);
        settings.setArcFitting(false);

        PocketToolPath pocket = new PocketToolPath(settings, rectangle);
        pocket.setTargetDepth(2);
        List<Segment> segments = pocket.toGcodePath().getSegments();

        // Every pass clears the same area, so they all cut the same positions in the same order
        assertEquals(positionsAtDepth(segments, 0), positionsAtDepth(segments, -1));
        assertEquals(positionsAtDepth(segments, 0), positionsAtDepth(segments, -2));
    }

    private static List<String> positionsAtDepth(List<Segment> segments, double depth) {
        return segments.stream()
                .filter(segment -> segment.point != null && segment.point.hasX() && segment.point.hasZ())
                .filter(segment -> Math.abs(segment.point.getZ() - depth) < 0.001)
                .map(segment -> Math.round(segment.point.getX() * 100) + "," + Math.round(segment.point.getY() * 100))
                .toList();
    }

    private static void assertPlungeInSquare(Segment segment, double x, double y) {
        assertEquals(x + 5, segment.point.getAxis(Axis.X), 5);
        assertEquals(y + 5, segment.point.getAxis(Axis.Y), 5);
    }

    private static void assertPlungeAt(Segment segment, double x, double z) {
        assertEquals(x, segment.point.getAxis(Axis.X), 0.01);
        assertEquals(z, segment.point.getAxis(Axis.Z), 0.01);
    }

    private static void assertSegmentAt(Segment segment, double x, double y, double z) {
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(x, segment.point.getAxis(Axis.X), 0.01);
        assertEquals(y, segment.point.getAxis(Axis.Y), 0.01);
        assertEquals(z, segment.point.getAxis(Axis.Z), 0.01);
    }

    private static GcodePath pocketOfCircle(Settings settings) {
        Ellipse circle = new Ellipse(0, 0, 40, 40);
        com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath pocket =
                new com.willwinder.ugs.designer.io.gcode.toolpaths.PocketToolPath(settings, circle);
        pocket.setTargetDepth(1);
        return pocket.toGcodePath();
    }
}