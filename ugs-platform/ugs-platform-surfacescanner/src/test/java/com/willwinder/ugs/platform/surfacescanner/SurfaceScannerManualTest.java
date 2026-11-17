package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurfaceScannerManualTest {
    private Settings settings;
    private SurfaceScanner scanner;

    @Before
    public void setUp() {
        BackendAPI backend = mock(BackendAPI.class);
        settings = new Settings();
        when(backend.getSettings()).thenReturn(settings);
        scanner = new SurfaceScanner(backend);
    }

    @Test
    public void manualPointsShouldBecomeValidAndReplaceNearbyXY() {
        scanner.captureManualPoint(new Position(0, 0, 1, Units.MM));
        scanner.captureManualPoint(new Position(10, 0, 2, Units.MM));
        assertFalse(scanner.isValid());

        scanner.captureManualPoint(new Position(0, 10, 3, Units.MM));
        assertTrue(scanner.isValid());
        assertEquals(3, scanner.getScannedPoints().size());

        scanner.captureManualPoint(new Position(0.005, 10, 4, Units.MM));
        assertEquals(3, scanner.getScannedPoints().size());
        assertEquals(4, scanner.getScannedPoints().get(2).getZ(), 0.001);
    }

    @Test
    public void manualCaptureShouldSubtractTouchPlateThickness() {
        settings.getAutoLevelSettings().setTouchPlateThickness(2.5);
        scanner.captureManualPoint(new Position(1, 2, 10, Units.MM));
        scanner.captureManualPoint(new Position(1, 2, 12, Units.MM));

        assertEquals(1, scanner.getScannedPoints().size());
        Position captured = scanner.getScannedPoints().get(0);
        assertEquals(1, captured.getX(), 0.001);
        assertEquals(2, captured.getY(), 0.001);
        assertEquals(9.5, captured.getZ(), 0.001);
    }

    @Test
    public void manualCaptureShouldSubtractThicknessInPreferredUnits() {
        settings.setPreferredUnits(Units.INCH);
        settings.getAutoLevelSettings().setTouchPlateThickness(25.4);
        scanner.captureManualPoint(new Position(0, 0, 2, Units.INCH));

        Position captured = scanner.getScannedPoints().get(0);
        assertEquals(Units.INCH, captured.getUnits());
        assertEquals(1, captured.getZ(), 0.001);
    }

    @Test
    public void rebuildingTheProbeGridShouldKeepSampledPoints() {
        scanner.captureManualPoint(new Position(0, 0, 1, Units.MM));
        scanner.captureManualPoint(new Position(10, 0, 2, Units.MM));
        scanner.captureManualPoint(new Position(0, 10, 3, Units.MM));

        scanner.update(new Position(-5, -5, -1, Units.MM), new Position(30, 30, 4, Units.MM));
        scanner.reset();

        assertTrue(scanner.isValid());
        assertEquals(3, scanner.getScannedPoints().size());
        assertEquals(1, scanner.getScannedPoints().get(0).getZ(), 0.001);
        assertEquals(-5, scanner.getProbePositionGrid()[0][0].getX(), 0.001);
    }

    @Test
    public void invalidManualPointShouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> scanner.captureManualPoint(null));
        assertThrows(IllegalArgumentException.class,
                () -> scanner.captureManualPoint(new Position(Double.NaN, 0, 0, Units.MM)));
    }

    @Test
    public void loadedPointsShouldRoundTripWithoutGridAssumptions() {
        scanner.setScannedPoints(List.of(
                new Position(4, 1, 0.2, Units.MM),
                new Position(0, 0, 0.1, Units.MM),
                new Position(1, 5, 0.3, Units.MM)));
        assertTrue(scanner.isValid());
        assertEquals(4, scanner.getScannedPoints().get(0).getX(), 0.001);
        scanner.removeManualPoint(0);
        assertFalse(scanner.isValid());
        scanner.clearManualPoints();
        assertTrue(scanner.getScannedPoints().isEmpty());
    }
}
