package com.willwinder.ugs.platform.surfacescanner.actions;

import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaptureSurfacePointActionTest {
    private BackendAPI backend;
    private SurfaceScanner scanner;
    private MockedStatic<LookupService> lookup;

    @Before
    public void setUp() {
        backend = mock(BackendAPI.class);
        when(backend.getSettings()).thenReturn(new Settings());
        when(backend.isConnected()).thenReturn(true);
        when(backend.isIdle()).thenReturn(true);
        lookup = mockStatic(LookupService.class);
        lookup.when(() -> LookupService.lookup(BackendAPI.class)).thenReturn(backend);
        scanner = new SurfaceScanner(backend);
    }

    @After
    public void tearDown() {
        lookup.close();
    }

    @Test
    public void disconnectedControllerShouldDisableCapture() {
        when(backend.isConnected()).thenReturn(false);
        assertFalse(new CaptureSurfacePointAction(scanner).isEnabled());
    }

    @Test
    public void validPositionShouldBeCaptured() {
        when(backend.getWorkPosition()).thenReturn(new Position(1, 2, 3, Units.MM));
        new CaptureSurfacePointAction(scanner).actionPerformed(null);
        verify(backend).getWorkPosition();
        assertFalse(scanner.getScannedPoints().isEmpty());
    }

    @Test
    public void captureShouldSubtractTouchPlateThickness() {
        backend.getSettings().getAutoLevelSettings().setTouchPlateThickness(1.5);
        when(backend.getWorkPosition()).thenReturn(new Position(1, 2, 3, Units.MM));
        new CaptureSurfacePointAction(scanner).actionPerformed(null);
        assertEquals(1.5, scanner.getScannedPoints().get(0).getZ(), 0.001);
    }

    @Test
    public void invalidPositionShouldDisplayError() {
        when(backend.getWorkPosition()).thenReturn(null);
        try (MockedStatic<GUIHelpers> dialogs = mockStatic(GUIHelpers.class)) {
            new CaptureSurfacePointAction(scanner).actionPerformed(null);
            dialogs.verify(() -> GUIHelpers.displayErrorDialog(Localization.getString("autoleveler.panel.capture-invalid")));
        }
    }
}
