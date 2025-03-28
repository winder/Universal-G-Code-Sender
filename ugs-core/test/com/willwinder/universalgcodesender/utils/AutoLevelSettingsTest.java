package com.willwinder.universalgcodesender.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class AutoLevelSettingsTest {

    @Test
    public void testAutoLevelSettings() {
        AutoLevelSettings autoLevelSettings = new AutoLevelSettings();
        autoLevelSettings.setZRetract(-10);
        assertEquals(0.1, autoLevelSettings.getZRetract(), 0.0001);
    }
}