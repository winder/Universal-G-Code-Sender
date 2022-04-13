package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;

public class DrillCenterToolPathTest {
    @Test
    public void drillCenterShouldDrillInCenterOfShape() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(15, 15));
        rectangle.setPosition(new Point2D.Double(10, 10));

        DrillCenterToolPath drillCenterToolPath = new DrillCenterToolPath(rectangle);
        drillCenterToolPath.setDepthPerPass(10);
        drillCenterToolPath.setTargetDepth(10);
        GcodePath gcodePath = drillCenterToolPath.toGcodePath();

        assertEquals(8, gcodePath.getSegments().size());

        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(0).type);
        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(1).type);
        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(2).type);
        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(3).type);
        assertEquals(0, gcodePath.getSegments().get(3).point.getZ(), 0.01);

        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(4).type);
        assertEquals(17.5, gcodePath.getSegments().get(4).point.getX(), 0.01);
        assertEquals(17.5, gcodePath.getSegments().get(4).point.getY(), 0.01);
        assertEquals(0, gcodePath.getSegments().get(4).point.getZ(), 0.01);

        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(5).type);
        assertEquals(-10, gcodePath.getSegments().get(5).point.getZ(), 0.01);

        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(6).type);
        assertEquals(0, gcodePath.getSegments().get(6).point.getZ(), 0.01);

        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(7).type);
    }
}
