package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.entities.Group;
import com.willwinder.ugs.nbp.designer.entities.Rectangle;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.SelectionSettings;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A gcode designer tool that works in stand alone mode
 *
 * @author Joacim Breiler
 */
public class DesignerMain extends JFrame {

    private static final long serialVersionUID = 0;

    /**
     * Constructs a new graphical user interface for the program and shows it.
     */
    public DesignerMain() {

        setTitle("UGS Designer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 768));

        Controller controller = new Controller();
        ToolBox tools = new ToolBox(controller);
        SelectionSettings selectionSettings = new SelectionSettings(controller);

        DrawingContainer drawingContainer = new DrawingContainer(controller);
        controller.addListener(drawingContainer);

        getContentPane().add(tools, BorderLayout.WEST);
        getContentPane().add(drawingContainer, BorderLayout.CENTER);
        //getContentPane().add(selectionSettings, BorderLayout.EAST);
        JPanel bottomPanel = new JPanel();

        JSlider zoomSlider = new JSlider(1, 1000, 100);
        zoomSlider.addChangeListener(event -> {
            double scale = ((double) zoomSlider.getValue()) / 100d;
            controller.setScale(scale);
        });
        bottomPanel.add(zoomSlider);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        MenuListener mainMenuListener = new MenuListener(controller);
        JMenuBar mainMenu = new MainMenu(mainMenuListener);
        this.setJMenuBar(mainMenu);

        pack();
        setVisible(true);

        Group group = new Group();
        group.setPosition(200, 200);
        group.setRotation(90);

        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setHeight(50);
        rectangle.setWidth(50);
        rectangle.setRotation(Math.PI / 2);
        group.addChild(rectangle);

        Rectangle rectangle2 = new Rectangle(100, 100);
        rectangle2.setHeight(50);
        rectangle2.setWidth(50);
        group.addChild(rectangle2);

        controller.getDrawing().insertEntity(group);

    }

    public static void main(String[] args) {
        new DesignerMain();
    }
}
