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
package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.ugs.nbp.designer.model.Size;
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
        rectangle.setLeadOutPercent(0);

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

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 7.5, 7.5);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldCoverTheWholeHeight() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        rectangle.setLeadOutPercent(0);

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

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldGenerateGcodeFromNegativeStartDepth() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        rectangle.setLeadOutPercent(0);

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

        // Move to safe height
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
    public void toGcodePathShouldGenerateGcodeFromStartDepthWithLeadIn() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(100);
        rectangle.setLeadOutPercent(0);

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
        assertXYPoint(point, 7.5, 2.5);

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
        assertXYPoint(segment.point, 7.5, 7.5);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldGenerateGcodeFromStartDepthWithLeadOut() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(0);
        rectangle.setLeadOutPercent(100);

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
        PartialPosition point = segment.point;
        assertXYPoint(point, 12.5, 2.5);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 9);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertXYPoint(segment.point, 2.5, 7.5);
        assertFalse(segment.point.hasZ());

        // Move towards material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, -1);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertXYPoint(segment.point, 12.5, 7.5);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    @Test
    public void toGcodePathShouldGenerateGcodeFromStartDepthWithLeadInAndOut() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setLeadInPercent(100);
        rectangle.setLeadOutPercent(100);

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

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertZPoint(segment.point, 10);
    }

    private static void assertXYPoint(PartialPosition point, double expectedX, double expectedY) {
        assertEquals(expectedX, point.getX(), 0.01);
        assertEquals(expectedY, point.getY(), 0.01);
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
}