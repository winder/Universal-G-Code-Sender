package com.willwinder.ugs.platform.surfacescanner.ui;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import com.willwinder.universalgcodesender.utils.AutoLevelArcMode;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;

import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertEquals(AutoLevelArcMode.LINE_SEGMENTS, autoLevelSettings.getArcMode());
    }

    @Test
    public void save_shouldStoreKeepArcsModeAndDisableSegmentLength() {
        Settings settings = new Settings();
        AutoLevelerSettingsPanel panel = new AutoLevelerSettingsPanel(settings, null);
        panel.updateComponents(settings);

        JComboBox<?> arcMode = findCombo(panel);
        arcMode.setSelectedItem(AutoLevelArcMode.KEEP_ARCS);
        panel.save();

        assertEquals(AutoLevelArcMode.KEEP_ARCS, settings.getAutoLevelSettings().getArcMode());
        assertEquals(1, countSpinners(panel, false));
        assertTrue(countSpinners(panel, true) > 1);

        panel.updateComponents(settings);
        panel.save();
        assertEquals(AutoLevelArcMode.KEEP_ARCS, settings.getAutoLevelSettings().getArcMode());
    }

    private static JComboBox<?> findCombo(Component component) {
        if (component instanceof JComboBox<?> combo) {
            return combo;
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                JComboBox<?> found = findCombo(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static int countSpinners(Component component, boolean enabled) {
        int count = 0;
        if (component instanceof UnitSpinner spinner && spinner.isEnabled() == enabled) {
            count++;
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                count += countSpinners(child, enabled);
            }
        }
        return count;
    }
}
