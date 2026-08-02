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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Regression guard for the tool number carrying over between tools. Typing into the spinner used
 * to stay uncommitted until the field lost focus, which Swing delivers after the list selection
 * has already moved on — so the pending number landed on the newly selected tool. Every unassigned
 * tool shares the number zero, so the spinner model saw no change on rebind and left the typed
 * text in place.
 */
public class ToolEditorPanelToolNumberTest {

    private ToolEditorPanel panel;
    private JSpinner toolNumberSpinner;
    private AtomicInteger fireCount;
    private AtomicReference<ToolDefinition> lastEdit;
    private ToolDefinition toolA;
    private ToolDefinition toolB;

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        panel = new ToolEditorPanel(UnitUtils.Units.MM);
        toolNumberSpinner = readField("toolNumberSpinner");

        fireCount = new AtomicInteger();
        lastEdit = new AtomicReference<>();
        panel.setChangeListener(t -> {
            fireCount.incrementAndGet();
            lastEdit.set(t);
        });

        toolA = unassignedTool("tool-a", "Tool A");
        toolB = unassignedTool("tool-b", "Tool B");
        panel.setTool(toolA, false);
        resetListener();
    }

    @Test
    public void toolNumberEditor_ShouldBeLeftAligned() {
        assertEquals(JTextField.LEFT, editorField().getHorizontalAlignment());
    }

    @Test
    public void typeToolNumber_ShouldCommitImmediately() throws Exception {
        typeToolNumber("5");

        assertEquals(1, fireCount.get());
        assertNotNull(lastEdit.get());
        assertEquals("tool-a", lastEdit.get().getId());
        assertEquals(5, lastEdit.get().getToolNumber());
    }

    @Test
    public void setTool_ShouldReplaceTheEditorTextOfAnEquallyNumberedTool() throws Exception {
        typeToolNumber("5");
        resetListener();

        panel.setTool(toolB, false);

        assertEquals("0", editorText());
    }

    @Test
    public void setTool_ShouldNotFireAChangeForTheNewlyBoundTool() throws Exception {
        typeToolNumber("5");
        resetListener();

        panel.setTool(toolB, false);

        assertEquals(0, fireCount.get());
    }

    @Test
    public void commitAfterSwitchingTool_ShouldNotNumberTheNewlyBoundTool() throws Exception {
        typeToolNumber("5");
        panel.setTool(toolB, false);
        resetListener();

        editorField().commitEdit();

        assertEquals(0, fireCount.get());
        assertEquals(ToolDefinition.UNASSIGNED_TOOL_NUMBER, panel.getTool().getToolNumber());
        assertEquals("tool-b", panel.getTool().getId());
    }

    @Test
    public void typeToolNumberAfterSwitching_ShouldApplyToTheNewlyBoundTool() throws Exception {
        typeToolNumber("5");
        panel.setTool(toolB, false);
        resetListener();

        typeToolNumber("7");

        assertEquals("tool-b", lastEdit.get().getId());
        assertEquals(7, lastEdit.get().getToolNumber());
    }

    private void typeToolNumber(String text) throws Exception {
        JFormattedTextField field = editorField();
        field.getDocument().remove(0, field.getDocument().getLength());
        field.getDocument().insertString(0, text, null);
    }

    private String editorText() {
        return editorField().getText();
    }

    private JFormattedTextField editorField() {
        return ((JSpinner.DefaultEditor) toolNumberSpinner.getEditor()).getTextField();
    }

    private void resetListener() {
        fireCount.set(0);
        lastEdit.set(null);
    }

    private static ToolDefinition unassignedTool(String id, String name) {
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

    @SuppressWarnings("unchecked")
    private <T> T readField(String name) throws Exception {
        Field field = ToolEditorPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(panel);
    }
}
