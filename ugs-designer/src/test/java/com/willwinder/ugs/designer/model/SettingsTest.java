/*
    Copyright 2024 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.designer.model;

import com.willwinder.ugs.designer.logic.SettingsListener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SettingsTest {

    @Test
    public void setToolStepOverPreventZero() {
        Settings settings = new Settings();
        settings.setToolStepOver(0);
        assertTrue(settings.getToolStepOver() > 0);
    }

    @Test
    public void setToolStepOverPreventNegativeValues() {
        Settings settings = new Settings();
        settings.setToolStepOver(-0.1);
        assertTrue(settings.getToolStepOver() > 0);
    }

    @Test
    public void setToolStepOverPreventMoreThanHundredPercent() {
        Settings settings = new Settings();
        settings.setToolStepOver(2);
        assertEquals(1, settings.getToolStepOver(), 0.01);
    }

    @Test
    public void setToolStepOverShouldNotifyListeners() {
        SettingsListener listener = mock(SettingsListener.class);
        Settings settings = new Settings();
        settings.addListener(listener);
        settings.setToolStepOver(0.1);
        verify(listener, times(1)).onSettingsChanged();
    }

    @Test
    public void setDepthPerPassPreventZero() {
        Settings settings = new Settings();
        settings.setDepthPerPass(0);
        assertTrue(settings.getDepthPerPass() > 0);
    }

    @Test
    public void setDepthPerPassPreventNegativeValues() {
        Settings settings = new Settings();
        settings.setDepthPerPass(-0.1);
        assertEquals(0.1, settings.getDepthPerPass(), 0.01);
    }

    @Test
    public void setDepthPerPassShouldNotifyListeners() {
        SettingsListener listener = mock(SettingsListener.class);
        Settings settings = new Settings();
        settings.addListener(listener);
        settings.setDepthPerPass(0.1);
        verify(listener, times(1)).onSettingsChanged();
    }

    @Test
    public void getUseToolChanges_ShouldDefaultToOff() {
        Settings settings = new Settings();

        assertFalse(settings.getUseToolChanges());
    }

    @Test
    public void applySettings_ShouldCopyUseToolChanges() {
        Settings source = new Settings();
        source.setUseToolChanges(true);

        Settings target = new Settings();
        target.applySettings(source);

        assertTrue(target.getUseToolChanges());
    }

    @Test
    public void applySettings_shouldCopyThePenSettings() {
        Settings source = new Settings();
        source.setPenMode(PenMode.CUSTOM_COMMAND);
        source.setPenWidth(0.3);
        source.setPenDownDepth(0.8);
        source.setPenDownSpindleSpeed(700);
        source.setPenUpSpindleSpeed(20);
        source.setPenDownCommand("M280 P0 S30");
        source.setPenUpCommand("M280 P0 S90");

        Settings copy = new Settings(source);

        assertEquals(PenMode.CUSTOM_COMMAND, copy.getPenMode());
        assertEquals(0.3, copy.getPenWidth(), 1e-9);
        assertEquals(0.8, copy.getPenDownDepth(), 1e-9);
        assertEquals(700, copy.getPenDownSpindleSpeed());
        assertEquals(20, copy.getPenUpSpindleSpeed());
        assertEquals("M280 P0 S30", copy.getPenDownCommand());
        assertEquals("M280 P0 S90", copy.getPenUpCommand());
    }

    @Test
    public void setPenMode_shouldFallBackToTheZAxisForNull() {
        Settings settings = new Settings();

        settings.setPenMode(null);

        assertEquals(PenMode.Z_AXIS, settings.getPenMode());
    }
}
