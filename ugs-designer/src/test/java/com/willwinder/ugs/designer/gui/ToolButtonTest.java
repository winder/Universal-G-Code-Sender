/*
    Copyright 2026 Will Winder

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
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ToolButtonTest {

    private Controller controller;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        controller = new Controller(new SelectionManager(), new SimpleUndoManager());
    }

    @Test
    public void laserIconShouldBeLoadable() {
        assertTrue(SvgIconLoader.loadImageIcon(ToolButton.LASER_ICON_PATH, 16).isPresent());
        assertTrue(SvgIconLoader.loadImageIcon(
                ToolButton.LASER_ICON_PATH.replace(".svg", "_dark.svg"), 16).isPresent());
    }

    @Test
    public void millDesignShouldShowTheEndmillIcon() {
        ToolButton button = new ToolButton(controller);

        assertNotNull(getIcon(button));
    }

    @Test
    public void laserDesignShouldShowTheLaserIcon() {
        addEntity(CutType.LASER_ON_PATH);

        ToolButton button = new ToolButton(controller);

        assertEquals("Laser", getText(button));
        assertNotNull(getIcon(button));
    }

    @Test
    public void mixedDesignShouldShowNoIcon() {
        addEntity(CutType.LASER_ON_PATH);
        addEntity(CutType.POCKET);

        ToolButton button = new ToolButton(controller);

        assertEquals("Mixed", getText(button));
        assertNull(getIcon(button));
    }

    @Test
    public void iconShouldBePlacedLeftOfTheTexts() {
        ToolButton button = new ToolButton(controller);
        button.setSize(120, 48);
        layoutRecursively(button);

        java.awt.Rectangle icon = boundsInButton(button, labelAt(button, 0));
        assertTrue(icon.x + icon.width <= boundsInButton(button, labelAt(button, 1)).x);
        assertTrue(icon.x + icon.width <= boundsInButton(button, labelAt(button, 2)).x);
    }

    private void addEntity(CutType cutType) {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setCutType(cutType);
        controller.getDrawing().insertEntity(rectangle);
    }

    private JLabel labelAt(ToolButton button, int index) {
        List<JLabel> labels = new ArrayList<>();
        collectLabels(button, labels);
        return labels.get(index);
    }

    private void collectLabels(Container container, List<JLabel> labels) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label) {
                labels.add(label);
            } else if (component instanceof Container child) {
                collectLabels(child, labels);
            }
        }
    }

    private java.awt.Rectangle boundsInButton(ToolButton button, JLabel label) {
        return SwingUtilities.convertRectangle(label.getParent(), label.getBounds(), button);
    }

    private void layoutRecursively(Component component) {
        component.doLayout();
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                layoutRecursively(child);
            }
        }
    }

    private Icon getIcon(ToolButton button) {
        return labelAt(button, 0).getIcon();
    }

    private String getText(ToolButton button) {
        return labelAt(button, 2).getText();
    }
}
