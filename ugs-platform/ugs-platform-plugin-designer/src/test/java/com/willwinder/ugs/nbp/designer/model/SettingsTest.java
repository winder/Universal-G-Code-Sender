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
package com.willwinder.ugs.nbp.designer.model;

import com.willwinder.ugs.nbp.designer.logic.SettingsListener;
import static org.junit.Assert.assertEquals;
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
}