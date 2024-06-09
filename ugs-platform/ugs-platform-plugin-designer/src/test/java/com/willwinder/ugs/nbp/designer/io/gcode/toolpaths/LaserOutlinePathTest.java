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

        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(10000);

        LaserOutlineToolPath toolPath = new LaserOutlineToolPath(settings, rectangle);
        toolPath.setStartDepth(-1);
        toolPath.setTargetDepth(-1);
        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();

        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(10000, segments.get(0).getSpindleSpeed(), 0.01);

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

        // Start spindle
        assertEquals(SegmentType.SEAM, segments.get(0).type);
        assertNull(segments.get(0).point);
        assertEquals(5000, segments.get(0).getSpindleSpeed(), 0.01);
        assertEquals(500, segments.get(0).getFeedSpeed(), 0.01);

        assertEquals(SegmentType.MOVE, segments.get(1).type);
        assertEquals(0, segments.get(1).getPoint().getX(), 0.01);
        assertEquals(0, segments.get(1).getPoint().getY(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(2).type);
        assertEquals(0, segments.get(2).getPoint().getX(), 0.01);
        assertEquals(0, segments.get(2).getPoint().getY(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(3).type);
        assertEquals(0, segments.get(3).getPoint().getX(), 0.01);
        assertEquals(10, segments.get(3).getPoint().getY(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(4).type);
        assertEquals(10, segments.get(4).getPoint().getX(), 0.01);
        assertEquals(10, segments.get(4).getPoint().getY(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(5).type);
        assertEquals(10, segments.get(5).getPoint().getX(), 0.01);
        assertEquals(0, segments.get(5).getPoint().getY(), 0.01);

        assertEquals(SegmentType.LINE, segments.get(6).type);
        assertEquals(0, segments.get(6).getPoint().getX(), 0.01);
        assertEquals(0, segments.get(6).getPoint().getY(), 0.01);

        assertEquals(7, segments.size());
    }
}
