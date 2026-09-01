package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AutoLevelSettingsTest {

    @Test
    public void zRetractLowerThanMinShouldDefaultToMin() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();
        autoLevelSettings.setZRetract(-10);
        assertEquals(0.01, autoLevelSettings.getZRetract(), 0.0001);
    }

    @Test
    public void zRetractHigherThanMaxShouldDefaultToMin() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();
        autoLevelSettings.setZRetract(2);
        assertEquals(1, autoLevelSettings.getZRetract(), 0.0001);
    }

    @Test
    public void setMin_shouldConvertInchPositionToMillimeters() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();

        autoLevelSettings.setMin(new Position(1, 2, 3, Units.INCH));

        assertEquals(25.4, autoLevelSettings.getMinX(), 0.001);
        assertEquals(50.8, autoLevelSettings.getMinY(), 0.001);
        assertEquals(76.2, autoLevelSettings.getMinZ(), 0.001);
    }

    @Test
    public void setMax_shouldConvertInchPositionToMillimeters() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();

        autoLevelSettings.setMax(new Position(1, 2, 3, Units.INCH));

        assertEquals(25.4, autoLevelSettings.getMaxX(), 0.001);
        assertEquals(50.8, autoLevelSettings.getMaxY(), 0.001);
        assertEquals(76.2, autoLevelSettings.getMaxZ(), 0.001);
    }

    @Test
    public void setMin_shouldStoreMillimeterPositionUnchanged() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();

        autoLevelSettings.setMin(new Position(1, 2, 3, Units.MM));

        assertEquals(1, autoLevelSettings.getMinX(), 0.001);
        assertEquals(2, autoLevelSettings.getMinY(), 0.001);
        assertEquals(3, autoLevelSettings.getMinZ(), 0.001);
    }

    @Test
    public void setMax_shouldStoreMillimeterPositionUnchanged() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();

        autoLevelSettings.setMax(new Position(1, 2, 3, Units.MM));

        assertEquals(1, autoLevelSettings.getMaxX(), 0.001);
        assertEquals(2, autoLevelSettings.getMaxY(), 0.001);
        assertEquals(3, autoLevelSettings.getMaxZ(), 0.001);
    }

    @Test
    public void setAutoLevelProbeOffset_shouldConvertInchPositionToMillimeters() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();

        autoLevelSettings.setAutoLevelProbeOffset(new Position(1, 2, -3, Units.INCH));

        Position probeOffset = autoLevelSettings.getAutoLevelProbeOffset();
        assertEquals(Units.MM, probeOffset.getUnits());
        assertEquals(25.4, probeOffset.getX(), 0.001);
        assertEquals(50.8, probeOffset.getY(), 0.001);
        assertEquals(-76.2, probeOffset.getZ(), 0.001);
    }

    @Test
    public void copyConstructor_shouldCopyAllMillimeterValues() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();
        autoLevelSettings.setMin(new Position(1, 2, 3, Units.MM));
        autoLevelSettings.setMax(new Position(4, 5, 6, Units.MM));
        autoLevelSettings.setStepResolution(7);
        autoLevelSettings.setZSurface(8);
        autoLevelSettings.setZRetract(0.5);
        autoLevelSettings.setProbeSpeed(9);
        autoLevelSettings.setProbeScanFeedRate(10);
        autoLevelSettings.setAutoLevelArcSliceLength(11);
        autoLevelSettings.setTouchPlateThickness(12);
        autoLevelSettings.setAutoLevelProbeOffset(new Position(13, 14, 15, Units.MM));

        AutoLevelSettings copy = new AutoLevelSettings(autoLevelSettings);

        assertEquals(1, copy.getMinX(), 0.001);
        assertEquals(2, copy.getMinY(), 0.001);
        assertEquals(3, copy.getMinZ(), 0.001);
        assertEquals(4, copy.getMaxX(), 0.001);
        assertEquals(5, copy.getMaxY(), 0.001);
        assertEquals(6, copy.getMaxZ(), 0.001);
        assertEquals(7, copy.getStepResolution(), 0.001);
        assertEquals(8, copy.getZSurface(), 0.001);
        assertEquals(0.5, copy.getZRetract(), 0.001);
        assertEquals(9, copy.getProbeSpeed(), 0.001);
        assertEquals(10, copy.getProbeScanFeedRate(), 0.001);
        assertEquals(11, copy.getAutoLevelArcSliceLength(), 0.001);
        assertEquals(12, copy.getTouchPlateThickness(), 0.001);
        assertEquals(new Position(13, 14, 15, Units.MM), copy.getAutoLevelProbeOffset());
    }
}
