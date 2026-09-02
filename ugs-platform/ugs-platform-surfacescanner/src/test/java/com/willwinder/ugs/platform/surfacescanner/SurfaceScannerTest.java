package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ProbeEvent;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SurfaceScannerTest {

    @Mock
    private BackendAPI backendAPI;

    private ArgumentCaptor<String> sentGcodeCommands;
    private ArgumentCaptor<Double> probeFeedRate;
    private ArgumentCaptor<Double> probeDistance;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sentGcodeCommands = ArgumentCaptor.forClass(String.class);
        probeFeedRate = ArgumentCaptor.forClass(Double.class);
        probeDistance = ArgumentCaptor.forClass(Double.class);
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
        surfaceScanner.probeEvent(createProbePoint(third, UnitUtils.Units.MM, 3));

        Position fourth = new Position(surfaceScanner.getNextProbePoint().get());
        third.setY(fourth.getY() - 0.11);
        surfaceScanner.probeEvent(createProbePoint(third, UnitUtils.Units.MM, 4));

        Position[][] probePositionGrid = surfaceScanner.getProbePositionGrid();
        assertEquals(2, probePositionGrid.length);
        assertEquals(new Position(0, 0, 1.0, UnitUtils.Units.MM), probePositionGrid[0][0]);
        assertEquals(new Position(0, 1, 2.0, UnitUtils.Units.MM), probePositionGrid[0][1]);
        assertEquals(new Position(1, 0, 4.0, UnitUtils.Units.MM), probePositionGrid[1][0]);
        assertEquals(new Position(1, 1, 3.0, UnitUtils.Units.MM), probePositionGrid[1][1]);
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

    @Test
    public void scanShouldMoveToSafe() throws Exception {
        Settings settings = new Settings();
        settings.setSafetyHeight(6);

        AutoLevelSettings autoLevelSettings = settings.getAutoLevelSettings();
        autoLevelSettings.setMin(new Position(0,0,0, UnitUtils.Units.MM));
        autoLevelSettings.setMax(new Position(0,0,0, UnitUtils.Units.MM));
        autoLevelSettings.setProbeScanFeedRate(500);

        when(backendAPI.getSettings()).thenReturn(settings);
        when(backendAPI.getWorkPosition()).thenReturn(new Position(0,0,0 ,UnitUtils.Units.MM));
        when(backendAPI.getMachinePosition()).thenReturn(new Position(0,0,0, UnitUtils.Units.MM));

        ArgumentCaptor<String> sentGcodeCommandCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(backendAPI).sendGcodeCommand(anyBoolean(), sentGcodeCommandCaptor.capture());

        SurfaceScanner surfaceScanner = new SurfaceScanner(backendAPI);
        surfaceScanner.reset();
        surfaceScanner.scan();

        assertEquals(4, sentGcodeCommandCaptor.getAllValues().size());
        assertEquals("G21G90G0Z6F500", sentGcodeCommandCaptor.getAllValues().get(0));
        assertEquals("G21G90G0X0Y0F500", sentGcodeCommandCaptor.getAllValues().get(1));
        assertEquals("G21G90G0Z0F500", sentGcodeCommandCaptor.getAllValues().get(2));
        assertEquals("G21G90G0X0Y0F500", sentGcodeCommandCaptor.getAllValues().get(3));
    }

    @Test
    public void scan_shouldRaiseProbeMovementsByTheTouchPlateThickness() throws Exception {
        Settings settings = new Settings();
        settings.setSafetyHeight(6);

        AutoLevelSettings autoLevelSettings = settings.getAutoLevelSettings();
        autoLevelSettings.setMin(new Position(0, 0, 0, UnitUtils.Units.MM));
        autoLevelSettings.setMax(new Position(1, 1, 2, UnitUtils.Units.MM));
        autoLevelSettings.setProbeScanFeedRate(500);
        autoLevelSettings.setTouchPlateThickness(10);

        when(backendAPI.getSettings()).thenReturn(settings);
        when(backendAPI.getWorkPosition()).thenReturn(new Position(0, 0, 0, UnitUtils.Units.MM));
        when(backendAPI.getMachinePosition()).thenReturn(new Position(0, 0, 0, UnitUtils.Units.MM));

        ArgumentCaptor<String> sentGcodeCommandCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(backendAPI).sendGcodeCommand(anyBoolean(), sentGcodeCommandCaptor.capture());

        SurfaceScanner surfaceScanner = new SurfaceScanner(backendAPI);
        surfaceScanner.reset();
        surfaceScanner.scan();

        assertEquals("G21G90G0Z18F500", sentGcodeCommandCaptor.getAllValues().get(0));
        assertEquals("G21G90G0X0Y0F500", sentGcodeCommandCaptor.getAllValues().get(1));
        assertEquals("G21G90G0Z12F500", sentGcodeCommandCaptor.getAllValues().get(2));
        verify(backendAPI).probe(eq("Z"), anyDouble(), eq(-2.0), eq(UnitUtils.Units.MM));
    }

    @Test
    public void handleEvent_shouldRecordProbedHeightWithoutTheTouchPlateThickness() {
        Settings settings = new Settings();

        AutoLevelSettings autoLevelSettings = settings.getAutoLevelSettings();
        autoLevelSettings.setMin(new Position(0, 0, 0, UnitUtils.Units.MM));
        autoLevelSettings.setMax(new Position(1, 1, 2, UnitUtils.Units.MM));
        autoLevelSettings.setTouchPlateThickness(10);

        when(backendAPI.getSettings()).thenReturn(settings);
        when(backendAPI.getWorkPosition()).thenReturn(new Position(0, 0, 0, UnitUtils.Units.MM));
        when(backendAPI.getMachinePosition()).thenReturn(new Position(0, 0, 0, UnitUtils.Units.MM));

        SurfaceScanner surfaceScanner = new SurfaceScanner(backendAPI);
        surfaceScanner.reset();
        surfaceScanner.scan();
        surfaceScanner.handleEvent(new ProbeEvent(new Position(0, 0, 10.5, UnitUtils.Units.MM)));

        assertEquals(0.5, surfaceScanner.getProbePositionGrid()[0][0].getZ(), 0.001);
    }

    @Test
    public void scan_shouldMoveToSafetyHeightConvertedFromMillimeters() throws Exception {
        Settings settings = createInchSettings();
        SurfaceScanner surfaceScanner = createScanner(settings);

        surfaceScanner.scan();

        // 25.4mm safety height above the 25.4mm max boundary, at 254mm/min
        assertEquals("G20G90G0Z2F10", sentGcodeCommands.getAllValues().get(0));
        assertEquals("G20G90G0X0Y0F10", sentGcodeCommands.getAllValues().get(1));
        assertEquals("G20G90G0Z1F10", sentGcodeCommands.getAllValues().get(2));
    }

    @Test
    public void scan_shouldProbeWithSpeedAndDistanceConvertedFromMillimeters() throws Exception {
        Settings settings = createInchSettings();
        SurfaceScanner surfaceScanner = createScanner(settings);

        surfaceScanner.scan();

        // Probing at 254mm/min from the 25.4mm max boundary down to the 0mm min boundary
        verify(backendAPI).probe(eq("Z"), probeFeedRate.capture(), probeDistance.capture(), eq(UnitUtils.Units.INCH));
        assertEquals(10, probeFeedRate.getValue(), 0.001);
        assertEquals(-1, probeDistance.getValue(), 0.001);
    }

    @Test
    public void reset_shouldCreateGridWithStepResolutionConvertedFromMillimeters() throws Exception {
        Settings settings = createInchSettings();
        settings.getAutoLevelSettings().setStepResolution(12.7);

        SurfaceScanner surfaceScanner = createScanner(settings);

        Position[][] probePositionGrid = surfaceScanner.getProbePositionGrid();
        assertEquals(3, probePositionGrid.length);
        assertEquals(UnitUtils.Units.INCH, probePositionGrid[0][0].getUnits());
        assertEquals(0, probePositionGrid[0][0].getX(), 0.001);
        assertEquals(0.5, probePositionGrid[1][0].getX(), 0.001);
        assertEquals(1, probePositionGrid[2][0].getX(), 0.001);
    }

    @Test
    public void scan_shouldRaiseMovementsByTouchPlateThicknessConvertedFromMillimeters() throws Exception {
        Settings settings = createInchSettings();
        settings.getAutoLevelSettings().setTouchPlateThickness(25.4);
        SurfaceScanner surfaceScanner = createScanner(settings);

        surfaceScanner.scan();

        // The 25.4mm thick touch plate raises the start height and the probe target by one inch
        assertEquals("G20G90G0Z3F10", sentGcodeCommands.getAllValues().get(0));
        assertEquals("G20G90G0Z2F10", sentGcodeCommands.getAllValues().get(2));
        verify(backendAPI).probe(eq("Z"), probeFeedRate.capture(), probeDistance.capture(), eq(UnitUtils.Units.INCH));
        assertEquals(-1, probeDistance.getValue(), 0.001);
    }

    @Test
    public void handleEvent_shouldRetractWithinBoundsConvertedFromMillimeters() throws Exception {
        Settings settings = createInchSettings();
        SurfaceScanner surfaceScanner = createScanner(settings);
        surfaceScanner.scan();

        surfaceScanner.handleEvent(new ProbeEvent(new Position(0, 0, 0.25, UnitUtils.Units.INCH)));

        // A full retract from the probed position is capped by the 25.4mm max boundary
        assertEquals("G20G90G0Z1F10", sentGcodeCommands.getAllValues().get(4));
    }

    @Test
    public void probeEvent_shouldApplyProbeOffsetConvertedFromMillimeters() throws Exception {
        Settings settings = createInchSettings();
        settings.getAutoLevelSettings().setAutoLevelProbeOffset(new Position(25.4, 0, -25.4, UnitUtils.Units.MM));
        SurfaceScanner surfaceScanner = createScanner(settings);

        surfaceScanner.probeEvent(createProbePoint(surfaceScanner.getNextProbePoint().get(), UnitUtils.Units.INCH, 2));

        Position probedPosition = surfaceScanner.getProbePositionGrid()[0][0];
        assertEquals(1, probedPosition.getX(), 0.001);
        assertEquals(0, probedPosition.getY(), 0.001);
        assertEquals(1, probedPosition.getZ(), 0.001);
    }

    private Settings createInchSettings() {
        Settings settings = new Settings();
        settings.setPreferredUnits(UnitUtils.Units.INCH);
        settings.setSafetyHeight(25.4);

        AutoLevelSettings autoLevelSettings = settings.getAutoLevelSettings();
        autoLevelSettings.setMin(new Position(0, 0, 0, UnitUtils.Units.MM));
        autoLevelSettings.setMax(new Position(25.4, 25.4, 25.4, UnitUtils.Units.MM));
        autoLevelSettings.setStepResolution(25.4);
        autoLevelSettings.setProbeScanFeedRate(254);
        autoLevelSettings.setProbeSpeed(254);
        return settings;
    }

    private SurfaceScanner createScanner(Settings settings) throws Exception {
        when(backendAPI.getSettings()).thenReturn(settings);
        when(backendAPI.getWorkPosition()).thenReturn(new Position(0, 0, 0, settings.getPreferredUnits()));
        when(backendAPI.getMachinePosition()).thenReturn(new Position(0, 0, 0, settings.getPreferredUnits()));
        doNothing().when(backendAPI).sendGcodeCommand(anyBoolean(), sentGcodeCommands.capture());

        SurfaceScanner surfaceScanner = new SurfaceScanner(backendAPI);
        surfaceScanner.reset();
        return surfaceScanner;
    }

    private static Position createProbePoint(Position position, UnitUtils.Units units, double z) {
        Position probePoint = new Position(position.getPositionIn(units));
        probePoint.setZ(z);
        return probePoint;
    }
}