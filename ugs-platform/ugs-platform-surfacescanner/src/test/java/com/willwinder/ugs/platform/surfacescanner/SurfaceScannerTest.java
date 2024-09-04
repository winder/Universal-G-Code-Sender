package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class SurfaceScannerTest {

    @Mock
    private BackendAPI backendAPI;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void probeEventShouldProgressTheScanForEachProbeEventInMillimeters() {
        Settings settings = new Settings();
        when(backendAPI.getSettings()).thenReturn(settings);

        SurfaceScanner surfaceScanner = new SurfaceScanner(backendAPI);
        surfaceScanner.reset();

        Position first = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, first.getUnits());
        assertEquals(0, first.getX(), 0.1);
        assertEquals(0, first.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(first, UnitUtils.Units.MM, 1));

        Position second = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, second.getUnits());
        assertEquals(0, second.getX(), 0.1);
        assertEquals(1, second.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(second, UnitUtils.Units.MM, 2));

        Position third = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, third.getUnits());
        assertEquals(1, third.getX(), 0.1);
        assertEquals(1, third.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(third, UnitUtils.Units.MM, 1));

        Position fourth = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, fourth.getUnits());
        assertEquals(1, fourth.getX(), 0.1);
        assertEquals(0, fourth.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(fourth, UnitUtils.Units.MM, 2));

        assertFalse(surfaceScanner.getNextProbePoint().isPresent());

        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[0][0].getUnits());
        assertEquals(1, surfaceScanner.getProbePositionGrid()[0][0].getZ(), 0.1);

        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[0][1].getUnits());
        assertEquals(2, surfaceScanner.getProbePositionGrid()[0][1].getZ(), 0.1);

        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[1][0].getUnits());
        assertEquals(2, surfaceScanner.getProbePositionGrid()[1][0].getZ(), 0.1);

        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[1][1].getUnits());
        assertEquals(1, surfaceScanner.getProbePositionGrid()[1][1].getZ(), 0.1);
    }

    @Test
    public void probeEventShouldAllowForSomePrecisionErrorsFromController() {
        Settings settings = new Settings();
        when(backendAPI.getSettings()).thenReturn(settings);

        SurfaceScanner surfaceScanner = new SurfaceScanner(backendAPI);
        surfaceScanner.reset();

        Position first = new Position(surfaceScanner.getNextProbePoint().get());
        first.setX(first.getX() - 0.1);
        surfaceScanner.probeEvent(createProbePoint(first, UnitUtils.Units.MM, 1));

        Position second = new Position(surfaceScanner.getNextProbePoint().get());
        first.setY(first.getY() + 0.1);
        surfaceScanner.probeEvent(createProbePoint(second, UnitUtils.Units.MM, 2));

        Position third = new Position(surfaceScanner.getNextProbePoint().get());
        third.setY(third.getY() + 0.11);
        assertThrows(RuntimeException.class, ()  -> surfaceScanner.probeEvent(createProbePoint(third, UnitUtils.Units.MM, 1)));
    }


    @Test
    public void probeEventShouldProgressTheScanForEachProbeEventInInches() {
        Settings settings = new Settings();
        when(backendAPI.getSettings()).thenReturn(settings);

        SurfaceScanner surfaceScanner = new SurfaceScanner(backendAPI);
        surfaceScanner.reset();

        Position first = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, first.getUnits());
        assertEquals(0, first.getX(), 0.1);
        assertEquals(0, first.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(first, UnitUtils.Units.INCH, 1));

        Position second = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, second.getUnits());
        assertEquals(0, second.getX(), 0.1);
        assertEquals(1, second.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(second, UnitUtils.Units.INCH, 2));

        Position third = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, third.getUnits());
        assertEquals(1, third.getX(), 0.1);
        assertEquals(1, third.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(third, UnitUtils.Units.INCH, 1));

        Position fourth = surfaceScanner.getNextProbePoint().get();
        assertEquals(UnitUtils.Units.MM, fourth.getUnits());
        assertEquals(1, fourth.getX(), 0.1);
        assertEquals(0, fourth.getY(), 0.1);
        surfaceScanner.probeEvent(createProbePoint(fourth, UnitUtils.Units.INCH, 2));

        assertFalse(surfaceScanner.getNextProbePoint().isPresent());
        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[0][0].getUnits());
        assertEquals(25.4, surfaceScanner.getProbePositionGrid()[0][0].getZ(), 0.1);

        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[0][1].getUnits());
        assertEquals(50.8, surfaceScanner.getProbePositionGrid()[0][1].getZ(), 0.1);

        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[1][0].getUnits());
        assertEquals(50.8, surfaceScanner.getProbePositionGrid()[1][0].getZ(), 0.1);

        assertEquals(UnitUtils.Units.MM, surfaceScanner.getProbePositionGrid()[1][1].getUnits());
        assertEquals(25.4, surfaceScanner.getProbePositionGrid()[1][1].getZ(), 0.1);
    }


    private static Position createProbePoint(Position position, UnitUtils.Units units, double z) {
        Position probePoint = new Position(position.getPositionIn(units));
        probePoint.setZ(z);
        return probePoint;
    }
}