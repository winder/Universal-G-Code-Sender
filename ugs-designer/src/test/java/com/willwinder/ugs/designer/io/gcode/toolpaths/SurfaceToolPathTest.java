/*
    Copyright 2025 Will Winder

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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.model.PartialPosition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.List;

public class SurfaceToolPathTest {

    @Test
    public void toGcodePathShouldGenerateGcodeFromStartDepth() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);

        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, segments.size());


        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(1000), segment.getFeedSpeed());
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 2.5, 2.5);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 7.5, 2.5);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 2.5, 7.5);
        assertFalse(segment.point.hasZ());

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 7.5, 7.5);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldCoverTheWholeHeightWithConventionalMilling() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        rectangle.setDirection(Direction.CONVENTIONAL);

        Settings settings = new Settings();
        settings.setToolDiameter(8);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(1000), segment.getFeedSpeed());
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 4, 6);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 6, 6);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 4, 4);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 6, 4);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }


    @Test
    public void toGcodePathShouldCoverTheWholeWidthWithConventionalMillingVertically() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        rectangle.setDirection(Direction.CONVENTIONAL);
        rectangle.setToolPathDirection(ToolPathDirection.VERTICAL);

        Settings settings = new Settings();
        settings.setToolDiameter(8);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(1000), segment.getFeedSpeed());
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 6, 6);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 6, 4);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 4, 6);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 4, 4);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldCoverTheWholeHeightWithClimbMilling() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        rectangle.setDirection(Direction.CLIMB);

        Settings settings = new Settings();
        settings.setToolDiameter(8);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(1000), segment.getFeedSpeed());
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 4, 4);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 6, 4);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 4, 6);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 6, 6);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldCoverTheWholeHeightWithBothMilling() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        rectangle.setDirection(Direction.BOTH);

        Settings settings = new Settings();
        settings.setToolDiameter(2);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(18, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(1000), segment.getFeedSpeed());
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 1, 1);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 9, 1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 9, 3);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 1, 3);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 1, 5);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 9, 5);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 9, 7);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 1, 7);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 1, 9);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 9, 9);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldGenerateGcodeFromNegativeStartDepth() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);

        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(1000), segment.getFeedSpeed());
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 11);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 2.5, 2.5);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 7.5, 2.5);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 11);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 2.5, 7.5);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 7.5, 7.5);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 11);
    }

    private static void assertZPoint(PartialPosition point, int expected) {
        assertFalse(point.hasX());
        assertFalse(point.hasY());
        assertEquals(expected, point.getZ(), 0.01);
    }

    @Test
    public void toGcodePathShouldGenerateGcodeFromStartDepthWithLeadInAndOut() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(100);

        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(1000), segment.getFeedSpeed());
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        // Move to XY start
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, -2.5, 2.5);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        PartialPosition point = segment.point;
        assertXYPoint(point, 12.5, 2.5);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, -2.5, 7.5);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 12.5, 7.5);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldStopRoughingPassesAtStockToLeaveAndAddFinishingPass() {
        Rectangle rectangle = createSurfaceRectangle();
        rectangle.setFinishingPass(true);
        rectangle.setStockToLeave(0.2);

        SurfaceToolPath toolPath = new SurfaceToolPath(createSurfaceSettings(), rectangle);
        toolPath.setStartDepth(0);
        toolPath.setTargetDepth(2);

        List<Double> depths = getCuttingDepths(toolPath.toGcodePath(), 10);
        assertEquals(4, depths.size());
        assertEquals(0d, depths.get(0), 0.001);
        assertEquals(-1d, depths.get(1), 0.001);
        assertEquals(-1.8d, depths.get(2), 0.001);
        assertEquals(-2d, depths.get(3), 0.001);
    }

    @Test
    public void toGcodePathShouldCutToTargetDepthWithoutFinishingPass() {
        Rectangle rectangle = createSurfaceRectangle();
        rectangle.setFinishingPass(false);
        rectangle.setStockToLeave(0.2);

        SurfaceToolPath toolPath = new SurfaceToolPath(createSurfaceSettings(), rectangle);
        toolPath.setStartDepth(0);
        toolPath.setTargetDepth(2);

        List<Double> depths = getCuttingDepths(toolPath.toGcodePath(), 10);
        assertEquals(3, depths.size());
        assertEquals(0d, depths.get(0), 0.001);
        assertEquals(-1d, depths.get(1), 0.001);
        assertEquals(-2d, depths.get(2), 0.001);
    }

    @Test
    public void toGcodePathShouldAddFinishingPassWhenStartDepthIsTheTargetDepth() {
        Rectangle rectangle = createSurfaceRectangle();
        rectangle.setFinishingPass(true);
        rectangle.setStockToLeave(0.2);

        SurfaceToolPath toolPath = new SurfaceToolPath(createSurfaceSettings(), rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        // The single pass must be raised so that there is stock left for the finishing pass
        List<Double> depths = getCuttingDepths(toolPath.toGcodePath(), 9);
        assertEquals(2, depths.size());
        assertEquals(-0.8d, depths.get(0), 0.001);
        assertEquals(-1d, depths.get(1), 0.001);
    }

    @Test
    public void toGcodePathShouldAddFinishingPassWhenStartDepthIsBelowTheStockToLeave() {
        Rectangle rectangle = createSurfaceRectangle();
        rectangle.setFinishingPass(true);
        rectangle.setStockToLeave(0.2);

        SurfaceToolPath toolPath = new SurfaceToolPath(createSurfaceSettings(), rectangle);
        toolPath.setStartDepth(1.9);
        toolPath.setTargetDepth(2);

        List<Double> depths = getCuttingDepths(toolPath.toGcodePath(), 8.1);
        assertEquals(2, depths.size());
        assertEquals(-1.8d, depths.get(0), 0.001);
        assertEquals(-2d, depths.get(1), 0.001);
    }

    @Test
    public void toGcodePathShouldAddFinishingPassWhenStartDepthIsNegative() {
        Rectangle rectangle = createSurfaceRectangle();
        rectangle.setFinishingPass(true);
        rectangle.setStockToLeave(0.2);

        SurfaceToolPath toolPath = new SurfaceToolPath(createSurfaceSettings(), rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);

        List<Double> depths = getCuttingDepths(toolPath.toGcodePath(), 11);
        assertEquals(2, depths.size());
        assertEquals(1.2d, depths.get(0), 0.001);
        assertEquals(1d, depths.get(1), 0.001);
    }

    @Test
    public void toGcodePathShouldAddFinishingPassWhenStockToLeaveExceedsTheCutDepth() {
        Rectangle rectangle = createSurfaceRectangle();
        rectangle.setFinishingPass(true);
        rectangle.setStockToLeave(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(createSurfaceSettings(), rectangle);
        toolPath.setStartDepth(0);
        toolPath.setTargetDepth(2);

        // The roughing pass ends up above the material, but the target depth must still be cut
        List<Double> depths = getCuttingDepths(toolPath.toGcodePath(), 10);
        assertEquals(2, depths.size());
        assertEquals(-2d, depths.get(1), 0.001);
    }

    @Test
    public void toGcodePathShouldDefaultToLeavingTwoTenthsOfAMillimeter() {
        assertEquals(0.2, new Rectangle(0, 0).getStockToLeave(), 0.001);
    }

    private static Rectangle createSurfaceRectangle() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        return rectangle;
    }

    private static Settings createSurfaceSettings() {
        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);
        settings.setDepthPerPass(1);
        return settings;
    }

    /**
     * Returns the Z position of each depth pass in the tool path. Every line in a pass plunges to the
     * same depth, so the plunges are deduplicated, and the moves to the safe height are skipped.
     */
    private static List<Double> getCuttingDepths(GcodePath gcodePath, double safeHeight) {
        List<Segment> segments = gcodePath.getSegments();
        // The last segment is the final retract which is done to the absolute safe height
        return segments.subList(0, segments.size() - 1).stream()
                .map(segment -> segment.point)
                .filter(point -> point != null && point.hasZ() && !point.hasX() && !point.hasY())
                .map(PartialPosition::getZ)
                .filter(z -> Math.abs(z - safeHeight) > 0.001)
                .distinct()
                .toList();
    }

    private static void assertXYPoint(PartialPosition point, double expectedX, double expectedY) {
        assertEquals("Expected another X", expectedX, point.getX(), 0.01);
        assertEquals("Expected another Y", expectedY, point.getY(), 0.01);
    }

    @Test
    public void toGcodePathShouldAddSpindleSpeed() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setFeedRate(200);
        rectangle.setSpindleSpeed(90); // Sets the spindle speed in percent
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setSafeHeight(10);
        settings.setMaxSpindleSpeed(10000);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();

        // Start spindle
        Segment segment = segments.get(0);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(9000, segment.getSpindleSpeed(), 0.01);
        assertEquals(200, segment.getFeedSpeed(), 0.01);
    }

    @Test
    public void toGcodePathShouldStartFromTheTopWithConventionalMilling() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(250, 270));
        rectangle.setLeadInPercent(0);
        rectangle.setDirection(Direction.CONVENTIONAL);
        rectangle.setToolPathDirection(ToolPathDirection.HORIZONTAL);

        Settings settings = new Settings();
        settings.setToolDiameter(38);
        settings.setToolStepOver(100);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);

        GcodePath gcodePath = toolPath.toGcodePath();
        List<Segment> segments = gcodePath.getSegments();

        // 8 passes -> 4 segments each, plus SEAM and final safe-height move
        assertEquals(34, segments.size());

        // Conventional milling should start at the top-most usable pass.
        // Segment layout for the first pass:
        // 0 SEAM, 1 safe Z, 2 XY start, 3 Z plunge, 4 line end
        Segment firstPassStart = segments.get(2);
        assertEquals(SegmentType.MOVE, firstPassStart.type);
        assertXYPoint(firstPassStart.point, 19, 251);
        assertFalse(firstPassStart.point.hasZ());
    }
}