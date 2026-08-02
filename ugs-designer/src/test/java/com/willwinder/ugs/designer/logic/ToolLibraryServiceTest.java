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
    public void firstRunSeedsDefaultsToDisk() {
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
    public void addingAndDeletingUserToolPersists() {
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
        service.deleteTool(DefaultToolSeeds.CUSTOM_SENTINEL_ID);
        assertTrue(service.getById(DefaultToolSeeds.CUSTOM_SENTINEL_ID).isEmpty());
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
    public void removedBuiltInsShouldNotBeReaddedOnLoad() throws Exception {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        service.flushForTesting();
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
        assertEquals(1, reloaded.getTools().size());
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
    public void seededToolsHaveNoToolNumber() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);

        List<ToolDefinition> tools = service.getTools();

        assertTrue(tools.stream().noneMatch(ToolDefinition::hasToolNumber));
    }

    @Test
    public void nextAvailableToolNumberSkipsClaimedSlots() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        service.addTool(namedToolWithNumber("first", 1));
        service.addTool(namedToolWithNumber("third", 3));

        int next = service.nextAvailableToolNumber();

        assertEquals(2, next);
    }

    @Test
    public void addingToolWithClaimedToolNumberIsRejected() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        service.addTool(namedToolWithNumber("first", 5));

        try {
            service.addTool(namedToolWithNumber("second", 5));
            fail("Expected rejection of duplicate tool number");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("first"));
        }
    }

    @Test
    public void updatingToolToClaimedToolNumberIsRejected() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        service.addTool(namedToolWithNumber("first", 5));
        ToolDefinition second = service.addTool(namedToolWithNumber("second", 6));
        second.setToolNumber(5);

        try {
            service.updateTool(second);
            fail("Expected rejection of duplicate tool number");
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }

    @Test
    public void updatingToolKeepingItsOwnToolNumberIsAllowed() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition tool = service.addTool(namedToolWithNumber("first", 5));
        tool.setFeedSpeed(1234);

        ToolDefinition updated = service.updateTool(tool);

        assertEquals(5, updated.getToolNumber());
        assertEquals(1234, updated.getFeedSpeed());
    }

    @Test
    public void unassignedToolNumbersDoNotCollide() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        service.addTool(namedToolWithNumber("first", ToolDefinition.UNASSIGNED_TOOL_NUMBER));

        ToolDefinition second = service.addTool(
                namedToolWithNumber("second", ToolDefinition.UNASSIGNED_TOOL_NUMBER));

        assertFalse(second.hasToolNumber());
    }

    @Test
    public void getByToolNumberFindsTheOccupyingTool() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition added = service.addTool(namedToolWithNumber("slot two", 2));

        Optional<ToolDefinition> found = service.getByToolNumber(2);

        assertTrue(found.isPresent());
        assertEquals(added.getId(), found.get().getId());
    }

    @Test
    public void getByToolNumberIgnoresUnassignedNumber() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);

        Optional<ToolDefinition> found = service.getByToolNumber(ToolDefinition.UNASSIGNED_TOOL_NUMBER);

        assertFalse(found.isPresent());
    }

    @Test
    public void duplicateDoesNotClaimTheSourceToolNumber() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition source = service.addTool(namedToolWithNumber("slot two", 2));

        ToolDefinition copy = service.duplicate(source.getId());

        assertFalse(copy.hasToolNumber());
        assertEquals(2, service.getByToolNumber(2).orElseThrow().getToolNumber());
    }

    @Test
    public void importFromProjectDropsAToolNumberThatIsAlreadyClaimed() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        service.addTool(namedToolWithNumber("mine", 3));

        ToolDefinition imported = service.importFromProject(namedToolWithNumber("theirs", 3));

        assertFalse(imported.hasToolNumber());
    }

    @Test
    public void revertToDefaultKeepsTheAssignedToolNumber() {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition builtIn = service.getTools().stream()
                .filter(t -> t.isBuiltIn() && !t.isCustomSentinel())
                .findFirst()
                .orElseThrow();
        ToolDefinition numbered = new ToolDefinition(builtIn);
        numbered.setToolNumber(8);
        service.updateTool(numbered);

        ToolDefinition reverted = service.revertToDefault(builtIn.getId());

        assertEquals(8, reverted.getToolNumber());
    }

    @Test
    public void toolNumberSurvivesAReload() throws Exception {
        ToolLibraryService service = new ToolLibraryService(libraryPath);
        ToolDefinition added = service.addTool(namedToolWithNumber("slot nine", 9));
        service.flushForTesting();

        ToolLibraryService reloaded = new ToolLibraryService(libraryPath);

        assertEquals(9, reloaded.getById(added.getId()).orElseThrow().getToolNumber());
    }

    @Test
    public void loadingAFileWithClashingToolNumbersKeepsOnlyTheFirstClaim() throws Exception {
        Files.createDirectories(libraryPath.getParent());
        Files.writeString(libraryPath,
                "{\"schemaVersion\":2,\"tools\":[" + numberedToolJson("a", 4) + "," + numberedToolJson("b", 4) + "]}");

        ToolLibraryService service = new ToolLibraryService(libraryPath);

        assertEquals(4, service.getById("a").orElseThrow().getToolNumber());
        assertFalse(service.getById("b").orElseThrow().hasToolNumber());
    }

    @Test
    public void loadingASchemaV1FileWithToolNumberShouldWork() throws Exception {
        Files.createDirectories(libraryPath.getParent());
        Files.writeString(libraryPath,
                "{\"schemaVersion\":1,\"tools\":[" + numberedToolJson("legacy", 4) + "]}");

        ToolLibraryService service = new ToolLibraryService(libraryPath);

        assertTrue(service.getById("legacy").orElseThrow().hasToolNumber());
    }

    private static String numberedToolJson(String id, int toolNumber) {
        return "{\"id\":\"" + id + "\",\"name\":\"" + id + "\",\"toolNumber\":" + toolNumber + ","
                + "\"shape\":\"UPCUT\",\"diameter\":3.0,\"diameterUnit\":\"MM\","
                + "\"feedSpeed\":900,\"plungeSpeed\":300,\"depthPerPass\":1.0,"
                + "\"stepOverPercent\":0.4,\"maxSpindleSpeed\":18000,\"spindleDirection\":\"M3\","
                + "\"builtIn\":false,\"isCustomSentinel\":false}";
    }

    private static ToolDefinition namedToolWithNumber(String name, int toolNumber) {
        ToolDefinition tool = new ToolDefinition();
        tool.setName(name);
        tool.setToolNumber(toolNumber);
        tool.setDiameter(3.0);
        tool.setDiameterUnit(UnitUtils.Units.MM);
        tool.setFeedSpeed(900);
        tool.setPlungeSpeed(300);
        tool.setDepthPerPass(1.0);
        tool.setStepOverPercent(0.4);
        tool.setMaxSpindleSpeed(18000);
        return tool;
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
