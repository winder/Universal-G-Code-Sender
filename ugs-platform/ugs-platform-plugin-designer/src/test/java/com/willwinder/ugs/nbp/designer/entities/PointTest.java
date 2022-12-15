package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointTest {

    @Test
    public void pointsShouldNotHaveWidthAndHeight() {
        Point point = new Point(10, 10);
        assertEquals(0, point.getSize().getWidth(), 0.01);
    }
}
