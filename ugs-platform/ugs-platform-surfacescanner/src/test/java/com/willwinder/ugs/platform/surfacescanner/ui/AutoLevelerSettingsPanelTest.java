package com.willwinder.ugs.platform.surfacescanner.ui;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AutoLevelerSettingsPanelTest {

    @Test
    public void save_shouldStoreSettingsInMillimetersWhenDisplayedInInches() {
        Settings settings = new Settings();
        settings.setPreferredUnits(UnitUtils.Units.INCH);

        AutoLevelSettings autoLevelSettings = settings.getAutoLevelSettings();
        autoLevelSettings.setTouchPlateThickness(25.4);
        autoLevelSettings.setProbeSpeed(254);
        autoLevelSettings.setAutoLevelProbeOffset(new Position(25.4, -25.4, 0, UnitUtils.Units.MM));

        AutoLevelerSettingsPanel panel = new AutoLevelerSettingsPanel(settings, null);
        panel.updateComponents(settings);
        panel.save();

        assertEquals(25.4, autoLevelSettings.getTouchPlateThickness(), 0.001);
        assertEquals(254, autoLevelSettings.getProbeSpeed(), 0.001);
        assertEquals(25.4, autoLevelSettings.getAutoLevelProbeOffset().getX(), 0.001);
        assertEquals(-25.4, autoLevelSettings.getAutoLevelProbeOffset().getY(), 0.001);
        assertEquals(UnitUtils.Units.MM, autoLevelSettings.getAutoLevelProbeOffset().getUnits());
    }
}
