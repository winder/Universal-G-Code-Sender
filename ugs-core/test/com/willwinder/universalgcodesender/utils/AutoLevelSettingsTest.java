package com.willwinder.universalgcodesender.utils;

import static org.junit.Assert.*;
import org.junit.Test;

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
}