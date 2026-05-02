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

import com.willwinder.ugs.designer.logic.ToolLibrarySyncService.Outcome;
import com.willwinder.ugs.designer.logic.ToolLibrarySyncService.Result;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ToolLibrarySyncServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ToolLibraryService library;
    private ToolLibrarySyncService sync;

    @Before
    public void setup() throws Exception {
        Path libraryPath = tempFolder.newFolder().toPath().resolve("tool-library.json");
        library = new ToolLibraryService(libraryPath);
        sync = new ToolLibrarySyncService(library);
    }

    @Test
    public void nullProjectToolIsNoOp() {
        Result result = sync.resolveDeterministic(null);
        assertEquals(Outcome.NO_OP, result.outcome());
    }

    @Test
    public void matchingToolIsNoOp() {
        ToolDefinition libraryTool = library.getTools().get(0);
        ToolDefinition projectTool = new ToolDefinition(libraryTool);
        Result result = sync.resolveDeterministic(projectTool);
        assertEquals(Outcome.NO_OP, result.outcome());
        assertEquals(libraryTool, result.toolToApply());
    }

    @Test
    public void differingLibraryToolRequestsApplyProject() {
        ToolDefinition libraryTool = library.getTools().get(0);
        ToolDefinition projectTool = new ToolDefinition(libraryTool);
        projectTool.setFeedSpeed(libraryTool.getFeedSpeed() + 123);
        Result result = sync.resolveDeterministic(projectTool);
        // Caller is expected to prompt; deterministic path applies the project tool.
        assertEquals(Outcome.APPLY_PROJECT_TOOL, result.outcome());
        assertNotNull(result.toolToApply());
    }

    @Test
    public void unknownIdDefaultsToApplyProjectTool() {
        ToolDefinition unknown = new ToolDefinition();
        unknown.setId("project-only-id");
        unknown.setName("Project-only");
        Result result = sync.resolveDeterministic(unknown);
        assertEquals(Outcome.APPLY_PROJECT_TOOL, result.outcome());
        assertEquals("project-only-id", result.toolToApply().getId());
    }
}
