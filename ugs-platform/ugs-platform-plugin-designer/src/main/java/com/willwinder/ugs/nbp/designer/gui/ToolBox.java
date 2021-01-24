package com.willwinder.ugs.nbp.designer.gui;


import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;

import javax.swing.*;
import java.awt.*;

public class ToolBox extends JToolBar {

    private JToggleButton select;
    private JToggleButton rectangle;
    private JToggleButton circle;


    public ToolBox(Controller controller) {
        super("Tools", VERTICAL);

        add(Box.createRigidArea(new Dimension(10, 10)));

        select = new JToggleButton(new ImageIcon(ToolBox.class.getResource("/img/cursor.png")));
        select.setToolTipText("Select and move shapes");
        select.addActionListener((event) -> controller.setTool(Tool.SELECT));
        add(select);

        rectangle = new JToggleButton(new ImageIcon(ToolBox.class.getResource("/img/rectangle.png")));
        rectangle.setToolTipText("Draw squares and rectangles");
        rectangle.addActionListener((event) -> controller.setTool(Tool.RECTANGLE));
        add(rectangle);

        circle = new JToggleButton(new ImageIcon(ToolBox.class.getResource("/img/circle.png")));
        circle.setToolTipText("Draw circles and ellipses");
        circle.addActionListener((event) -> controller.setTool(Tool.CIRCLE));
        add(circle);

        add(Box.createRigidArea(new Dimension(10, 10)));

        ButtonGroup buttons = new ButtonGroup();
        buttons.add(select);
        buttons.add(circle);
        buttons.add(rectangle);
    }
}
