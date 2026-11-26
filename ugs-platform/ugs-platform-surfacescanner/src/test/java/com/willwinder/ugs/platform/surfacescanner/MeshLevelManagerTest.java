package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeshLevelManagerTest {
    private BackendAPI backend;
    private SurfaceScanner scanner;
    private MeshLevelManager manager;

    @Before
    public void setUp() {
        backend = mock(BackendAPI.class);
        Settings settings = new Settings();
        when(backend.getSettings()).thenReturn(settings);
        scanner = new SurfaceScanner(backend);
        scanner.setScannedPoints(List.of(
                new Position(0, 0, 0.1, Units.MM),
                new Position(10, 0, 0.2, Units.MM),
                new Position(0, 10, 0.4, Units.MM)));
        manager = new MeshLevelManager(scanner, backend);
    }

    @Test
    public void reapplyShouldInstallTheProcessorAgainWithoutClearingPoints() throws Exception {
        manager.update();
        manager.reapply();

        verify(backend).removeCommandProcessor(any());
        verify(backend, times(2)).replaceCommandProcessor(isNull(), any());
        assertEquals(3, scanner.getScannedPoints().size());
        assertEquals(0.4, scanner.getScannedPoints().get(2).getZ(), 0.001);
    }
}
