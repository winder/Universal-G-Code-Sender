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
        Settings settings = new Settings();
        settings.setToolDiameter(5);
        settings.setToolStepOver(1);
        settings.setSafeHeight(10);

        SurfaceToolPath toolPath = new SurfaceToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();

        assertEquals(12, segments.size());


        // Move to safe height
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(Integer.valueOf(0), segments.get(0).getFeedSpeed());
        assertEquals(Integer.valueOf(0), segments.get(0).getSpindleSpeed());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertFalse(segments.get(1).point.hasX());
        assertFalse(segments.get(1).point.hasY());
        assertEquals(10, segments.get(1).point.getZ(), 0.01);

        // Move to XY start
        assertEquals(SegmentType.MOVE, segments.get(2).type);
        assertEquals(2.5, segments.get(2).point.getX(), 0.01);
        assertEquals(2.5, segments.get(2).point.getY(), 0.01);
        assertFalse(segments.get(2).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(3).type);
        assertFalse(segments.get(3).point.hasX());
        assertFalse(segments.get(3).point.hasY());
        assertEquals(0, segments.get(3).point.getZ(), 0.01);

        // Move into material
        assertEquals(SegmentType.POINT, segments.get(4).type);
        assertFalse(segments.get(4).point.hasX());
        assertFalse(segments.get(4).point.hasY());
        assertEquals(-1, segments.get(4).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(5).type);
        assertEquals(7.5, segments.get(5).point.getX(), 0.01);
        assertEquals(2.5, segments.get(5).point.getY(), 0.01);

        assertEquals(SegmentType.MOVE, segments.get(6).type);
        assertFalse(segments.get(6).point.hasX());
        assertFalse(segments.get(6).point.hasY());
        assertEquals(10, segments.get(6).point.getZ(), 0.01);

        assertEquals(SegmentType.MOVE, segments.get(7).type);
        assertEquals(2.5, segments.get(7).point.getX(), 0.01);
        assertEquals(7.5, segments.get(7).point.getY(), 0.01);
        assertFalse(segments.get(7).point.hasZ());

        // Move towards material
        assertEquals(SegmentType.MOVE, segments.get(8).type);
        assertFalse(segments.get(8).point.hasX());
        assertFalse(segments.get(8).point.hasY());
        assertEquals(0, segments.get(8).point.getZ(), 0.01);

        // Move into material
        assertEquals(SegmentType.POINT, segments.get(9).type);
        assertFalse(segments.get(9).point.hasX());
        assertFalse(segments.get(9).point.hasY());
        assertEquals(-1, segments.get(9).point.getZ(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(10).type);
        assertEquals(7.5, segments.get(10).point.getX(), 0.01);
        assertEquals(7.5, segments.get(10).point.getY(), 0.01);

        // Move to safe height
        assertEquals(SegmentType.MOVE, segments.get(11).type);
        assertFalse(segments.get(11).point.hasX());
        assertFalse(segments.get(11).point.hasY());
        assertEquals(10, segments.get(11).point.getZ(), 0.01);
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