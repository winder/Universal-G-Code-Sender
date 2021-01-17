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

import java.io.File;
import java.io.IOException;

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
        assertNotNull(target.getFileStats());
        assertEquals("GRBL", target.getFirmwareVersion());
        assertEquals(Double.valueOf(10.0), Double.valueOf(target.getJogFeedRate()));
        assertEquals("en_US", target.getLanguage());
        assertNotNull(target.getMainWindowSettings());
        assertFalse(target.isManualModeEnabled());
        assertEquals(Double.valueOf(1.0), Double.valueOf(target.getManualModeStepSize()));
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
        assertEquals(Double.valueOf(1), Double.valueOf(target.getZJogStepSize()));
        assertFalse(target.isAutoStartPendant());
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
    public void settingFileShouldUpdateRecents() throws IOException {
      String path = "/some/file";
      File file = new File(path + "/file.gcode").getCanonicalFile() ;

      target.setLastOpenedFilename(file.getCanonicalPath());
      
      Assertions.assertThat(target.getRecentFiles())
              .hasSize(1)
              .containsExactly(file.getPath());
      Assertions.assertThat(target.getRecentDirectories())
              .hasSize(1)
              .containsExactly(file.getParentFile().getPath());
    }

    @Test
    public void recentsShouldOverflowOldestAndReturnLIFO() throws IOException {
      String path = new File("/some/file").getCanonicalPath();

      // Add up recents to the brim.
      for(int i = 0; i < HISTORY_SIZE; i++) {
        target.setLastOpenedFilename(path + i + File.separator+"file.gcode");
      }

      // Overflow.
      target.setLastOpenedFilename(path + HISTORY_SIZE + File.separator+"file.gcode");

      Assertions.assertThat(target.getRecentFiles())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "0"+File.separator+"file.gcode");
      Assertions.assertThat(target.getRecentDirectories())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "0");

      // Re-add "1" then overflow "2"
      target.setLastOpenedFilename(path + "1"+File.separator+"file.gcode");
      target.setLastOpenedFilename(path + (HISTORY_SIZE + 1) + File.separator+"file.gcode");

      // Verify that "2" was bumped and that "1" is the most recent.
      Assertions.assertThat(target.getRecentFiles())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "2"+File.separator+"file.gcode")
              .startsWith(path + "21"+File.separator+"file.gcode", path + "1"+File.separator+"file.gcode");
      Assertions.assertThat(target.getRecentDirectories())
              .hasSize(HISTORY_SIZE)
              .doesNotContain(path + "2")
              .startsWith(path + "21", path + "1");
    }
}
