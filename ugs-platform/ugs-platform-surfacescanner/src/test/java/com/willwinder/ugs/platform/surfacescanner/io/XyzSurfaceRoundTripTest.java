package com.willwinder.ugs.platform.surfacescanner.io;

import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class XyzSurfaceRoundTripTest {
    private MockedStatic<LookupService> lookup;

    @Before
    public void setUp() {
        BackendAPI backend = mock(BackendAPI.class);
        Settings settings = new Settings();
        settings.setPreferredUnits(Units.MM);
        when(backend.getSettings()).thenReturn(settings);
        lookup = mockStatic(LookupService.class);
        lookup.when(() -> LookupService.lookup(BackendAPI.class)).thenReturn(backend);
    }

    @After
    public void tearDown() {
        lookup.close();
    }

    @Test
    public void rectangularAndScatteredFilesShouldLoadWithoutHeaders() throws Exception {
        String rectangular = "0.0 0.0 0.1\n0.0 1.0 0.2\n1.0 0.0 0.3\n1.0 1.0 0.4\n";
        String scattered = "0.0 0.0 0.1\n  10.0   0.0   0.2\n\n2.0 8.0 0.5\n";
        XyzSurfaceReader reader = new XyzSurfaceReader();

        List<Position> rectangularPoints = reader.read(new ByteArrayInputStream(rectangular.getBytes(StandardCharsets.UTF_8)));
        List<Position> scatteredPoints = reader.read(new ByteArrayInputStream(scattered.getBytes(StandardCharsets.UTF_8)));
        assertEquals(4, rectangularPoints.size());
        assertEquals(3, scatteredPoints.size());

        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        SurfaceScanner scanner = new SurfaceScanner(backend);
        scanner.setScannedPoints(rectangularPoints);
        assertTrue(scanner.isValid());
        scanner.setScannedPoints(scatteredPoints);
        assertTrue(scanner.isValid());
        assertEquals(2, scanner.getScannedPoints().get(2).getX(), 0.001);
    }
}
