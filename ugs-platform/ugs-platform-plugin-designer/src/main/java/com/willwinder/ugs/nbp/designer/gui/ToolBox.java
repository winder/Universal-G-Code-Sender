package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolDrawCircleAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolDrawRectangleAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolInsertAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ToolSelectAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Dimension;

public class ToolBox extends JToolBar {

    public ToolBox() {
        super("Tools", VERTICAL);

        add(Box.createRigidArea(new Dimension(10, 10)));

        JToggleButton select = new JToggleButton(new ToolSelectAction());
        select.setText("");
        select.setToolTipText("Select and move shapes");
        add(select);

        JToggleButton rectangle = new JToggleButton(new ToolDrawRectangleAction());
        rectangle.setText("");
        rectangle.setToolTipText("Draw squares and rectangles");
        add(rectangle);

        JToggleButton circle = new JToggleButton(new ToolDrawCircleAction());
        circle.setText("");
        circle.setToolTipText("Draw circles and ellipses");
        add(circle);

        JToggleButton insert = new JToggleButton(new ToolInsertAction());
        insert.setText("");
        insert.setToolTipText("Inserts a drawing");
        insert.setContentAreaFilled(false);
        add(insert);

        add(Box.createRigidArea(new Dimension(10, 10)));

        ButtonGroup buttons = new ButtonGroup();
        buttons.add(select);
        buttons.add(circle);
        buttons.add(rectangle);
        buttons.add(insert);

        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        controller.addListener((event) -> {
            if (event == ControllerEventType.TOOL_SELECTED) {
                buttons.clearSelection();
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
