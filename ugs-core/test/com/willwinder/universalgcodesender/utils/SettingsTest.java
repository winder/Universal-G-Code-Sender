/*
    Copyright 2018 Will Winder

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

import static com.willwinder.universalgcodesender.utils.Settings.HISTORY_SIZE;
import org.assertj.core.api.Assertions;
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
        assertEquals("mm", target.getPreferredUnits().abbreviation);
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
        assertFalse(target.useZStepSize());
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

    @Test
    public void settingFileShouldUpdateRecents() {
      String path = "/some/file";
      String file = path + "/file.gcode";
      target.setLastOpenedFilename(file);

      Assertions.assertThat(target.getRecentFiles())
              .hasSize(1)
              .containsExactly(file);
      Assertions.assertThat(target.getRecentDirectories())
              .hasSize(1)
              .containsExactly(path);
    }

    @Test
    public void recentsShouldOverflowOldestAndReturnLIFO() {
      String path = "/some/file";

      // Add up recents to the brim.
      for(int i = 0; i < HISTORY_SIZE; i++) {
        target.setLastOpenedFilename(path + i + "/file.gcode");
      }

      // Overflow.
      target.setLastOpenedFilename(path + HISTORY_SIZE + "/file.gcode");

      Assertions.assertThat(target.getRecentFiles())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "0/file.gcode");
      Assertions.assertThat(target.getRecentDirectories())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "0");

      // Re-add "1" then overflow "2"
      target.setLastOpenedFilename(path + "1/file.gcode");
      target.setLastOpenedFilename(path + (HISTORY_SIZE + 1) + "/file.gcode");

      // Verify that "2" was bumped and that "1" is the most recent.
      Assertions.assertThat(target.getRecentFiles())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "2/file.gcode")
              .startsWith(path + "21/file.gcode", path + "1/file.gcode");
      Assertions.assertThat(target.getRecentDirectories())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "2")
              .startsWith(path + "21", path + "1");
    }
}
