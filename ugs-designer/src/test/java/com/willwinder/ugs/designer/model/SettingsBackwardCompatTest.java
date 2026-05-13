/*
    Copyright 2026 Damian Nikodem

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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Guards the invariants existing users rely on: a fresh {@link Settings} looks exactly like it
 * did before the Tool Library existed, and applying another "legacy-shaped" Settings never
 * introduces tool-library state out of thin air.
 */
public class SettingsBackwardCompatTest {

    @Test
    public void freshSettingsHasNoLibraryBinding() {
        Settings settings = new Settings();
        assertNull(settings.getCurrentToolId());
        assertNull(settings.getCurrentToolSnapshot());
    }

    @Test
    public void defaultNumericValuesAreUnchanged() {
        // Anchoring the pre-Tool-Library defaults — if any of these change, existing users'
        // newly created designs would behave differently without a migration being run.
        Settings settings = new Settings();
        assertEquals(1000, settings.getFeedSpeed());
        assertEquals(400, settings.getPlungeSpeed());
        assertEquals(3.0, settings.getToolDiameter(), 1e-9);
        assertEquals(10.0, settings.getStockThickness(), 1e-9);
        assertEquals(5.0, settings.getSafeHeight(), 1e-9);
        assertEquals(0.3, settings.getToolStepOver(), 1e-9);
        assertEquals(1.0, settings.getDepthPerPass(), 1e-9);
        assertEquals(255, settings.getMaxSpindleSpeed());
        assertEquals("M3", settings.getSpindleDirection());
    }

    @Test
    public void applyingLegacyShapedSettingsPreservesNullLibraryBinding() {
        // A "legacy-shaped" Settings mirrors what an older in-memory instance would look like —
        // no currentToolId, no snapshot. Apply() must not spin up new library state.
        Settings legacy = new Settings();
        legacy.setFeedSpeed(1234);
        legacy.setToolDiameter(4.2);

        Settings target = new Settings();
        target.applySettings(legacy);

        assertEquals(1234, target.getFeedSpeed());
        assertEquals(4.2, target.getToolDiameter(), 1e-9);
        assertNull("applySettings from a tool-less source must leave currentToolId null",
                target.getCurrentToolId());
        assertNull(target.getCurrentToolSnapshot());
    }

    @Test
    public void applyingClearsAnExistingLibraryBindingWhenSourceHasNone() {
        // A user who had selected a library tool, and now opens a pre-change project, should
        // end up without a stale library binding.
        Settings existing = new Settings();
        existing.setCurrentToolId("some-library-tool");

        Settings projectWithoutLibrary = new Settings();
        existing.applySettings(projectWithoutLibrary);

        assertNull(existing.getCurrentToolId());
        assertNull(existing.getCurrentToolSnapshot());
    }

    @Test
    public void copyConstructorOnLegacyShapePreservesNulls() {
        Settings legacy = new Settings();
        legacy.setFeedSpeed(700);
        Settings copy = new Settings(legacy);
        assertEquals(700, copy.getFeedSpeed());
        assertNull(copy.getCurrentToolId());
        assertNull(copy.getCurrentToolSnapshot());
    }
}
