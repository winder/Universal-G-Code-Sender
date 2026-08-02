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
import org.junit.Test;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ToolListCellRendererTest {
    private static final int TOOL_NUMBER = 0;
    private static final int NAME = 2;

    @Test
    public void render_ShouldShowToolNumberForNumberedTool() {
        ToolDefinition tool = millimeterUpcut();
        tool.setToolNumber(2);

        List<JLabel> labels = render(tool);

        assertEquals("T2", labels.get(TOOL_NUMBER).getText());
    }

    @Test
    public void render_ShouldLeaveToolNumberEmptyWhenUnassigned() {
        ToolDefinition tool = millimeterUpcut();

        List<JLabel> labels = render(tool);

        assertEquals("", labels.get(TOOL_NUMBER).getText());
    }

    @Test
    public void render_ShouldKeepNameColumnAlignedWhetherOrNotToolIsNumbered() {
        ToolDefinition numbered = millimeterUpcut();
        numbered.setToolNumber(2);

        int numberedX = render(numbered).get(NAME).getX();
        int unnumberedX = render(millimeterUpcut()).get(NAME).getX();

        assertEquals(numberedX, unnumberedX);
    }

    @Test
    public void render_ShouldShowTheShapeIcon() {
        List<JLabel> labels = render(millimeterUpcut());

        assertNotNull(labels.get(1).getIcon());
    }

    @Test
    public void render_ShouldShowTheName() {
        List<JLabel> labels = render(millimeterUpcut());

        assertEquals("3 mm Upcut", labels.get(NAME).getText());
    }

    @Test
    public void render_ShouldNotIndentAFocusedRow() {
        JList<ToolDefinition> list = listContaining(millimeterUpcut());
        ToolListCellRenderer renderer = new ToolListCellRenderer();

        Insets unfocused = ((JComponent) renderer.getListCellRendererComponent(
                list, millimeterUpcut(), 0, false, false)).getInsets();
        Insets focused = ((JComponent) renderer.getListCellRendererComponent(
                list, millimeterUpcut(), 0, true, true)).getInsets();

        assertEquals("A focused row must not shift its contents", unfocused, focused);
    }

    @Test
    public void render_ShouldRenderTheToolOnASingleLine() {
        List<JLabel> labels = render(millimeterUpcut());

        assertEquals("Expected only the number, icon and name", 3, labels.size());
    }

    @Test
    public void render_ShouldUseSelectionColorsWhenSelected() {
        JList<ToolDefinition> list = listContaining(millimeterUpcut());
        ToolListCellRenderer renderer = new ToolListCellRenderer();

        Component component = renderer.getListCellRendererComponent(
                list, millimeterUpcut(), 0, true, false);

        assertEquals(list.getSelectionBackground(), component.getBackground());
    }

    private static List<JLabel> render(ToolDefinition tool) {
        JList<ToolDefinition> list = listContaining(tool);
        ToolListCellRenderer renderer = new ToolListCellRenderer();

        Component component = renderer.getListCellRendererComponent(list, tool, 0, false, false);
        component.setSize(component.getPreferredSize());
        layoutRecursively((JComponent) component);
        return labelsOf((Container) component);
    }

    private static JList<ToolDefinition> listContaining(ToolDefinition tool) {
        DefaultListModel<ToolDefinition> model = new DefaultListModel<>();
        model.addElement(tool);
        JList<ToolDefinition> list = new JList<>(model);
        list.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        return list;
    }

    private static void layoutRecursively(JComponent component) {
        component.doLayout();
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent jComponent) {
                layoutRecursively(jComponent);
            }
        }
    }

    private static List<JLabel> labelsOf(Container container) {
        List<JLabel> labels = new ArrayList<>();
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel label) {
                labels.add(label);
            }
        }
        return labels;
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
}
