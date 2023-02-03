package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Test;

import static org.junit.Assert.*;

public class PathTest {

    @Test
    public void pathInOnlyOneDimensionShouldReturnCorrectWidth() {
        Path path = new Path();
        path.moveTo(10, 0);
        path.lineTo(20, 0);
        path.close();

        Size size = path.getSize();
        assertEquals(10, size.getWidth(), 0.1);
        assertEquals(0, size.getHeight(), 0.1);
    }

    @Test
    public void pathInOnlyOneDimensionShouldReturnCorrectHeight() {
        Path path = new Path();
        path.moveTo(0, 10);
        path.lineTo(0, 20);
        path.close();

        Size size = path.getSize();
        assertEquals(0, size.getWidth(), 0.1);
        assertEquals(10, size.getHeight(), 0.1);
    }
}