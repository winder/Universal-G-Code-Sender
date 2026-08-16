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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.PlungeType;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;

public class OutlineToolPathTest {

    @Test
    public void toGcodePathShouldGenerateGcodeFromStartDepth() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setFeedRate(2000);
        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setPlungeSpeed(500);

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
        assertNull(segment.getFeedSpeed());

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);
        assertEquals(2000, segment.getFeedSpeed(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(10, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);
        assertEquals(2000, segment.getFeedSpeed(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);
        assertEquals(2000, segment.getFeedSpeed(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(0, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);
        assertEquals(2000, segment.getFeedSpeed(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        assertEquals(10, segments.size());
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
        assertEquals(0, segment.point.getZ(), 0.01);

        assertEquals(10, segments.size());
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

        assertEquals(10, segments.size());
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

        assertEquals(10, segments.size());
    }

    @Test
    public void toGcodePathShouldGenerateGcodeFromNegativeStartDepth() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPlungeType(PlungeType.STRAIGHT);
        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setDepthPerPass(1);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-10);
        toolPath.setTargetDepth(-9);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        assertEquals(18, segments.size());

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
        int segmentIndex = 0;

        // Start spindle
        Segment segment = segments.get(segmentIndex);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(10000, segment.getSpindleSpeed(), 0.01);

        assertEquals(10, segments.size());
    }

    @Test
    public void toGcodePath_shouldGenerateArcsForCircleWhenArcFittingIsEnabled() {
        Ellipse circle = new Ellipse(0, 0, 40, 40);
        Settings settings = new Settings();
        settings.setArcFitting(true);

        GcodePath gcodePath = outlineOf(circle, settings);

        List<Segment> arcs = gcodePath.getSegments().stream().filter(s -> s.getType().isArc()).toList();
        assertFalse(arcs.isEmpty());
        arcs.forEach(arc -> assertNotNull(arc.getArcCenter()));
    }

    @Test
    public void toGcodePath_shouldGenerateOnlyLinesForCircleWhenArcFittingIsDisabled() {
        Ellipse circle = new Ellipse(0, 0, 40, 40);
        Settings settings = new Settings();
        settings.setArcFitting(false);

        GcodePath gcodePath = outlineOf(circle, settings);

        assertTrue(gcodePath.getSegments().stream().noneMatch(s -> s.getType().isArc()));
    }

    @Test
    public void toGcodePath_shouldGenerateFewerSegmentsForCircleWhenArcFittingIsEnabled() {
        Ellipse circle = new Ellipse(0, 0, 40, 40);
        Settings fittedSettings = new Settings();
        fittedSettings.setArcFitting(true);
        fittedSettings.setFlatnessPrecision(0.2);

        GcodePath fitted = outlineOf(circle, fittedSettings);

        Settings unfittedSettings = new Settings();
        fittedSettings.setArcFitting(false);
        fittedSettings.setFlatnessPrecision(0.2);
        GcodePath outline = outlineOf(circle, unfittedSettings);
        assertTrue("Expected fitted size " + fitted.getSize() + " to be smaller " + outline.getSize(), fitted.getSize() < outline.getSize());
    }

    @Test
    public void toGcodePath_shouldKeepStraightEdgesAsLinesWhenArcFittingIsEnabled() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(30, 20));
        Settings settings = new Settings();
        settings.setArcFitting(true);

        GcodePath gcodePath = outlineOf(rectangle, settings);

        assertTrue(gcodePath.getSegments().stream().noneMatch(s -> s.getType().isArc()));
    }

    @Test
    public void toGcodePath_shouldRampIntoMaterialWhenPlungeTypeIsLinearRamp() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setFeedRate(2000);
        rectangle.setPlungeType(PlungeType.LINEAR_RAMP);
        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setDepthPerPass(1);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setTargetDepth(1);
        List<Segment> segments = toolPath.toGcodePath().getSegments();

        // The first pass cuts at the surface, so there is nothing to ramp into yet
        Segment segment = segments.get(4);
        assertEquals(SegmentType.POINT, segment.type);
        assertEquals(0, segment.point.getZ(), 0.01);
        assertSegment(segments.get(8), 0, 0, -0);

        // The pass after it descends to the full depth of cut along the first edge of the rectangle,
        // straight from where the previous one left the tool
        segment = segments.get(9);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(3.73, segment.point.getY(), 0.01);
        assertEquals(-1, segment.point.getZ(), 0.01);
        assertEquals(Integer.valueOf(2000), segment.getFeedSpeed());

        // Continue cutting the outline from where the ramp ended
        assertSegment(segments.get(10), 0, 10, -1);
        assertSegment(segments.get(11), 10, 10, -1);
        assertSegment(segments.get(12), 10, 0, -1);
        assertSegment(segments.get(13), 0, 0, -1);

        // And clear out the material that the ramp left behind
        assertSegment(segments.get(14), 0, 3.73, -1);

        // Retract to safe height
        assertEquals(SegmentType.MOVE, segments.get(15).type);
        assertEquals(10, segments.get(15).point.getZ(), 0.01);
        assertEquals(16, segments.size());
    }

    @Test
    public void toGcodePath_shouldNotPlungeToFullDepthWhenPlungeTypeIsLinearRamp() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPlungeType(PlungeType.LINEAR_RAMP);
        Settings settings = new Settings();
        settings.setDepthPerPass(1);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setTargetDepth(3);
        List<Segment> segments = toolPath.toGcodePath().getSegments();

        assertTrue("No pass should move straight down into the material",
                segments.stream()
                        .filter(s -> s.type == SegmentType.POINT)
                        .allMatch(s -> s.point.getZ() == 0));
    }

    @Test
    public void toGcodePath_shouldRampTowardsTheStartOfAnOpenPath() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(0, 10);
        path.setPlungeType(PlungeType.LINEAR_RAMP);
        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setDepthPerPass(1);

        OutlineToolPath toolPath = new OutlineToolPath(settings, path);
        toolPath.setTargetDepth(1);
        List<Segment> segments = toolPath.toGcodePath().getSegments();

        // An open path can not be extended to clear out the ramp, so the tool moves a bit into the
        // path and descends back towards its start
        Segment segment = segments.get(7);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.point.getX(), 0.01);
        assertEquals(3.73, segment.point.getY(), 0.01);

        // Descend back to the start of the path
        assertSegment(segments.get(10), 0, 0, -1);

        // The path is then cut in its entirety, clearing out the ramp as it goes
        assertSegment(segments.get(11), 0, 10, -1);
    }

    @Test
    public void toGcodePath_shouldNotRapidIntoMaterialWhenDepthPerPassIsDeeperThanSafeHeight() {
        Ellipse ellipse = new Ellipse(0, 0);
        ellipse.setSize(new Size(23, 19.8));
        Settings settings = new Settings();
        settings.setSafeHeight(5);
        settings.setDepthPerPass(8);

        OutlineToolPath toolPath = new OutlineToolPath(settings, ellipse);
        toolPath.setTargetDepth(8);
        List<Segment> segments = toolPath.toGcodePath().getSegments();

        assertTrue("A rapid must never move down into the material",
                segments.stream()
                        .filter(s -> s.type == SegmentType.MOVE && s.point.hasZ())
                        .allMatch(s -> s.point.getZ() >= 0));
    }

    @Test
    public void toGcodePath_shouldCarryOnFromWhereTheRampEndedWithoutRetracting() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPlungeType(PlungeType.LINEAR_RAMP);
        Settings settings = new Settings();
        settings.setSafeHeight(5);
        settings.setDepthPerPass(1);

        OutlineToolPath toolPath = new OutlineToolPath(settings, rectangle);
        toolPath.setTargetDepth(3);
        List<Segment> segments = toolPath.toGcodePath().getSegments();

        // Every pass ends where the next one starts descending, so the only rapids left are the ones
        // approaching the outline and the one retracting from it once it has been cut
        List<Segment> rapids = segments.stream().filter(s -> s.type == SegmentType.MOVE).toList();
        assertEquals(4, rapids.size());
        assertRetract(rapids.get(0), 5);
        assertRetract(rapids.get(2), 5);
        assertRetract(rapids.get(3), 5);
        assertEquals(segments.get(segments.size() - 1), rapids.get(3));

        // Each pass picks up a ramp length further along the outline than the previous one
        assertSegment(segments.get(9), 0, 3.73, -1);
        assertSegment(segments.get(15), 0, 7.46, -2);
    }

    @Test
    public void toGcodePath_shouldRetractToSafeHeightWhenMovingBetweenShapes() {
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
        path.setPlungeType(PlungeType.STRAIGHT);

        Settings settings = new Settings();
        settings.setSafeHeight(5);
        settings.setDepthPerPass(1);

        OutlineToolPath toolPath = new OutlineToolPath(settings, path);
        toolPath.setTargetDepth(2);
        List<Segment> segments = toolPath.toGcodePath().getSegments();

        // Every pass of the first shape starts where the previous one ended
        assertRetract(segments.get(9), 4);
        assertRetract(segments.get(17), 3);

        // Moving on to the second shape takes the tool over material that has not been cut away
        assertRetract(segments.get(25), 5);
        assertEquals(SegmentType.MOVE, segments.get(26).type);
        assertEquals(50, segments.get(26).point.getX(), 0.01);
        assertEquals(0, segments.get(26).point.getY(), 0.01);
    }

    private static void assertRetract(Segment segment, double z) {
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertEquals(z, segment.point.getZ(), 0.01);
    }

    private static void assertSegment(Segment segment, double x, double y, double z) {
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(x, segment.point.getX(), 0.01);
        assertEquals(y, segment.point.getY(), 0.01);
        assertEquals(z, segment.point.getZ(), 0.01);
    }

    private static GcodePath outlineOf(Cuttable source, Settings settings) {
        OutlineToolPath toolPath = new OutlineToolPath(settings, source);
        toolPath.setTargetDepth(1);
        return toolPath.toGcodePath();
    }
}