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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.DefaultToolSeeds;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Regression tests for two bugs introduced with the Tool Library:
 *   1. Selecting the "Custom" sentinel produced no visible feedback and (combined with #2) left
 *      the user with no way to recover state.
 *   2. Once an imperial tool was picked, the diameter field was locked in inches — there was no
 *      UI control to switch it back to mm.
 *
 * The fix adds a MM/INCH unit combo next to the diameter field. These tests verify the combo
 * stays in sync with the picked tool, that toggling it converts values cleanly, and that picking
 * "Custom" preserves the user's field values while still clearing the library binding.
 */
public class ToolSettingsPanelTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Controller controller;
    private ToolSettingsPanel panel;

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Path libraryPath = tempFolder.newFolder().toPath().resolve("tool-library.json");
        LookupService.remove(ToolLibraryService.class);
        LookupService.register(new ToolLibraryService(libraryPath));
        controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        panel = new ToolSettingsPanel(controller);
    }

    @After
    public void tearDown() {
        LookupService.remove(ToolLibraryService.class);
    }

    @Test
    public void initialStateIsMetric() throws Exception {
        assertEquals(UnitUtils.Units.MM, getCombo().getSelectedItem());
    }

    @Test
    public void pickingImperialToolSwitchesComboToInch() throws Exception {
        panel.selectTool(quarterInchUpcut());

        assertEquals(UnitUtils.Units.INCH, getCombo().getSelectedItem());
        assertEquals(0.25, getDiameterField().getDoubleValue(), 1e-4);
        assertEquals(0.25 * UnitUtils.scaleUnits(UnitUtils.Units.INCH, UnitUtils.Units.MM),
                panel.getToolDiameter(), 1e-3);
    }

    @Test
    public void changingComboToMmAfterImperialPickConvertsValue() throws Exception {
        panel.selectTool(quarterInchUpcut());
        double mmValue = 0.25 * UnitUtils.scaleUnits(UnitUtils.Units.INCH, UnitUtils.Units.MM);

        getCombo().setSelectedItem(UnitUtils.Units.MM);

        assertEquals(UnitUtils.Units.MM, getCombo().getSelectedItem());
        assertEquals(mmValue, getDiameterField().getDoubleValue(), 1e-3);
        assertEquals(mmValue, panel.getToolDiameter(), 1e-3);
    }

    @Test
    public void changingComboBackToInchRestoresValue() throws Exception {
        panel.selectTool(quarterInchUpcut());

        getCombo().setSelectedItem(UnitUtils.Units.MM);
        getCombo().setSelectedItem(UnitUtils.Units.INCH);

        assertEquals(UnitUtils.Units.INCH, getCombo().getSelectedItem());
        assertEquals(0.25, getDiameterField().getDoubleValue(), 1e-4);
        assertEquals(0.25 * UnitUtils.scaleUnits(UnitUtils.Units.INCH, UnitUtils.Units.MM),
                panel.getToolDiameter(), 1e-3);
    }

    @Test
    public void pickingCustomPreservesFieldsAndComboState() throws Exception {
        panel.selectTool(quarterInchUpcut());
        Settings beforeCustom = panel.getSettings();

        panel.selectTool(DefaultToolSeeds.createCustomSentinel());

        Settings afterCustom = panel.getSettings();
        assertNull(afterCustom.getCurrentToolId());
        assertNull(afterCustom.getCurrentToolSnapshot());
        assertEquals(UnitUtils.Units.INCH, getCombo().getSelectedItem());
        assertEquals(beforeCustom.getFeedSpeed(), afterCustom.getFeedSpeed());
        assertEquals(beforeCustom.getPlungeSpeed(), afterCustom.getPlungeSpeed());
        assertEquals(beforeCustom.getDepthPerPass(), afterCustom.getDepthPerPass(), 1e-6);
        assertEquals(beforeCustom.getToolStepOver(), afterCustom.getToolStepOver(), 1e-6);
        assertEquals(beforeCustom.getMaxSpindleSpeed(), afterCustom.getMaxSpindleSpeed());
        assertEquals(beforeCustom.getToolDiameter(), afterCustom.getToolDiameter(), 1e-6);
        assertEquals("— Custom —", getSelectedToolLabel().getText());
    }

    @Test
    public void selectingCustomThenSwitchingComboToMmEnablesMetricFlow() throws Exception {
        panel.selectTool(quarterInchUpcut());
        panel.selectTool(DefaultToolSeeds.createCustomSentinel());
        double mmValue = 0.25 * UnitUtils.scaleUnits(UnitUtils.Units.INCH, UnitUtils.Units.MM);

        getCombo().setSelectedItem(UnitUtils.Units.MM);

        assertEquals(UnitUtils.Units.MM, getCombo().getSelectedItem());
        assertEquals(mmValue, getDiameterField().getDoubleValue(), 1e-3);
        assertEquals(mmValue, panel.getToolDiameter(), 1e-3);
    }

    private ToolDefinition quarterInchUpcut() {
        List<ToolDefinition> seeds = DefaultToolSeeds.create();
        return seeds.stream()
                .filter(t -> "builtin:upcut:1_4in".equals(t.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("seed 1/4\" Upcut missing"));
    }

    @SuppressWarnings("unchecked")
    private JComboBox<UnitUtils.Units> getCombo() throws Exception {
        return (JComboBox<UnitUtils.Units>) readField("diameterUnitCombo");
    }

    private TextFieldWithUnit getDiameterField() throws Exception {
        TextFieldWithUnit field = (TextFieldWithUnit) readField("toolDiameter");
        assertNotNull(field);
        return field;
    }

    private JLabel getSelectedToolLabel() throws Exception {
        return (JLabel) readField("selectedToolLabel");
    }

    private Object readField(String name) throws Exception {
        Field field = ToolSettingsPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(panel);
    }
}
