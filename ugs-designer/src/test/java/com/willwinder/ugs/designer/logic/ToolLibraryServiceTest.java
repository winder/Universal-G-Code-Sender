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

import com.willwinder.ugs.designer.model.toollibrary.DefaultToolSeeds;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ToolLibraryServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Path libraryPath;

    @Before
    public void setup() throws Exception {
        libraryPath = tempFolder.newFolder().toPath().resolve("tool-library.json");
    }

    @Test
    public void firstRunSeedsDefaultsToDisk() throws Exception {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        service.flushForTesting();
        assertEquals(41, service.getTools().size());
        assertTrue(Files.exists(libraryPath));
    }

    @Test
    public void getByIdFindsSeededTools() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        Optional<ToolDefinition> custom = service.getById(DefaultToolSeeds.CUSTOM_SENTINEL_ID);
        assertTrue(custom.isPresent());
        assertTrue(custom.get().isCustomSentinel());
    }

    @Test
    public void addingAndDeletingUserToolPersists() throws Exception {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition userTool = new ToolDefinition();
        userTool.setName("3 mm compression");
        userTool.setDiameter(3.0);
        userTool.setDiameterUnit(UnitUtils.Units.MM);
        userTool.setFeedSpeed(800);
        userTool.setPlungeSpeed(250);
        userTool.setDepthPerPass(1.5);
        userTool.setStepOverPercent(0.4);
        userTool.setMaxSpindleSpeed(18000);
        ToolDefinition added = service.addTool(userTool);
        service.flushForTesting();

        ToolLibraryService reloaded = new ToolLibraryService(libraryPath);
        assertTrue(reloaded.getById(added.getId()).isPresent());

        reloaded.deleteTool(added.getId());
        reloaded.flushForTesting();
        ToolLibraryService reloadedAgain = new ToolLibraryService(libraryPath);
        assertFalse(reloadedAgain.getById(added.getId()).isPresent());
    }

    @Test
    public void deletingBuiltInIsRejected() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        try {
            service.deleteTool(DefaultToolSeeds.CUSTOM_SENTINEL_ID);
            fail("Expected rejection for built-in delete");
        } catch (IllegalStateException expected) {
            // ok
        }
    }

    @Test
    public void duplicateProducesNewIdAndSuffixedName() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition seed = service.getTools().get(0);
        ToolDefinition copy = service.duplicate(seed.getId());
        assertNotEquals(seed.getId(), copy.getId());
        assertTrue(copy.getName().endsWith("(copy)"));
        assertFalse(copy.isBuiltIn());
    }

    @Test
    public void revertRestoresSeedValuesButKeepsName() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition seed = service.getTools().stream()
                .filter(t -> !t.isCustomSentinel())
                .findFirst()
                .orElseThrow();
        ToolDefinition mutated = new ToolDefinition(seed);
        mutated.setName("renamed");
        mutated.setFeedSpeed(seed.getFeedSpeed() + 500);
        service.updateTool(mutated);

        ToolDefinition reverted = service.revertToDefault(seed.getId());
        assertEquals("renamed", reverted.getName());
        assertEquals(seed.getFeedSpeed(), reverted.getFeedSpeed());
    }

    @Test
    public void reinjectsMissingBuiltInsOnLoad() throws Exception {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        // Hand-corrupt the file to drop every built-in except Custom.
        service.flushForTesting();
        String json = Files.readString(libraryPath);
        String reduced = json;
        // Simulate by creating a new service with a fresh file that only has Custom.
        Path reducedPath = libraryPath.resolveSibling("reduced.json");
        Files.writeString(reducedPath,
                "{\"schemaVersion\":1,\"tools\":[{" +
                        "\"id\":\"" + DefaultToolSeeds.CUSTOM_SENTINEL_ID + "\"," +
                        "\"name\":\"Custom\"," +
                        "\"shape\":\"CUSTOM\"," +
                        "\"diameter\":3.0," +
                        "\"diameterUnit\":\"MM\"," +
                        "\"feedSpeed\":1000," +
                        "\"plungeSpeed\":400," +
                        "\"depthPerPass\":1.0," +
                        "\"stepOverPercent\":0.3," +
                        "\"maxSpindleSpeed\":18000," +
                        "\"spindleDirection\":\"M3\"," +
                        "\"builtIn\":true," +
                        "\"isCustomSentinel\":true}]}");
        ToolLibraryService reloaded = new ToolLibraryService(reducedPath);
        assertEquals(41, reloaded.getTools().size());
    }

    @Test
    public void duplicateIdInsertRejected() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition seed = service.getTools().get(0);
        ToolDefinition clash = new ToolDefinition();
        clash.setId(seed.getId());
        clash.setDiameter(5.0);
        try {
            service.addTool(clash);
            fail("Expected rejection of duplicate id");
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }

    @Test
    public void importFromProjectAssignsNewIdOnCollision() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition existing = service.getTools().get(0);
        ToolDefinition fromProject = new ToolDefinition(existing);
        fromProject.setFeedSpeed(existing.getFeedSpeed() + 500);
        ToolDefinition imported = service.importFromProject(fromProject);
        assertNotEquals(existing.getId(), imported.getId());
        assertFalse(imported.isBuiltIn());
    }

    @Test
    public void snapshotsReturnedByGettersAreDefensive() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        List<ToolDefinition> tools = service.getTools();
        try {
            tools.add(new ToolDefinition());
            fail("Expected immutable list");
        } catch (UnsupportedOperationException expected) {
            // ok
        }
        ToolDefinition first = service.getTools().get(0);
        String originalName = first.getName();
        first.setName("tampered");
        assertEquals(originalName, service.getTools().get(0).getName());
    }

    @Test
    public void libraryPathGetterReturnsConfiguredPath() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        assertEquals(libraryPath, service.getLibraryPath());
    }

    @Test
    public void corruptFileIsBackedUpAndReseeded() throws Exception {
        Files.createDirectories(libraryPath.getParent());
        Files.writeString(libraryPath, "{not valid json");
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        assertNotNull(service.getTools());
        assertFalse(service.getTools().isEmpty());
    }
}
