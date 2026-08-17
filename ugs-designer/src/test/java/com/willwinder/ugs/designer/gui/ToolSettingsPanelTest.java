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
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.toollibrary.EndmillShapeCombo;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.CoolantMode;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.DefaultToolSeeds;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for two bugs introduced with the Tool Library:
 *   1. Selecting the "Custom" sentinel produced no visible feedback and (combined with #2) left
 *      the user with no way to recover state.
 *   2. Once an imperial tool was picked, the diameter field was locked in inches — there was no
 *      UI control to switch it back to mm.
 * <p/>
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
    public void newPanelShouldKeepDesignDiameterWhenBoundToLibraryTool() throws Exception {
        ToolDefinition libraryTool = quarterInchUpcut();
        controller.getSettings().setCurrentToolId(libraryTool.getId());
        controller.getSettings().setCurrentToolSnapshot(libraryTool);
        controller.getSettings().setToolDiameter(1.0);

        ToolSettingsPanel reopenedPanel = new ToolSettingsPanel(controller);

        assertEquals(1.0, readField(reopenedPanel, TextFieldWithUnit.class, "toolDiameter").getDoubleValue(), 1e-6);
        assertEquals(1.0, reopenedPanel.getToolDiameter(), 1e-6);
        assertEquals(libraryTool.getName(), readField(reopenedPanel, JLabel.class, "selectedToolLabel").getText());
        assertEquals(libraryTool.getId(), reopenedPanel.getSettings().getCurrentToolId());
    }

    @Test
    public void newPanelShouldKeepDesignFeedsAndSpeedsWhenBoundToLibraryTool() {
        ToolDefinition libraryTool = quarterInchUpcut();
        controller.getSettings().setCurrentToolId(libraryTool.getId());
        controller.getSettings().setCurrentToolSnapshot(libraryTool);
        controller.getSettings().setFeedSpeed(libraryTool.getFeedSpeed() + 250);
        controller.getSettings().setDepthPerPass(libraryTool.getDepthPerPass() + 1.5);

        ToolSettingsPanel reopenedPanel = new ToolSettingsPanel(controller);

        assertEquals(libraryTool.getFeedSpeed() + 250, reopenedPanel.getFeedSpeed());
        assertEquals(libraryTool.getDepthPerPass() + 1.5, reopenedPanel.getDepthPerPass(), 1e-6);
    }

    @Test
    public void initialShapeShouldComeFromSettings() {
        controller.getSettings().setToolShape(EndmillShape.BALL);

        ToolSettingsPanel reopenedPanel = new ToolSettingsPanel(controller);

        assertEquals(EndmillShape.BALL, reopenedPanel.getEndmillShape());
        assertEquals(EndmillShape.BALL, reopenedPanel.getSettings().getToolShape());
    }

    @Test
    public void initialCoolantModeShouldComeFromSettings() {
        controller.getSettings().setCoolantMode(CoolantMode.FLOOD);

        ToolSettingsPanel reopenedPanel = new ToolSettingsPanel(controller);

        assertEquals(CoolantMode.FLOOD, reopenedPanel.getSettings().getCoolantMode());
    }

    @Test
    public void coolantModeShouldDefaultToNone() {
        assertEquals(CoolantMode.NONE, panel.getSettings().getCoolantMode());
    }

    @Test
    public void pickingToolShouldApplyItsShape() {
        ToolDefinition libraryTool = quarterInchUpcut();

        panel.selectTool(libraryTool);

        assertEquals(libraryTool.getShape(), panel.getEndmillShape());
        assertEquals(libraryTool.getShape(), panel.getSettings().getToolShape());
    }

    @Test
    public void newPanelShouldKeepDesignShapeWhenBoundToLibraryTool() {
        ToolDefinition libraryTool = quarterInchUpcut();
        controller.getSettings().setCurrentToolId(libraryTool.getId());
        controller.getSettings().setCurrentToolSnapshot(libraryTool);
        controller.getSettings().setToolShape(EndmillShape.DOWNCUT);

        ToolSettingsPanel reopenedPanel = new ToolSettingsPanel(controller);

        assertEquals(EndmillShape.DOWNCUT, reopenedPanel.getEndmillShape());
    }

    @Test
    public void vBitAngleShouldBeHiddenForNonVBitShape() throws Exception {
        getShapeCombo().setSelectedItem(EndmillShape.UPCUT);

        assertFalse(getVBitAngleLabel().isVisible());
        assertFalse(getVBitAngleField().isVisible());
    }

    @Test
    public void vBitAngleShouldBeVisibleForVBitShape() throws Exception {
        getShapeCombo().setSelectedItem(EndmillShape.V_BIT);

        assertTrue(getVBitAngleLabel().isVisible());
        assertTrue(getVBitAngleField().isVisible());
    }

    @Test
    public void vBitAngleShouldBeHiddenOnCreationWhenSettingsHoldNonVBitShape() throws Exception {
        controller.getSettings().setToolShape(EndmillShape.BALL);

        ToolSettingsPanel reopenedPanel = new ToolSettingsPanel(controller);

        assertFalse(readField(reopenedPanel, JLabel.class, "vBitAngleLabel").isVisible());
        assertFalse(readField(reopenedPanel, TextFieldWithUnit.class, "vBitAngle").isVisible());
    }

    @Test
    public void vBitAngleShouldBeVisibleOnCreationWhenSettingsHoldVBitShape() throws Exception {
        controller.getSettings().setToolShape(EndmillShape.V_BIT);

        ToolSettingsPanel reopenedPanel = new ToolSettingsPanel(controller);

        assertTrue(readField(reopenedPanel, JLabel.class, "vBitAngleLabel").isVisible());
        assertTrue(readField(reopenedPanel, TextFieldWithUnit.class, "vBitAngle").isVisible());
    }

    @Test
    public void hidingVBitAngleShouldKeepItsValue() throws Exception {
        getShapeCombo().setSelectedItem(EndmillShape.V_BIT);
        getVBitAngleField().setDoubleValue(30);

        getShapeCombo().setSelectedItem(EndmillShape.UPCUT);

        assertEquals(30, panel.getVBitAngle(), 1e-6);
        assertEquals(30, panel.getSettings().getVBitAngle(), 1e-6);
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

    private EndmillShapeCombo getShapeCombo() throws Exception {
        return readField(panel, EndmillShapeCombo.class, "endmillShape");
    }

    private JLabel getVBitAngleLabel() throws Exception {
        return readField(panel, JLabel.class, "vBitAngleLabel");
    }

    private TextFieldWithUnit getVBitAngleField() throws Exception {
        return readField(panel, TextFieldWithUnit.class, "vBitAngle");
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
        return readField(panel, Object.class, name);
    }

    private <T> T readField(ToolSettingsPanel target, Class<T> type, String name) throws Exception {
        Field field = ToolSettingsPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}
