package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DrillCenterToolPathTest {
    @Test
    public void drillCenterShouldDrillInCenterOfShape() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(15, 15));
        rectangle.setPosition(new Point2D.Double(10, 10));

        DrillCenterToolPath drillCenterToolPath = new DrillCenterToolPath(rectangle);
        drillCenterToolPath.setDepthPerPass(5);
        drillCenterToolPath.setTargetDepth(10);
        drillCenterToolPath.setSafeHeight(11);
        GcodePath gcodePath = drillCenterToolPath.toGcodePath();

        assertEquals(9, gcodePath.getSegments().size());

        // Move to safe height
        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(0).type);
        assertFalse(gcodePath.getSegments().get(0).point.hasX());
        assertFalse(gcodePath.getSegments().get(0).point.hasY());
        assertEquals(11, gcodePath.getSegments().get(0).point.getZ(), 0.01);

        // Move in XY-place
        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(1).type);
        assertEquals(17.5, gcodePath.getSegments().get(1).point.getX(), 0.01);
        assertEquals(17.5, gcodePath.getSegments().get(1).point.getY(), 0.01);
        assertFalse(gcodePath.getSegments().get(1).point.hasZ());

        // Move to Z zero
        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(2).type);
        assertFalse(gcodePath.getSegments().get(2).point.hasX());
        assertFalse(gcodePath.getSegments().get(2).point.hasY());
        assertEquals(0, gcodePath.getSegments().get(2).point.getZ(), 0.01);

        // First depth pass
        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(3).type);
        assertFalse(gcodePath.getSegments().get(3).point.hasX());
        assertFalse(gcodePath.getSegments().get(3).point.hasY());
        assertEquals(0, gcodePath.getSegments().get(3).point.getZ(), 0.01);

        // Second depth pass
        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(4).type);
        assertFalse(gcodePath.getSegments().get(4).point.hasX());
        assertFalse(gcodePath.getSegments().get(4).point.hasY());
        assertEquals(-5, gcodePath.getSegments().get(4).point.getZ(), 0.01);

        // Clear material
        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(5).type);
        assertFalse(gcodePath.getSegments().get(5).point.hasX());
        assertFalse(gcodePath.getSegments().get(5).point.hasY());
        assertEquals(-0, gcodePath.getSegments().get(5).point.getZ(), 0.01);

        // Third depth pass
        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(6).type);
        assertFalse(gcodePath.getSegments().get(6).point.hasX());
        assertFalse(gcodePath.getSegments().get(6).point.hasY());
        assertEquals(-10, gcodePath.getSegments().get(6).point.getZ(), 0.01);

        // Clear material
        assertEquals(SegmentType.POINT, gcodePath.getSegments().get(7).type);
        assertFalse(gcodePath.getSegments().get(7).point.hasX());
        assertFalse(gcodePath.getSegments().get(7).point.hasY());
        assertEquals(0, gcodePath.getSegments().get(7).point.getZ(), 0.01);

        // Move to safe height
        assertEquals(SegmentType.MOVE, gcodePath.getSegments().get(8).type);
        assertFalse(gcodePath.getSegments().get(8).point.hasX());
        assertFalse(gcodePath.getSegments().get(8).point.hasY());
        assertEquals(11, gcodePath.getSegments().get(8).point.getZ(), 0.01);
    }
}
