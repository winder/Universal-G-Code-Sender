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

import javax.swing.*;

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

        JToggleButton zoom = new JToggleButton(new ToolZoomAction(controller));
        zoom.setText("");
        zoom.setToolTipText("Controls zoom");
        zoom.setBorderPainted(false);
        add(zoom);

        ButtonGroup buttons = new ButtonGroup();
        buttons.add(select);
        buttons.add(circle);
        buttons.add(rectangle);
        buttons.add(text);
        buttons.add(insert);
        buttons.add(zoom);

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
                    case ZOOM:
                        zoom.setSelected(true);
                        break;
                }
                repaint();
            }
        });
    }
}