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
package com.willwinder.ugs.designer.io.ugsd;

import com.willwinder.ugs.designer.model.Design;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that .ugsd project files saved by pre-Tool-Library versions of UGS still load cleanly
 * and do not trigger any tool reconciliation side-effects. Existing users with saved projects
 * must not see any change in load behaviour.
 */
public class UgsDesignReaderBackwardCompatTest {

    @Test
    public void legacyFileWithSettingsBlockAndNoToolFieldLoads() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/x.ugsd")) {
            assertNotNull("x.ugsd fixture missing", in);
            Optional<Design> design = new UgsDesignReader().read(in);
            assertTrue("Legacy file with 'settings' block must still parse", design.isPresent());
            assertNull("Legacy file has no tool snapshot", design.get().getToolSnapshot());
            assertFalse("Entities should still load", design.get().getEntities().isEmpty());
        }
    }

    @Test
    public void pocketTestFixtureLoadsWithoutToolSnapshot() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/pocket-test.ugsd")) {
            assertNotNull("pocket-test.ugsd fixture missing", in);
            Optional<Design> design = new UgsDesignReader().read(in);
            assertTrue(design.isPresent());
            assertNull(design.get().getToolSnapshot());
            assertFalse(design.get().getEntities().isEmpty());
        }
    }

    @Test
    public void legacyJsonWithOldStyleSettingsBlockIsIgnoredGracefully() {
        // Matches the shape of older UGS-written .ugsd files — a top-level 'settings' object that
        // the current reader has never consumed. Must not throw, must not produce a tool snapshot.
        String legacy = "{\n" +
                "  \"version\": \"1\",\n" +
                "  \"settings\": {\n" +
                "    \"feedSpeed\": 1200,\n" +
                "    \"toolDiameter\": 3.175,\n" +
                "    \"stockThickness\": 12.0\n" +
                "  },\n" +
                "  \"entities\": []\n" +
                "}";
        Optional<Design> design = new UgsDesignReader()
                .read(IOUtils.toInputStream(legacy, StandardCharsets.UTF_8));
        assertTrue(design.isPresent());
        assertNull(design.get().getToolSnapshot());
    }

    @Test
    public void legacyFileWithExplicitNullToolField() {
        String legacy = "{\"version\":\"1\",\"tool\":null,\"entities\":[]}";
        Optional<Design> design = new UgsDesignReader()
                .read(IOUtils.toInputStream(legacy, StandardCharsets.UTF_8));
        assertTrue(design.isPresent());
        assertNull(design.get().getToolSnapshot());
    }
}
