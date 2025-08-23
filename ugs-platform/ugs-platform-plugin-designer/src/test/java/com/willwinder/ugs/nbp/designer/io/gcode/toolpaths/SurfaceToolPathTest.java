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

        assertEquals(10, segments.size());


        // Move to safe height
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(Integer.valueOf(0), segments.get(0).getFeedSpeed());
        assertEquals(Integer.valueOf(0), segments.get(0).getSpindleSpeed());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertZPoint(segments.get(1).point, 9);

        // Move to XY start
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertXYPoint(segments.get(2).point, 2.5, 2.5);
        assertFalse(segments.get(2).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(3).type);
        assertZPoint(segments.get(3).point, -1);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        assertXYPoint(segments.get(4).point, 7.5, 2.5);

        assertEquals(SegmentType.MOVE, segments.get(5).type);
        assertZPoint(segments.get(5).point, 9);

        assertEquals(SegmentType.MOVE, segments.get(6).type);
        assertXYPoint(segments.get(6).point, 2.5, 7.5);
        assertFalse(segments.get(6).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(7).type);
        assertZPoint(segments.get(7).point, -1);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertXYPoint(segments.get(8).point, 7.5, 7.5);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertZPoint(segments.get(9).point, 10);
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

        assertEquals(10, segments.size());


        // Move to safe height
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(Integer.valueOf(0), segments.get(0).getFeedSpeed());
        assertEquals(Integer.valueOf(0), segments.get(0).getSpindleSpeed());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertZPoint(segments.get(1).point, 9);

        // Move to XY start
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertXYPoint(segments.get(2).point, 4, 4);
        assertFalse(segments.get(2).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(3).type);
        assertZPoint(segments.get(3).point, -1);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        assertXYPoint(segments.get(4).point, 6, 4);

        assertEquals(SegmentType.MOVE, segments.get(5).type);
        assertZPoint(segments.get(5).point, 9);

        assertEquals(SegmentType.MOVE, segments.get(6).type);
        assertXYPoint(segments.get(6).point, 4, 6);
        assertFalse(segments.get(6).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(7).type);
        assertZPoint(segments.get(7).point, -1);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertXYPoint(segments.get(8).point, 6, 6);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertZPoint(segments.get(9).point, 10);
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

        assertEquals(10, segments.size());


        // Move to safe height
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(Integer.valueOf(0), segments.get(0).getFeedSpeed());
        assertEquals(Integer.valueOf(0), segments.get(0).getSpindleSpeed());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertZPoint(segments.get(1).point, 11);

        // Move to XY start
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertXYPoint(segments.get(2).point, 2.5, 2.5);
        assertFalse(segments.get(2).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(3).type);
        assertZPoint(segments.get(3).point, 1);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        assertXYPoint(segments.get(4).point, 7.5, 2.5);

        assertEquals(SegmentType.MOVE, segments.get(5).type);
        assertZPoint(segments.get(5).point, 11);

        assertEquals(SegmentType.MOVE, segments.get(6).type);
        assertXYPoint(segments.get(6).point, 2.5, 7.5);
        assertFalse(segments.get(6).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(7).type);
        assertZPoint(segments.get(7).point, 1);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertXYPoint(segments.get(8).point, 7.5, 7.5);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertZPoint(segments.get(9).point, 11);
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

        assertEquals(10, segments.size());

        // Move to safe height
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(Integer.valueOf(0), segments.get(0).getFeedSpeed());
        assertEquals(Integer.valueOf(0), segments.get(0).getSpindleSpeed());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertZPoint(segments.get(1).point, 9);

        // Move to XY start
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertXYPoint(segments.get(2).point, -2.5, 2.5);
        assertFalse(segments.get(2).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(3).type);
        assertZPoint(segments.get(3).point, -1);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        PartialPosition point = segments.get(4).point;
        assertXYPoint(point, 7.5, 2.5);

        assertEquals(SegmentType.MOVE, segments.get(5).type);
        assertZPoint(segments.get(5).point, 9);

        assertEquals(SegmentType.MOVE, segments.get(6).type);
        assertXYPoint(segments.get(6).point, -2.5, 7.5);
        assertFalse(segments.get(6).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(7).type);
        assertZPoint(segments.get(7).point, -1);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertXYPoint(segments.get(8).point, 7.5, 7.5);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertZPoint(segments.get(9).point, 10);
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

        assertEquals(10, segments.size());

        // Move to safe height
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(Integer.valueOf(0), segments.get(0).getFeedSpeed());
        assertEquals(Integer.valueOf(0), segments.get(0).getSpindleSpeed());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertZPoint(segments.get(1).point, 9);

        // Move to XY start
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertXYPoint(segments.get(2).point, 2.5, 2.5);
        assertFalse(segments.get(2).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(3).type);
        assertZPoint(segments.get(3).point, -1);


        assertEquals(SegmentType.LINE, segments.get(4).type);
        PartialPosition point = segments.get(4).point;
        assertXYPoint(point, 12.5, 2.5);

        assertEquals(SegmentType.MOVE, segments.get(5).type);
        assertZPoint(segments.get(5).point, 9);

        assertEquals(SegmentType.MOVE, segments.get(6).type);
        assertXYPoint(segments.get(6).point, 2.5, 7.5);
        assertFalse(segments.get(6).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(7).type);
        assertZPoint(segments.get(7).point, -1);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertXYPoint(segments.get(8).point, 12.5, 7.5);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertZPoint(segments.get(9).point, 10);
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

        assertEquals(10, segments.size());

        // Move to safe height
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(Integer.valueOf(0), segments.get(0).getFeedSpeed());
        assertEquals(Integer.valueOf(0), segments.get(0).getSpindleSpeed());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertZPoint(segments.get(1).point, 9);

        // Move to XY start
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertXYPoint(segments.get(2).point, -2.5, 2.5);
        assertFalse(segments.get(2).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(3).type);
        assertZPoint(segments.get(3).point, -1);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        PartialPosition point = segments.get(4).point;
        assertXYPoint(point, 12.5, 2.5);

        assertEquals(SegmentType.MOVE, segments.get(5).type);
        assertZPoint(segments.get(5).point, 9);

        assertEquals(SegmentType.MOVE, segments.get(6).type);
        assertXYPoint(segments.get(6).point, -2.5, 7.5);
        assertFalse(segments.get(6).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(7).type);
        assertZPoint(segments.get(7).point, -1);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertXYPoint(segments.get(8).point, 12.5, 7.5);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertZPoint(segments.get(9).point, 10);
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
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(9000, segments.get(0).getSpindleSpeed(), 0.01);
        assertEquals(200, segments.get(0).getFeedSpeed(), 0.01);
    }
}