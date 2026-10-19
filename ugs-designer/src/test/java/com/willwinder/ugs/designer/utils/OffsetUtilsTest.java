package com.willwinder.ugs.designer.utils;

import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.model.OffsetDirection;
import com.willwinder.ugs.designer.model.OffsetJoinStyle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class OffsetUtilsTest {

    @Test
    public void offset_shouldGrowShapeWhenOffsetOutward() {
        Rectangle rectangle = new Rectangle(10, 10, 20, 10);

        List<Path> result = OffsetUtils.offset(List.of(rectangle), 2, OffsetDirection.OUTWARD, OffsetJoinStyle.MITRE, false);

        assertEquals(1, result.size());
        Rectangle2D bounds = result.getFirst().getBounds();
        assertEquals(8, bounds.getX(), 0.01);
        assertEquals(8, bounds.getY(), 0.01);
        assertEquals(24, bounds.getWidth(), 0.01);
        assertEquals(14, bounds.getHeight(), 0.01);
    }

    @Test
    public void offset_shouldShrinkShapeWhenOffsetInward() {
        Rectangle rectangle = new Rectangle(10, 10, 20, 10);

        List<Path> result = OffsetUtils.offset(List.of(rectangle), 2, OffsetDirection.INWARD, OffsetJoinStyle.MITRE, false);

        assertEquals(1, result.size());
        Rectangle2D bounds = result.getFirst().getBounds();
        assertEquals(12, bounds.getX(), 0.01);
        assertEquals(12, bounds.getY(), 0.01);
        assertEquals(16, bounds.getWidth(), 0.01);
        assertEquals(6, bounds.getHeight(), 0.01);
    }

    @Test
    public void offset_shouldCreateTwoPathsWhenOffsetInBothDirections() {
        Rectangle rectangle = new Rectangle(10, 10, 20, 10);

        List<Path> result = OffsetUtils.offset(List.of(rectangle), 2, OffsetDirection.BOTH, OffsetJoinStyle.ROUND, false);

        assertEquals(2, result.size());
    }

    @Test
    public void offset_shouldReturnNothingWhenInwardOffsetSwallowsShape() {
        Rectangle rectangle = new Rectangle(10, 10, 20, 10);

        List<Path> result = OffsetUtils.offset(List.of(rectangle), 6, OffsetDirection.INWARD, OffsetJoinStyle.ROUND, false);

        assertTrue(result.isEmpty());
    }

    @Test
    public void offset_shouldReturnNothingWhenDistanceIsZero() {
        Rectangle rectangle = new Rectangle(10, 10, 20, 10);

        List<Path> result = OffsetUtils.offset(List.of(rectangle), 0, OffsetDirection.OUTWARD, OffsetJoinStyle.ROUND, false);

        assertTrue(result.isEmpty());
    }

    @Test
    public void offset_shouldMergeOverlappingResultsWhenUnioned() {
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(11, 0, 10, 10);

        List<Path> result = OffsetUtils.offset(List.of(first, second), 1, OffsetDirection.OUTWARD, OffsetJoinStyle.MITRE, true);

        assertEquals(1, result.size());
        Rectangle2D bounds = result.getFirst().getBounds();
        assertEquals(-1, bounds.getX(), 0.01);
        assertEquals(23, bounds.getWidth(), 0.01);
    }

    @Test
    public void offset_shouldKeepResultsSeparateWhenNotUnioned() {
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(11, 0, 10, 10);

        List<Path> result = OffsetUtils.offset(List.of(first, second), 1, OffsetDirection.OUTWARD, OffsetJoinStyle.MITRE, false);

        assertEquals(2, result.size());
    }
}
