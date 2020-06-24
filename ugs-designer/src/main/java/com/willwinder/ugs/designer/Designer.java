package com.willwinder.ugs.designer;


import com.willwinder.ugs.designer.gui.DrawingContainer;
import com.willwinder.ugs.designer.gui.MainMenu;
import com.willwinder.ugs.designer.gui.MenuListener;
import com.willwinder.ugs.designer.gui.SelectionSettings;
import com.willwinder.ugs.designer.gui.ToolBox;
import com.willwinder.ugs.designer.logic.Controller;

import javax.swing.*;
import java.awt.*;

/**
 * Graphical user interface for the Drawing editor "Draw"
 *
 * @author Alex Lagerstedt
 */

public class Designer extends JFrame {

    private static final long serialVersionUID = 0;

    /**
     * Constructs a new graphical user interface for the program and shows it.
     */
    public Designer() {

        this.setTitle("Draw 0.2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Controller controller = new Controller();
        ToolBox tools = new ToolBox(controller);
        SelectionSettings selectionSettings = new SelectionSettings(controller);

        DrawingContainer drawingContainer = new DrawingContainer(controller);
        controller.addListener(drawingContainer);

        getContentPane().add(tools, BorderLayout.WEST);
        getContentPane().add(drawingContainer, BorderLayout.CENTER);
        getContentPane().add(selectionSettings, BorderLayout.EAST);



        MenuListener mainMenuListener = new MenuListener(controller);
        JMenuBar mainMenu = new MainMenu(mainMenuListener);
        this.setJMenuBar(mainMenu);

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        new Designer();
    }
}
