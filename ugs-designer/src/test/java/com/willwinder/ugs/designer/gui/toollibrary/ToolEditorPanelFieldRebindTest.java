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

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Regression guard for uncommitted numeric text carrying over between tools. The fields are
 * rebound by value, and a value that has not changed leaves the displayed text untouched — so a
 * half-typed number survived the switch to a tool that happened to share that value, and the
 * deferred focus-lost commit then wrote it onto the newly bound tool. Tools routinely share the
 * seeded feed, plunge and spindle speeds, so the collision is common rather than exotic.
 */
public class ToolEditorPanelFieldRebindTest {

    private ToolEditorPanel panel;
    private AtomicInteger fireCount;
    private AtomicReference<ToolDefinition> lastEdit;
    private ToolDefinition toolA;
    private ToolDefinition toolB;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        panel = new ToolEditorPanel(UnitUtils.Units.MM);

        fireCount = new AtomicInteger();
        lastEdit = new AtomicReference<>();
        panel.setChangeListener(t -> {
            fireCount.incrementAndGet();
            lastEdit.set(t);
        });

        toolA = tool("tool-a", "Tool A");
        toolB = tool("tool-b", "Tool B");
        panel.setTool(toolA, false);
    }

    @Test
    public void setTool_ShouldReplaceUncommittedFeedText() throws Exception {
        type("feedField", "1200");

        panel.setTool(toolB, false);

        assertFalse("Tool A's typed feed must not survive the switch",
                text("feedField").contains("1200"));
    }

    @Test
    public void commitAfterSwitching_ShouldNotChangeTheNewToolsFeed() throws Exception {
        type("feedField", "1200");
        panel.setTool(toolB, false);
        fireCount.set(0);

        field("feedField").commitEdit();

        assertEquals(0, fireCount.get());
        assertEquals(900, panel.getTool().getFeedSpeed());
        assertEquals("tool-b", panel.getTool().getId());
    }

    @Test
    public void commitAfterSwitching_ShouldNotChangeTheNewToolsSpindleSpeed() throws Exception {
        type("spindleSpeedField", "24000");
        panel.setTool(toolB, false);
        fireCount.set(0);

        field("spindleSpeedField").commitEdit();

        assertEquals(0, fireCount.get());
        assertEquals(18000, panel.getTool().getMaxSpindleSpeed());
    }

    @Test
    public void setTool_ShouldStillShowTheBoundToolsValues() throws Exception {
        ToolDefinition faster = tool("tool-c", "Tool C");
        faster.setFeedSpeed(1500);

        panel.setTool(faster, false);

        assertEquals(1500, panel.getTool().getFeedSpeed());
        assertEquals(0, fireCount.get());
    }

    private void type(String fieldName, String value) throws Exception {
        TextFieldWithUnit textField = field(fieldName);
        textField.getDocument().remove(0, textField.getDocument().getLength());
        textField.getDocument().insertString(0, value, null);
    }

    private String text(String fieldName) throws Exception {
        return field(fieldName).getText();
    }

    private TextFieldWithUnit field(String name) throws Exception {
        Field field = ToolEditorPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (TextFieldWithUnit) field.get(panel);
    }

    private static ToolDefinition tool(String id, String name) {
        ToolDefinition tool = new ToolDefinition();
        tool.setId(id);
        tool.setName(name);
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
