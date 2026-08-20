/*
    Copyright 2026 Joacim Breiler

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

import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.List;

public class PlotterToolPathTest {

    @Test
    public void appendGcodePath_shouldLiftThePenBeforeMovingAndLowerItBeforeDrawing() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setFeedRate(1500);
        Settings settings = new Settings();
        settings.setArcFitting(false);
        PlotterToolPath toolPath = new PlotterToolPath(settings, rectangle);

        List<Segment> segments = toolPath.toGcodePath().getSegments();

        assertEquals(SegmentType.PEN_UP, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertEquals(0, segments.get(1).point.getX(), 0.01);
        assertEquals(0, segments.get(1).point.getY(), 0.01);
        assertEquals(SegmentType.PEN_DOWN, segments.get(2).type);
        assertEquals(SegmentType.LINE, segments.get(3).type);
        assertEquals(1500, segments.get(3).getFeedSpeed(), 0.01);
        assertEquals(SegmentType.PEN_UP, segments.get(segments.size() - 1).type);
    }

    @Test
    public void appendGcodePath_shouldNotGenerateAnyDepths() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        Settings settings = new Settings();
        settings.setArcFitting(false);
        PlotterToolPath toolPath = new PlotterToolPath(settings, rectangle);

        List<Segment> segments = toolPath.toGcodePath().getSegments();

        assertTrue(segments.stream().filter(segment -> segment.point != null).noneMatch(segment -> segment.point.hasZ()));
        assertFalse(segments.isEmpty());
    }

    @Test
    public void appendGcodePath_shouldDrawTheShapeOnceRegardlessOfThePassesOfTheShape() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPasses(3);
        Settings settings = new Settings();
        settings.setArcFitting(false);
        PlotterToolPath toolPath = new PlotterToolPath(settings, rectangle);

        List<Segment> segments = toolPath.toGcodePath().getSegments();

        assertEquals(1, segments.stream().filter(segment -> segment.type == SegmentType.PEN_DOWN).count());
        assertEquals(2, segments.stream().filter(segment -> segment.type == SegmentType.PEN_UP).count());
    }
}
