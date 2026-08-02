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

import com.willwinder.ugs.designer.model.toollibrary.DefaultToolSeeds;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JComponent;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Regression guard for the diameter field staying editable in a disabled panel. The field is
 * rebuilt to match the tool's unit after the fields have been disabled, and a freshly constructed
 * component is enabled by default — so the read-only preview showed one live field among the
 * greyed-out ones.
 */
public class ToolEditorPanelReadOnlyTest {

    private ToolEditorPanel panel;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        panel = new ToolEditorPanel(UnitUtils.Units.MM);
    }

    @Test
    public void setTool_ShouldDisableDiameterWhenReadOnly() throws Exception {
        panel.setTool(millimeterUpcut(), true);

        assertFalse(readField("diameterField").isEnabled());
    }

    @Test
    public void setTool_ShouldDisableDiameterForTheCustomSentinel() throws Exception {
        panel.setTool(DefaultToolSeeds.createCustomSentinel(), false);

        assertFalse(readField("diameterField").isEnabled());
    }

    @Test
    public void setTool_ShouldDisableDiameterAlongsideTheOtherFields() throws Exception {
        panel.setTool(millimeterUpcut(), true);

        assertFalse(readField("nameField").isEnabled());
        assertFalse(readField("feedField").isEnabled());
        assertFalse(readField("diameterField").isEnabled());
    }

    @Test
    public void setTool_ShouldEnableDiameterWhenEditable() throws Exception {
        panel.setTool(millimeterUpcut(), false);

        assertTrue(readField("diameterField").isEnabled());
    }

    @Test
    public void setTool_ShouldReenableDiameterWhenSwitchingFromAReadOnlyTool() throws Exception {
        panel.setTool(millimeterUpcut(), true);

        panel.setTool(millimeterUpcut(), false);

        assertTrue(readField("diameterField").isEnabled());
    }

    private static ToolDefinition millimeterUpcut() {
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

    private JComponent readField(String name) throws Exception {
        Field field = ToolEditorPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JComponent) field.get(panel);
    }
}
