package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.List;

public class LaserOutlinePathTest {
    @Test
    public void outlineToolPathWithNoPassesShouldNotGenerateTheShape() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setFeedRate(100);
        rectangle.setSpindleSpeed(100);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPasses(0);

        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(10000);

        LaserOutlineToolPath toolPath = new LaserOutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();

        Segment segment = segments.getFirst();
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertNull(segment.getSpindleSpeed());

        assertEquals(1, segments.size());
    }

    @Test
    public void outlineToolPathWithOnePass() {
        Rectangle rectangle = new Rectangle(0,0);
        rectangle.setFeedRate(500);
        rectangle.setSpindleSpeed(50);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPasses(1);

        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(10000);

        LaserOutlineToolPath toolPath = new LaserOutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;

        // The seam only sets the feed rate, the laser is not fired until the first cutting line
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertNull(segment.getSpindleSpeed());
        assertEquals(500, segment.getFeedSpeed(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.getPoint().getX(), 0.01);
        assertEquals(0, segment.getPoint().getY(), 0.01);
        assertEquals(Segment.SPINDLE_OFF, segment.getSpindleSpeed(), 0.01);

        // The rapid already landed on the first coordinate, so the first cut goes straight to the second
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.getPoint().getX(), 0.01);
        assertEquals(10, segment.getPoint().getY(), 0.01);
        assertEquals(5000, segment.getSpindleSpeed(), 0.01);
        assertEquals(500, segment.getFeedSpeed(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.getPoint().getX(), 0.01);
        assertEquals(10, segment.getPoint().getY(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(10, segment.getPoint().getX(), 0.01);
        assertEquals(0, segment.getPoint().getY(), 0.01);

        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.getPoint().getX(), 0.01);
        assertEquals(0, segment.getPoint().getY(), 0.01);

        assertEquals(6, segments.size());
    }

    @Test
    public void outlineToolPathWithTabsShouldLeaveGapsInTheCut() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setFeedRate(500);
        rectangle.setSpindleSpeed(50);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPasses(1);
        rectangle.setTabs(true);
        rectangle.setTabCount(4);

        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(10000);
        settings.setTabLength(4);

        LaserOutlineToolPath toolPath = new LaserOutlineToolPath(settings, rectangle);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();

        // The cut is broken into the five stretches between the four tabs, each reached with a rapid
        // that leaves the laser off while it crosses a tab
        long rapids = segments.stream().filter(s -> s.type == SegmentType.MOVE).count();
        assertEquals(5, rapids);
    }

    @Test
    public void appendGcodePath_shouldTurnTheLaserOffOnEveryRapidAndOnAgainOnEveryCut() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setFeedRate(500);
        rectangle.setSpindleSpeed(50);
        rectangle.setSize(new Size(10, 10));
        rectangle.setPasses(2);
        rectangle.setTabs(true);
        rectangle.setTabCount(4);

        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(10000);
        settings.setTabLength(4);

        LaserOutlineToolPath toolPath = new LaserOutlineToolPath(settings, rectangle);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        segments.stream().filter(s -> s.type == SegmentType.MOVE)
                .forEach(s -> assertEquals(Segment.SPINDLE_OFF, s.getSpindleSpeed(), 0.01));
        segments.stream().filter(s -> s.type == SegmentType.LINE)
                .forEach(s -> assertEquals(5000, s.getSpindleSpeed(), 0.01));
    }
}
