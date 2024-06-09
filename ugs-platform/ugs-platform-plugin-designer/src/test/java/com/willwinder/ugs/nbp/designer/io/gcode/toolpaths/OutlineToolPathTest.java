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
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(0).type);
        assertFalse(segments.get(0).point.hasX());
        assertFalse(segments.get(0).point.hasY());
        assertEquals(10, segments.get(0).point.getZ(), 0.01);

        // Move in XY-place
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertEquals(0, segments.get(1).point.getX(), 0.01);
        assertEquals(0, segments.get(1).point.getY(), 0.01);
        assertFalse(segments.get(1).point.hasZ());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertFalse(segments.get(2).point.hasX());
        assertFalse(segments.get(2).point.hasY());
        assertEquals(0, segments.get(2).point.getZ(), 0.01);

        // Move into material
        assertEquals(SegmentType.POINT, segments.get(3).type);
        assertTrue(segments.get(3).point.hasX());
        assertTrue(segments.get(3).point.hasY());
        assertEquals(-1, segments.get(3).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        assertEquals(0, segments.get(4).point.getX(), 0.01);
        assertEquals(0, segments.get(4).point.getY(), 0.01);
        assertEquals(-1, segments.get(4).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(5).type);
        assertEquals(0, segments.get(5).point.getX(), 0.01);
        assertEquals(10, segments.get(5).point.getY(), 0.01);
        assertEquals(-1, segments.get(5).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(7).type);
        assertEquals(10, segments.get(7).point.getX(), 0.01);
        assertEquals(0, segments.get(7).point.getY(), 0.01);
        assertEquals(-1, segments.get(7).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertEquals(0, segments.get(8).point.getX(), 0.01);
        assertEquals(0, segments.get(8).point.getY(), 0.01);
        assertEquals(-1, segments.get(8).point.getZ(), 0.01);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertFalse(segments.get(9).point.hasX());
        assertFalse(segments.get(9).point.hasY());
        assertEquals(10, segments.get(9).point.getZ(), 0.01);

        assertEquals(10, segments.size());
    }

    @Test
    public void toGcodePathShouldAddFeedRate() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setFeedRate(100);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setSafeHeight(10);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(0).type);
        assertFalse(segments.get(0).point.hasX());
        assertFalse(segments.get(0).point.hasY());
        assertEquals(10, segments.get(0).point.getZ(), 0.01);

        // Move in XY-place
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertEquals(0, segments.get(1).point.getX(), 0.01);
        assertEquals(0, segments.get(1).point.getY(), 0.01);
        assertFalse(segments.get(1).point.hasZ());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertFalse(segments.get(2).point.hasX());
        assertFalse(segments.get(2).point.hasY());
        assertEquals(0, segments.get(2).point.getZ(), 0.01);

        // Move into material
        assertEquals(SegmentType.POINT, segments.get(3).type);
        assertTrue(segments.get(3).point.hasX());
        assertTrue(segments.get(3).point.hasY());
        assertEquals(-1, segments.get(3).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        assertEquals(0, segments.get(4).point.getX(), 0.01);
        assertEquals(0, segments.get(4).point.getY(), 0.01);
        assertEquals(-1, segments.get(4).point.getZ(), 0.01);
        assertEquals(100, segments.get(4).getFeedSpeed(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(5).type);
        assertEquals(0, segments.get(5).point.getX(), 0.01);
        assertEquals(10, segments.get(5).point.getY(), 0.01);
        assertEquals(-1, segments.get(5).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(7).type);
        assertEquals(10, segments.get(7).point.getX(), 0.01);
        assertEquals(0, segments.get(7).point.getY(), 0.01);
        assertEquals(-1, segments.get(7).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(8).type);
        assertEquals(0, segments.get(8).point.getX(), 0.01);
        assertEquals(0, segments.get(8).point.getY(), 0.01);
        assertEquals(-1, segments.get(8).point.getZ(), 0.01);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(9).type);
        assertFalse(segments.get(9).point.hasX());
        assertFalse(segments.get(9).point.hasY());
        assertEquals(10, segments.get(9).point.getZ(), 0.01);

        assertEquals(10, segments.size());
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

        // Start spindle
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(10000, segments.get(0).getSpindleSpeed(), 0.01);

        assertEquals(11, segments.size());
    }
}