package com.willwinder.universalgcodesender.visualizer;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class VisualizerUtilsTest {

    @Test
    public void toCartesianShouldNotApplyRotationIfZero() {
        Position position = new Position(10, 11, 12, UnitUtils.Units.MM);
        Position position1 = VisualizerUtils.toCartesian(position);
        assertEquals(10d, position1.x, 0.1);
        assertEquals(11d, position1.y, 0.1);
        assertEquals(12d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }

    @Test
    public void toCartesianShouldRotateAAroundX() {
        Position position = new Position(10, 10, 10, 180, 0, 0, UnitUtils.Units.MM);
        Position position1 = VisualizerUtils.toCartesian(position);
        assertEquals(10d, position1.x, 0.1);
        assertEquals(-10d, position1.y, 0.1);
        assertEquals(-10d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }

    @Test
    public void toCartesianShouldRotateBAroundY() {
        Position position = new Position(10, 10, 10, 0, 180, 0, UnitUtils.Units.MM);
        Position position1 = VisualizerUtils.toCartesian(position);
        assertEquals(-10d, position1.x, 0.1);
        assertEquals(10d, position1.y, 0.1);
        assertEquals(-10d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }

    @Test
    public void toCartesianShouldRotateCAroundZ() {
        Position position = new Position(10, 10, 10, 0, 0, 180, UnitUtils.Units.MM);
        Position position1 = VisualizerUtils.toCartesian(position);
        assertEquals(-10d, position1.x, 0.1);
        assertEquals(-10d, position1.y, 0.1);
        assertEquals(10d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }
}
