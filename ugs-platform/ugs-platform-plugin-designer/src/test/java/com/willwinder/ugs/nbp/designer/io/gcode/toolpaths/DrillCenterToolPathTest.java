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

import java.awt.geom.Point2D;
import java.util.List;

public class DrillCenterToolPathTest {
    @Test
    public void drillCenterShouldDrillInCenterOfShape() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(15, 15));
        rectangle.setPosition(new Point2D.Double(10, 10));

        Settings settings = new Settings();
        settings.setSafeHeight(11);
        settings.setDepthPerPass(5);

        DrillCenterToolPath drillCenterToolPath = new DrillCenterToolPath(settings, rectangle);
        drillCenterToolPath.setTargetDepth(10);
        GcodePath gcodePath = drillCenterToolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(11, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(17.5, segment.point.getX(), 0.01);
        assertEquals(17.5, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(11, segment.point.getZ(), 0.01);

        // Start spindle
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // First depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(0, segment.point.getZ(), 0.01);

        // Second depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(-5, segment.point.getZ(), 0.01);

        // Clear material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(-0, segment.point.getZ(), 0.01);

        // Third depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(-10, segment.point.getZ(), 0.01);

        // Clear material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(0, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(11, segment.point.getZ(), 0.01);
    }

    @Test
    public void drillCenterWithNegativeStartAndTargetDepth() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(15, 15));
        rectangle.setPosition(new Point2D.Double(10, 10));

        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setDepthPerPass(5);

        DrillCenterToolPath drillCenterToolPath = new DrillCenterToolPath(settings, rectangle);
        drillCenterToolPath.setStartDepth(-15);
        drillCenterToolPath.setTargetDepth(-5);
        GcodePath gcodePath = drillCenterToolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(10, gcodePath.getSegments().size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(25, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(17.5, segment.point.getX(), 0.01);
        assertEquals(17.5, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(25, segment.point.getZ(), 0.01);

        // Start spindle
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // First depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        // Second depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Clear material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        // Third depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(5, segment.point.getZ(), 0.01);

        // Clear material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(15, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(25, segment.point.getZ(), 0.01);
    }

    @Test
    public void drillCenterShouldMoveToSafeHeight() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(15, 15));
        rectangle.setPosition(new Point2D.Double(10, 10));

        Settings settings = new Settings();
        settings.setSafeHeight(10);
        settings.setDepthPerPass(10);

        DrillCenterToolPath drillCenterToolPath = new DrillCenterToolPath(settings, rectangle);
        drillCenterToolPath.setStartDepth(5);
        drillCenterToolPath.setTargetDepth(10);
        GcodePath gcodePath = drillCenterToolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;
        assertEquals(8, segments.size());

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(17.5, segment.point.getX(), 0.01);
        assertEquals(17.5, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to safe height
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);

        // Start spindle
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(Integer.valueOf(255), segment.getSpindleSpeed());

        // First depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(-5, segment.point.getZ(), 0.01);

        // Second depth pass
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(-10, segment.point.getZ(), 0.01);

        // Clear material
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(-5, segment.point.getZ(), 0.01);

        // Move to safe height
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(10, segment.point.getZ(), 0.01);
    }

    @Test
    public void drillCenterWithSpindleSpeedShouldTurnOnSpindle() {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(15, 15));
        rectangle.setPosition(new Point2D.Double(10, 10));
        rectangle.setSpindleSpeed(100);

        Settings settings = new Settings();
        settings.setSafeHeight(11);
        settings.setDepthPerPass(10);
        settings.setMaxSpindleSpeed(1000);

        DrillCenterToolPath drillCenterToolPath = new DrillCenterToolPath(settings, rectangle);
        drillCenterToolPath.setTargetDepth(10);
        GcodePath gcodePath = drillCenterToolPath.toGcodePath();

        List<Segment> segments = gcodePath.getSegments();
        int segmentIndex = 0;

        // Move to safe height
        Segment segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(11, segment.point.getZ(), 0.01);

        // Move in XY-place
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertEquals(17.5, segment.point.getX(), 0.01);
        assertEquals(17.5, segment.point.getY(), 0.01);
        assertFalse(segment.point.hasZ());

        // Move to Z zero
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.MOVE, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(11, segment.point.getZ(), 0.01);

        // Turn on spindle
        segment = segments.get(segmentIndex++);
        assertEquals(SegmentType.SEAM, segment.type);
        assertNull(segment.point);
        assertEquals(1000, segment.getSpindleSpeed(), 0.1);

        // First depth pass
        segment = segments.get(segmentIndex);
        assertEquals(SegmentType.POINT, segment.type);
        assertFalse(segment.point.hasX());
        assertFalse(segment.point.hasY());
        assertEquals(0, segment.point.getZ(), 0.01);
    }
}
