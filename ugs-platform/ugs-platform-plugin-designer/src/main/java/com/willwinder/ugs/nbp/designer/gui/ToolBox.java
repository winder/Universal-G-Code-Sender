package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolDrawCircleAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolDrawRectangleAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolInsertAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolSelectAction;

import javax.swing.*;
import java.awt.Dimension;

public class ToolBox extends JToolBar {

    public ToolBox(Controller controller) {
        super("Tools");
        setFloatable(false);

        JToggleButton select = new JToggleButton(new ToolSelectAction(controller));
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

        JToggleButton insert = new JToggleButton(new ToolInsertAction(controller));
        insert.setText("");
        insert.setToolTipText("Inserts a drawing");
        insert.setContentAreaFilled(false);
        add(insert);

        add(Box.createRigidArea(new Dimension(10, 10)));
        add(Box.createHorizontalGlue());

        JSlider zoomSlider = new JSlider(1, 1000, 100);
        zoomSlider.addChangeListener(event -> {
            double scale = ((double) zoomSlider.getValue()) / 100d;
            controller.getDrawing().setScale(scale);
        });
        zoomSlider.setValue((int) (controller.getDrawing().getScale() * 100));
        add(zoomSlider);

        ButtonGroup buttons = new ButtonGroup();
        buttons.add(select);
        buttons.add(circle);
        buttons.add(rectangle);
        buttons.add(insert);

        controller.addListener((event) -> {
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
                    case INSERT:
                        insert.setSelected(true);
                        break;
                }
                repaint();
            }
        });
    }
}
