package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.*;
import com.willwinder.ugs.nbp.designer.gui.tree.EntitiesTree;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import javax.swing.*;
import java.awt.*;

/**
 * A test implementation of the gcode designer tool that works in stand alone mode
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
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 768));

        UndoManager undoManager = new SimpleUndoManager();
        CentralLookup.getDefault().add(undoManager);

        SelectionManager selectionManager = new SelectionManager();
        CentralLookup.getDefault().add(selectionManager);

        Controller controller = new Controller(selectionManager, undoManager);
        CentralLookup.getDefault().add(controller);

        DrawingContainer drawingContainer = new DrawingContainer(controller);
        selectionManager.addSelectionListener(e -> drawingContainer.repaint());

        JSplitPane toolsSplit = createRightPanel(controller);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                drawingContainer, toolsSplit);
        splitPane.setResizeWeight(0.95);

        getContentPane().add(splitPane, BorderLayout.CENTER);

        ToolBox tools = new ToolBox(controller);
        add(tools, BorderLayout.NORTH);


        JMenuBar mainMenu = new MainMenu(controller);
        this.setJMenuBar(mainMenu);

        pack();
        setVisible(true);

        loadExample(controller);
        controller.getDrawing().setComponentPopupMenu(new PopupMenuFactory().createPopupMenu(controller));
        controller.getDrawing().repaint();
    }

    private JSplitPane createRightPanel(Controller controller) {
        JSplitPane toolsSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT, new JScrollPane(new EntitiesTree(controller)), new SelectionSettingsPanel(controller));
        toolsSplit.setResizeWeight(0.9);
        return toolsSplit;
    }

    private void loadExample(Controller controller) {
        SvgReader svgReader = new SvgReader();
        svgReader.read(DesignerMain.class.getResourceAsStream("/com/willwinder/ugs/nbp/designer/platform/example.svg"))
                .ifPresent(design -> design.getEntities().forEach(controller.getDrawing()::insertEntity));
    }

    public static void main(String[] args) {
        new DesignerMain();
    }
}
