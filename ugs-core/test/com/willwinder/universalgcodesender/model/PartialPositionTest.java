package com.willwinder.universalgcodesender.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PartialPositionTest {

    @Test
    public void testFormatted() {

        assertEquals("Y0 Z0", new PartialPosition(null, 0.0, 0.0).getFormatted());
        assertEquals("X0 Z0", new PartialPosition(0.0, null, 0.0).getFormatted());
        assertEquals("X0 Y0", new PartialPosition(0.0, 0.0, null).getFormatted());

        assertEquals("Y10 Z0", new PartialPosition(null, 10.0, 0.0).getFormatted());
        assertEquals("X10 Z0", new PartialPosition(10.0, null, 0.0).getFormatted());
        assertEquals("X0 Y10", new PartialPosition(0.0, 10.0, null).getFormatted());

        assertEquals("Y10 Z-20", new PartialPosition(null, 10.0, -20.0).getFormatted());
        assertEquals("X10 Z-20", new PartialPosition(10.0, null, -20.0).getFormatted());
        assertEquals("X-20 Y10", new PartialPosition(-20.0, 10.0, null).getFormatted());

        assertEquals("Y10.5 Z-20.05", new PartialPosition(null, 10.5, -20.05).getFormatted());
        assertEquals("X10.5 Z-20.05", new PartialPosition(10.5, null, -20.05).getFormatted());
        assertEquals("X-20.05 Y10.5", new PartialPosition(-20.05, 10.5, null).getFormatted());

        assertEquals("X5.2 Y10.5 Z-20.05", new PartialPosition(5.2, 10.5, -20.05).getFormatted());
        assertEquals("X10.5 Y5.2 Z-20.05", new PartialPosition(10.5, 5.2, -20.05).getFormatted());
        assertEquals("X-20.05 Y10.5 Z5.2", new PartialPosition(-20.05, 10.5, 5.2).getFormatted());

        assertEquals("Y10.5 Z-20.05", new PartialPosition.Builder().setY(10.5).setZ(-20.05).build().getFormatted());
        assertEquals("X10.5 Z-20.05", new PartialPosition.Builder().setX(10.5).setZ(-20.05).build().getFormatted());
        assertEquals("X-20.05 Y10.5", new PartialPosition.Builder().setY(10.5).setX(-20.05).build().getFormatted());

        assertEquals("Y10.5 Z-20.05", new PartialPosition.Builder()
                .setValue(Axis.Y, 10.5).setValue(Axis.Z, -20.05).build()
                .getFormatted());
        assertEquals("X10.5 Z-20.05", new PartialPosition.Builder()
                .setValue(Axis.X, 10.5).setValue(Axis.Z, -20.05).build()
                .getFormatted());
        assertEquals("X-20.05 Y10.5", new PartialPosition.Builder()
                .setValue(Axis.Y, 10.5).setValue(Axis.X, -20.05).build()
                .getFormatted());


    }
}