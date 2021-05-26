package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.gui.*;
import com.willwinder.ugs.nbp.designer.gui.entities.Ellipse;
import com.willwinder.ugs.nbp.designer.gui.entities.Group;
import com.willwinder.ugs.nbp.designer.gui.entities.Rectangle;
import com.willwinder.ugs.nbp.designer.io.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

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
        System.setProperty("apple.laf.useScreenMenuBar", "true");


        setTitle("UGS Designer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 768));

        UndoManager undoManager = new SimpleUndoManager();
        CentralLookup.getDefault().add(undoManager);

        SelectionManager selectionManager = new SelectionManager();
        CentralLookup.getDefault().add(selectionManager);

        Controller controller = new Controller();
        CentralLookup.getDefault().add(controller);

        SelectionSettings selectionSettings = new SelectionSettings(controller);

        DrawingContainer drawingContainer = new DrawingContainer(controller);
        controller.addListener(drawingContainer);

        TopToolBar topToolBar = new TopToolBar();
        getContentPane().add(topToolBar, BorderLayout.NORTH);
        ToolBox tools = new ToolBox();
        getContentPane().add(tools, BorderLayout.WEST);
        getContentPane().add(drawingContainer, BorderLayout.CENTER);
        //getContentPane().add(selectionSettings, BorderLayout.EAST);
        getContentPane().add(new EntitiesTree(controller), BorderLayout.EAST);
        JPanel bottomPanel = new JPanel();

        JSlider zoomSlider = new JSlider(1, 1000, 100);
        zoomSlider.addChangeListener(event -> {
            double scale = ((double) zoomSlider.getValue()) / 100d;
            controller.getDrawing().setScale(scale);
        });
        zoomSlider.setValue((int) (controller.getDrawing().getScale() * 100));
        bottomPanel.add(zoomSlider);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        JMenuBar mainMenu = new MainMenu();
        this.setJMenuBar(mainMenu);

        pack();
        setVisible(true);

        //loadExample(controller);
        controller.getDrawing().repaint();
    }

    private void loadExample(Controller controller) {
        SvgReader svgReader = new SvgReader();
        svgReader.read(DesignerMain.class.getResourceAsStream("/com/willwinder/ugs/nbp/designer/example.svg")).ifPresent(group -> {
            group.move(new Point2D.Double(10, 10));
            controller.getDrawing().insertEntity(group);
        });
    }

    public static void main(String[] args) {
        new DesignerMain();
    }
}
