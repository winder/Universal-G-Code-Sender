package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import org.junit.Test;

import java.util.List;

import static com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType.LINE;
import static com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType.MOVE;
import static com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType.SEAM;
import static org.junit.Assert.assertEquals;

public class LaserFillToolPathTest {

    @Test
    public void fillToolPathShouldNotConnectThePointEdgesWithALineSegment() {
        Path path = new Path();
        path.setPasses(1);
        path.moveTo(0,0);
        path.lineTo(0, 1);
        path.lineTo(1, 1);
        path.lineTo(1, 0);
        path.lineTo(0.5, 0.5);
        path.lineTo(0,0);
        path.close();

        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(10000);

        LaserFillToolPath toolPath = new LaserFillToolPath(settings, path);
        toolPath.setStartDepth(0);
        toolPath.setTargetDepth(0);

        GcodePath gcodePath = toolPath.toGcodePath();
        List<Segment> segments = gcodePath.getSegments();
        assertEquals(SEAM, segments.get(0).type);
        assertSegment(segments.get(1), MOVE, 1, 0.2);
        assertSegment(segments.get(2), LINE, 0.8, 0.2);
        assertSegment(segments.get(3), MOVE, 0.2, 0.2);
        assertSegment(segments.get(4), LINE, 0.0, 0.2);
    }

    private void assertSegment(Segment segment, SegmentType segmentType, double x, double y) {
        assertEquals(segmentType, segment.getType());
        assertEquals(x, segment.getPoint().getX(), 0.01);
        assertEquals(y, segment.getPoint().getY(), 0.01);
    }
}
