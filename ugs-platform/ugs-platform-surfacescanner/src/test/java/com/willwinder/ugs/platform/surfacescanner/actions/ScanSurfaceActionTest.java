package com.willwinder.ugs.platform.surfacescanner.actions;

import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.ugs.platform.surfacescanner.Utils;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScanSurfaceActionTest {
    private BackendAPI backend;
    private SurfaceScanner scanner;
    private MockedStatic<LookupService> lookup;

    @Before
    public void setUp() {
        backend = mock(BackendAPI.class);
        Settings settings = new Settings();
        settings.getAutoLevelSettings().setMin(new Position(0, 0, -1, Units.MM));
        settings.getAutoLevelSettings().setMax(new Position(1, 1, 1, Units.MM));
        when(backend.getSettings()).thenReturn(settings);
        when(backend.getMachinePosition()).thenReturn(Position.ZERO);
        lookup = mockStatic(LookupService.class);
        lookup.when(() -> LookupService.lookup(BackendAPI.class)).thenReturn(backend);
        scanner = spy(new SurfaceScanner(backend));
    }

    @After
    public void tearDown() {
        lookup.close();
    }

    @Test
    public void rejectedScanShouldDisplayLocalizedErrorWithoutMovement() throws Exception {
        when(backend.getWorkPosition()).thenReturn(new Position(0, 0, 2, Units.MM));
        try (MockedStatic<GUIHelpers> dialogs = mockStatic(GUIHelpers.class)) {
            new ScanSurfaceAction(scanner).actionPerformed(null);
            dialogs.verify(() -> GUIHelpers.displayErrorDialog(Localization.getString("autoleveler.scan-invalid-z")));
        }
        verify(backend, never()).sendGcodeCommand(anyBoolean(), anyString());
        verify(backend, never()).probe(anyString(), anyDouble(), anyDouble(), any());
    }

    @Test
    public void validScanShouldStartScanner() throws Exception {
        when(backend.getWorkPosition()).thenReturn(Position.ZERO);
        new ScanSurfaceAction(scanner).actionPerformed(null);
        verify(scanner).scan();
        verify(backend).probe(eq("Z"), anyDouble(), eq(-2.0), eq(Units.MM));
    }

    @Test
    public void cancelledOverwriteShouldNotScan() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.removeProbeData(scanner)).thenReturn(false);
            new ScanSurfaceAction(scanner).actionPerformed(null);
        }
        verify(scanner, never()).scan();
        verify(backend, never()).getWorkPosition();
    }
}
