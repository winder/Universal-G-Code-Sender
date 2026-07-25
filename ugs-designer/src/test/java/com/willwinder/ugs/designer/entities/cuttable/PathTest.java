package com.willwinder.ugs.designer.entities.cuttable;

import com.willwinder.ugs.designer.model.Size;
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
    public void isClosedShouldReturnFalseForExplicitlyClosedPathNotEndingAtTheStartPoint() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.close();

        boolean closed = path.isClosed();

        assertFalse(closed);
    }

    @Test
    public void isClosedShouldReturnTrueWhenLastPointIsAtTheStartPoint() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.lineTo(0, 0);

        boolean closed = path.isClosed();

        assertTrue(closed);
    }

    @Test
    public void isClosedShouldReturnFalseWhenLastPointIsNotAtTheStartPoint() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);

        boolean closed = path.isClosed();

        assertFalse(closed);
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