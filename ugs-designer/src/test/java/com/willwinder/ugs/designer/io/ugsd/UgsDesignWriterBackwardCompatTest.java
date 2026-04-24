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

import com.willwinder.ugs.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.designer.entities.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.model.Design;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Existing users who have never interacted with the Tool Library must be able to save and reopen
 * their projects without any new tool data appearing in the file, and without the reader
 * mistakenly inferring a tool from a missing field.
 */
public class UgsDesignWriterBackwardCompatTest {

    @Test
    public void writingControllerWithoutLibrarySelectionEmitsNoActiveTool() throws Exception {
        Controller controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        controller.addEntity(new Rectangle());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new UgsDesignWriter().write(out, controller);
        String json = out.toString(StandardCharsets.UTF_8);

        // "tool" can be absent or explicitly null, but must not contain any real values.
        assertTrue("Output must be valid JSON containing entities", json.contains("entities"));
        assertFalse("Output must not spontaneously embed a library tool",
                json.contains("\"feedSpeed\":") && json.contains("\"diameterUnit\":"));
    }

    @Test
    public void roundTripOfLegacyStyleControllerYieldsNullToolSnapshot() throws Exception {
        Controller controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        controller.addEntity(new Rectangle());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new UgsDesignWriter().write(out, controller);

        Optional<Design> reloaded = new UgsDesignReader()
                .read(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(reloaded.isPresent());
        assertNull("Round-trip must not invent a tool snapshot", reloaded.get().getToolSnapshot());
        assertEquals(1, reloaded.get().getEntities().size());
    }
}
