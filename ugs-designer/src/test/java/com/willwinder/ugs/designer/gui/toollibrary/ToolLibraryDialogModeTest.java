/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The library dialog serves both managing the library and picking a tool. Both modes share the
 * same editable master-detail; only the bottom buttons and the returned result differ.
 */
public class ToolLibraryDialogModeTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private ToolLibraryService service;
    private String toolId;

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Path libraryPath = temp.newFolder().toPath().resolve("tools.json");
        service = new ToolLibraryService(libraryPath);
        toolId = service.addTool(buildTool()).getId();
    }

    @Test
    public void pickMode_ShouldOfferSelectAndCancel() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK);

        List<String> buttons = buttonTexts(dialog);

        assertTrue(buttons.contains("Select"));
        assertTrue(buttons.contains("Cancel"));
        assertFalse(buttons.contains("Close"));
    }

    @Test
    public void manageMode_ShouldOfferOnlyClose() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.MANAGE);

        List<String> buttons = buttonTexts(dialog);

        assertTrue(buttons.contains("Close"));
        assertFalse(buttons.contains("Select"));
    }

    @Test
    public void bothModes_ShouldOfferTheEditingActions() {
        List<String> pickButtons = buttonTexts(dialog(ToolLibraryDialog.Mode.PICK));
        List<String> manageButtons = buttonTexts(dialog(ToolLibraryDialog.Mode.MANAGE));

        for (String action : List.of("Add", "Duplicate", "Delete", "Revert")) {
            assertTrue("Pick mode is missing " + action, pickButtons.contains(action));
            assertTrue("Manage mode is missing " + action, manageButtons.contains(action));
        }
    }

    @Test
    public void pickMode_ShouldKeepTheSelectedToolEditable() throws Exception {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK);

        assertTrue(editorField(dialog, "diameterField").isEnabled());
        assertTrue(editorField(dialog, "nameField").isEnabled());
    }

    @Test
    public void open_ShouldPreselectTheGivenTool() {
        String lastToolId = service.addTool(buildTool()).getId();

        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK, lastToolId);

        assertEquals(lastToolId, selectedTool(dialog).getId());
    }

    @Test
    public void open_ShouldPreselectTheGivenToolWhenManaging() {
        String lastToolId = service.addTool(buildTool()).getId();

        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.MANAGE, lastToolId);

        assertEquals(lastToolId, selectedTool(dialog).getId());
    }

    @Test
    public void open_ShouldShowThePreselectedToolInTheEditor() {
        ToolDefinition named = buildTool();
        named.setName("Preselected");
        String namedId = service.addTool(named).getId();

        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK, namedId);

        ToolEditorPanel editor = readField(dialog, "editorPanel");
        assertEquals("Preselected", editor.getTool().getName());
    }

    @Test
    public void open_ShouldFallBackToTheFirstToolWhenNoneGiven() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK, null);

        javax.swing.JList<ToolDefinition> list = readField(dialog, "toolList");
        assertEquals(0, list.getSelectedIndex());
    }

    @Test
    public void open_ShouldFallBackToTheFirstToolWhenTheToolIsGone() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK, "no-such-tool");

        javax.swing.JList<ToolDefinition> list = readField(dialog, "toolList");
        assertEquals(0, list.getSelectedIndex());
    }

    @Test
    public void select_ShouldReturnTheSelectedTool() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK);
        selectToolUnderTest(dialog);

        click(dialog, "Select");

        Optional<ToolDefinition> result = dialog.getResult();
        assertTrue(result.isPresent());
        assertEquals(toolId, result.get().getId());
    }

    @Test
    public void select_ShouldReturnTheToolAsStoredInTheLibrary() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK);
        selectToolUnderTest(dialog);
        ToolDefinition renamed = service.getById(toolId).orElseThrow();
        renamed.setName("Edited while picking");
        service.updateTool(renamed);

        click(dialog, "Select");

        assertEquals("Edited while picking", dialog.getResult().orElseThrow().getName());
    }

    @Test
    public void cancel_ShouldReturnNoTool() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.PICK);
        selectToolUnderTest(dialog);

        click(dialog, "Cancel");

        assertTrue(dialog.getResult().isEmpty());
    }

    @Test
    public void close_ShouldReturnNoTool() {
        ToolLibraryDialog dialog = dialog(ToolLibraryDialog.Mode.MANAGE);

        click(dialog, "Close");

        assertTrue(dialog.getResult().isEmpty());
    }

    private ToolLibraryDialog dialog(ToolLibraryDialog.Mode mode) {
        return dialog(mode, null);
    }

    private ToolLibraryDialog dialog(ToolLibraryDialog.Mode mode, String selectedToolId) {
        return new ToolLibraryDialog(null, service, UnitUtils.Units.MM, mode, selectedToolId);
    }

    private static ToolDefinition selectedTool(ToolLibraryDialog dialog) {
        javax.swing.JList<ToolDefinition> list = readField(dialog, "toolList");
        return list.getSelectedValue();
    }

    private void selectToolUnderTest(ToolLibraryDialog dialog) {
        javax.swing.JList<ToolDefinition> list = readField(dialog, "toolList");
        for (int i = 0; i < list.getModel().getSize(); i++) {
            if (toolId.equals(list.getModel().getElementAt(i).getId())) {
                list.setSelectedIndex(i);
                return;
            }
        }
    }

    private static void click(ToolLibraryDialog dialog, String text) {
        buttons(dialog.getContentPane()).stream()
                .filter(b -> text.equals(b.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No button labelled " + text))
                .doClick();
    }

    private static List<String> buttonTexts(ToolLibraryDialog dialog) {
        return buttons(dialog.getContentPane()).stream().map(AbstractButton::getText).toList();
    }

    private static List<AbstractButton> buttons(Container container) {
        List<AbstractButton> found = new ArrayList<>();
        for (Component child : container.getComponents()) {
            if (child instanceof AbstractButton button) {
                found.add(button);
            }
            if (child instanceof Container nested) {
                found.addAll(buttons(nested));
            }
        }
        return found;
    }

    private JComponent editorField(ToolLibraryDialog dialog, String name) throws Exception {
        ToolEditorPanel panel = readField(dialog, "editorPanel");
        Field field = ToolEditorPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JComponent) field.get(panel);
    }

    @SuppressWarnings("unchecked")
    private static <T> T readField(ToolLibraryDialog dialog, String name) {
        try {
            Field field = ToolLibraryDialog.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(dialog);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static ToolDefinition buildTool() {
        ToolDefinition tool = new ToolDefinition();
        tool.setName("3 mm Upcut");
        tool.setShape(EndmillShape.UPCUT);
        tool.setDiameter(3.0);
        tool.setDiameterUnit(UnitUtils.Units.MM);
        tool.setFeedSpeed(900);
        tool.setPlungeSpeed(300);
        tool.setDepthPerPass(1.0);
        tool.setStepOverPercent(0.4);
        tool.setMaxSpindleSpeed(18000);
        return tool;
    }
}
