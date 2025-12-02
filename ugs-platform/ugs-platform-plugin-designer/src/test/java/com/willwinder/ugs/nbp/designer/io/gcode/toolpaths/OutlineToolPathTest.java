/*
    Copyright 2024 Will Winder

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
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;

public class OutlineToolPathTest {

    @Test
    public void toGcodePathShouldGenerateGcodeFromStartDepth() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(10);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 1;

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Move into material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertTrue(segment.point.hasX());
        assertTrue(segment.point.hasY());
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        assertEquals(11, segments.size());
    }

    @Test
    public void toGcodePathShouldGenerateGcodeWithSafeHeightZero() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(0);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 1;

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(0, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(0, segment.point.getZ(), 0.01);

        // Move into material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertTrue(segment.point.hasX());
        assertTrue(segment.point.hasY());
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(0, segment.point.getZ(), 0.01);

        assertEquals(11, segments.size());
    }


    @Test
    public void toGcodePathShouldGenerateGcodeWithSafeHeightForNegativeStartDepthAndSafeHeightZero() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(0);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-15);
        toolPath.setTargetDepth(-15);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 1;

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        // Move into material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertTrue(segment.point.hasX());
        assertTrue(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        assertEquals(11, segments.size());
    }

    @Test
    public void toGcodePathShouldGenerateGcodeWithSafeHeightForNegativeStartDepthAndSafeHeight() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(10);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-15);
        toolPath.setTargetDepth(-15);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;

        // Start spindle
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to safe height
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(25, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(25, segment.point.getZ(), 0.01);

        // Move into material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertTrue(segment.point.hasX());
        assertTrue(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(15, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(25, segment.point.getZ(), 0.01);

        assertEquals(11, segments.size());
    }

    @Test
    public void toGcodePathShouldGenerateGcodeFromNegativeStartDepth() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setDepthPerPass(1);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-10);
        toolPath.setTargetDepth(-9);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        assertEquals(20, segments.size());

        int segmentIndex = 0;

        // Start spindle
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // Move to safe height
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(20, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(20, segment.point.getZ(), 0.01);

        // Move into material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertTrue(segment.point.hasX());
        assertTrue(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(10, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(10, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(10, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(10, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(10, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(19, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Move into material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertTrue(segment.point.hasX());
        assertTrue(segment.point.hasY());
        assertEquals(9, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(9, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(9, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(9, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(9, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(9, segment.point.getZ(), 0.01);

        // Move to Z zero
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(20, segment.point.getZ(), 0.01);
    }

    @Test
    public void toGcodePathShouldAddFeedRate() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setFeedRate(100);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(10);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(1);
        toolPath.setTargetDepth(1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 1;

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Move into material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertTrue(segment.point.hasX());
        assertTrue(segment.point.hasY());
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);
        assertEquals(100, segment.getFeedSpeed(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        assertEquals(11, segments.size());
    }

    @Test
    public void toGcodePathShouldAddSpindleSpeed() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setFeedRate(100);
        rectangle.setSpindleSpeed(100);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setMaxSpindleSpeed(10000);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;

        // Start spindle
        Segment segment = segments.get(segmentIndex);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(10000, segment.getSpindleSpeed(), 0.01);

        assertEquals(11, segments.size());
    }
}