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
    private SurfaceScanner scanner;

    @Before
    public void setUp() {
        BackendAPI backend = mock(BackendAPI.class);
        Settings settings = new Settings();
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
