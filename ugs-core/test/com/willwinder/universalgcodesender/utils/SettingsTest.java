/*
    Copywrite 2018 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joacim Breiler
 */
public class SettingsTest {

    private Settings target;
    private boolean hasNotifiedListener;

    @Before
    public void setUp() {
        target = new Settings();
    }

    @Test
    public void settingsShouldHaveDefaultValues() {
        assertFalse(target.isAutoConnectEnabled());
        assertNotNull(target.getAutoLevelSettings());
        assertFalse(target.isAutoReconnect());
        assertFalse(target.isCommandTableEnabled());
        assertEquals("mm", target.getDefaultUnits());
        assertTrue(target.isDisplayStateColor());
        assertNotNull(target.getFileStats());
        assertEquals("GRBL", target.getFirmwareVersion());
        assertEquals(Double.valueOf(10.0), Double.valueOf(target.getJogFeedRate()));
        assertEquals("en_US", target.getLanguage());
        assertEquals(Integer.valueOf(1), target.getNumMacros());
        assertNotNull(target.getMainWindowSettings());
        assertFalse(target.isManualModeEnabled());
        assertEquals(Double.valueOf(1.0), Double.valueOf(target.getManualModeStepSize()));
        assertNotNull(target.getPendantConfig());
        assertEquals("", target.getPort());
        assertEquals("115200", target.getPortRate());
        assertTrue(target.isScrollWindowEnabled());
        assertTrue(target.isShowNightlyWarning());
        assertTrue(target.isShowSerialPortWarning());
        assertFalse(target.isSingleStepMode());
        assertEquals(Double.valueOf(200), Double.valueOf(target.getStatusUpdateRate()));
        assertTrue(target.isStatusUpdatesEnabled());
        assertTrue(target.useZStepSize());
        assertFalse(target.isVerboseOutputEnabled());
        assertNotNull(target.getVisualizerWindowSettings());
        assertEquals(Double.valueOf(1), Double.valueOf(target.getzJogStepSize()));
    }

    @Test
    public void changingValueShouldNotifyObservers() {
        hasNotifiedListener = false;
        target.setSettingChangeListener(() -> {
            hasNotifiedListener = true;
        });

        target.setPort("/dev/ttyS0");
        assertTrue(hasNotifiedListener);
    }

}
