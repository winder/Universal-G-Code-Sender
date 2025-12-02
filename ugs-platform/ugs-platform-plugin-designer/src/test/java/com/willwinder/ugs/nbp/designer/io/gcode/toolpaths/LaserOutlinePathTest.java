package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.ugs.nbp.designer.model.Size;
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

        Segment segment = segments.get(0);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(10000, segment.getSpindleSpeed(), 0.01);

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

        // Start spindle
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(5000, segment.getSpindleSpeed(), 0.01);
        assertEquals(500, segment.getFeedSpeed(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(0, segment.getPoint().getX(), 0.01);
        assertEquals(0, segment.getPoint().getY(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.getPoint().getX(), 0.01);
        assertEquals(0, segment.getPoint().getY(), 0.01);

        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.LINE, segment.type);
        assertEquals(0, segment.getPoint().getX(), 0.01);
        assertEquals(10, segment.getPoint().getY(), 0.01);

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

        assertEquals(7, segments.size());
    }
}
