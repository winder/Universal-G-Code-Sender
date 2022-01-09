/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.core.ui.ToolBar;
import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.*;

import static org.openide.NotifyDescriptor.OK_OPTION;

/**
 * @author Joacim Breiler
 */
public class ToolBox extends ToolBar {

    public ToolBox(Controller controller) {
        setFloatable(false);

        JToggleButton select = new JToggleButton(new ToolSelectAction(controller));
        select.setSelected(true);
        select.setText("");
        select.setToolTipText("Select and move shapes");
        add(select);

        JToggleButton rectangle = new JToggleButton(new ToolDrawRectangleAction(controller));
        rectangle.setText("");
        rectangle.setToolTipText("Draw squares and rectangles");
        add(rectangle);

        JToggleButton circle = new JToggleButton(new ToolDrawCircleAction(controller));
        circle.setText("");
        circle.setToolTipText("Draw circles and ellipses");
        add(circle);

        JToggleButton text = new JToggleButton(new ToolDrawTextAction(controller));
        text.setText("");
        text.setToolTipText("Write text");
        add(text);

        JButton insert = new JButton(new ToolInsertAction(controller));
        insert.setText("");
        insert.setToolTipText("Inserts a drawing");
        insert.setContentAreaFilled(false);
        insert.setBorderPainted(false);
        add(insert);

        addSeparator();

        JButton flipHorizontal = new JButton(new FlipHorizontallyAction(controller));
        flipHorizontal.setText("");
        flipHorizontal.setToolTipText("Flips horizontally");
        flipHorizontal.setBorderPainted(false);
        add(flipHorizontal);

        JButton flipVertical = new JButton(new FlipVerticallyAction(controller));
        flipVertical.setText("");
        flipVertical.setToolTipText("Flips vertically");
        flipVertical.setBorderPainted(false);
        add(flipVertical);

        addSeparator();

        JButton union = new JButton(new UnionAction(controller));
        union.setText("");
        union.setToolTipText("Unions two or more entities with each other");
        union.setBorderPainted(false);
        add(union);

        JButton subtract = new JButton(new SubtractAction(controller));
        subtract.setText("");
        subtract.setToolTipText("Subtracts one entity with another");
        subtract.setBorderPainted(false);
        add(subtract);

        JButton intersection = new JButton(new IntersectionAction(controller));
        intersection.setText("");
        intersection.setToolTipText("Makes an intersection between two entities");
        intersection.setBorderPainted(false);
        add(intersection);

        JButton breakApart = new JButton(new BreakApartAction(controller));
        breakApart.setText("");
        breakApart.setToolTipText("Breaks apart multiple entities");
        breakApart.setBorderPainted(false);
        add(breakApart);

        addSeparator();

        JButton multiply = new JButton(new MultiplyAction(controller));
        multiply.setText("");
        multiply.setToolTipText("Multiplies the selection");
        multiply.setBorderPainted(false);
        add(multiply);

        addSeparator();

        add(Box.createRigidArea(new Dimension(10, 10)));
        add(Box.createHorizontalGlue());

        JSlider zoomSlider = new JSlider(1, 2000, 400);
        zoomSlider.addChangeListener(event -> ThreadHelper.invokeLater(() -> {
            double scale = ((double) zoomSlider.getValue()) / 100d;
            controller.getDrawing().setScale(scale);
        }));
        zoomSlider.setValue((int) (controller.getDrawing().getScale() * 100));

        zoomSlider.setMaximumSize(new Dimension(100, 32));
        add(zoomSlider);

        add(Box.createHorizontalStrut(6));
        PanelButton toolButton = new PanelButton("Tool", controller.getSettings().getToolDescription());
        controller.getSettings().addListener(() -> toolButton.setText(controller.getSettings().getToolDescription()));
        toolButton.addActionListener(e -> {
            ToolSettingsPanel toolSettingsPanel = new ToolSettingsPanel(controller);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(toolSettingsPanel, "Tool settings", true, null);
            if (DialogDisplayer.getDefault().notify(dialogDescriptor) == OK_OPTION) {
                ChangeToolSettingsAction changeStockSettings = new ChangeToolSettingsAction(controller, toolSettingsPanel.getSettings());
                changeStockSettings.actionPerformed(null);
                controller.getUndoManager().addAction(changeStockSettings);
                toolButton.setText(controller.getSettings().getToolDescription());
            }
        });
        add(toolButton);

        add(Box.createHorizontalStrut(6));
        PanelButton stockButton = new PanelButton("Stock", controller.getSettings().getStockSizeDescription());
        controller.getSettings().addListener(() -> stockButton.setText(controller.getSettings().getStockSizeDescription()));
        stockButton.addActionListener(e -> {
            StockSettingsPanel stockSettingsPanel = new StockSettingsPanel(controller);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(stockSettingsPanel, "Stock settings", true, null);
            if (DialogDisplayer.getDefault().notify(dialogDescriptor) == OK_OPTION) {
                double stockThickness = stockSettingsPanel.getStockThickness();
                ChangeStockSettingsAction changeStockSettingsAction = new ChangeStockSettingsAction(controller, stockThickness);
                changeStockSettingsAction.actionPerformed(null);
                controller.getUndoManager().addAction(changeStockSettingsAction);
                stockButton.setText(controller.getSettings().getStockSizeDescription());
            }
        });
        add(stockButton);

        ButtonGroup buttons = new ButtonGroup();
        buttons.add(select);
        buttons.add(circle);
        buttons.add(rectangle);
        buttons.add(text);
        buttons.add(insert);

        controller.addListener(event -> {
            if (event == ControllerEventType.TOOL_SELECTED) {
                buttons.clearSelection();
                controller.getSelectionManager().clearSelection();
                switch (controller.getTool()) {
                    case SELECT:
                        select.setSelected(true);
                        break;
                    case RECTANGLE:
                        rectangle.setSelected(true);
                        break;
                    case CIRCLE:
                        circle.setSelected(true);
                        break;
                    case TEXT:
                        text.setSelected(true);
                        break;
                    case INSERT:
                        insert.setSelected(true);
                        break;
                }
                repaint();
            }
        });
    }
}