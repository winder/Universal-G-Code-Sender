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
package com.willwinder.ugs.designer.logic;

import com.willwinder.ugs.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.model.Settings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 * Verifies that {@link Controller#loadFile(File)} is a no-op for Tool Library state when the
 * project file was saved by a pre-Tool-Library UGS. An existing user reopening one of their old
 * projects must not see any prompt and must not have their in-memory tool settings overwritten.
 */
public class ControllerLoadBackwardCompatTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void loadingLegacyProjectDoesNotSetToolId() throws Exception {
        File legacy = copyFixtureToTemp("/pocket-test.ugsd", "pocket-test.ugsd");

        Controller controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        Settings initial = new Settings(controller.getSettings());

        controller.loadFile(legacy);

        Settings after = controller.getSettings();
        assertNull("Legacy file has no tool id to restore", after.getCurrentToolId());
        assertNull("Legacy file has no tool snapshot", after.getCurrentToolSnapshot());
        assertEquals("Tool diameter must remain whatever the session had before load",
                initial.getToolDiameter(), after.getToolDiameter(), 1e-9);
        assertEquals(initial.getFeedSpeed(), after.getFeedSpeed());
        assertEquals(initial.getPlungeSpeed(), after.getPlungeSpeed());
        assertEquals(initial.getDepthPerPass(), after.getDepthPerPass(), 1e-9);
    }

    @Test
    public void loadingSecondLegacyFixtureBehavesIdentically() throws Exception {
        File legacy = copyFixtureToTemp("/x.ugsd", "x.ugsd");

        Controller controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        controller.getSettings().setToolDiameter(5.5);
        controller.getSettings().setFeedSpeed(1234);

        controller.loadFile(legacy);

        Settings after = controller.getSettings();
        assertNull(after.getCurrentToolId());
        assertNull(after.getCurrentToolSnapshot());
        assertEquals("User's in-memory tool diameter must not be overwritten by load",
                5.5, after.getToolDiameter(), 1e-9);
        assertEquals("User's in-memory feed speed must not be overwritten by load",
                1234, after.getFeedSpeed());
        assertNotNull(controller.getDrawing().getRootEntity());
    }

    private File copyFixtureToTemp(String resourcePath, String filename) throws Exception {
        File out = new File(tempFolder.getRoot(), filename);
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull("Fixture " + resourcePath + " missing", in);
            Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return out;
    }
}
