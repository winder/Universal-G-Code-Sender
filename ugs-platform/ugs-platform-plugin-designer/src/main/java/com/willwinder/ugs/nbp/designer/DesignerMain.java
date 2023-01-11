package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.MainMenu;
import com.willwinder.ugs.nbp.designer.gui.PopupMenuFactory;
import com.willwinder.ugs.nbp.designer.gui.SelectionSettingsPanel;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.gui.tree.EntitiesTree;
import com.willwinder.ugs.nbp.designer.gui.tree.EntityTreeModel;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A test implementation of the gcode designer tool that works in stand alone mode
 *
 * @author Joacim Breiler
 */
public class DesignerMain extends JFrame {

    private static final long serialVersionUID = 0;
    public static final String PROPERTY_IS_STANDALONE = "ugs.designer.standalone";
    public static final String PROPERTY_USE_SCREEN_MENU = "apple.laf.useScreenMenuBar";

    /**
     * Constructs a new graphical user interface for the program and shows it.
     */
    public DesignerMain() {
        System.setProperty(PROPERTY_USE_SCREEN_MENU, "true");
        System.setProperty(PROPERTY_IS_STANDALONE, "true");

        setTitle("UGS Designer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 768));

        Controller controller = ControllerFactory.getController();
        CentralLookup.getDefault().add(controller);

        UndoManager undoManager = ControllerFactory.getUndoManager();
        CentralLookup.getDefault().add(undoManager);

        SelectionManager selectionManager = ControllerFactory.getSelectionManager();
        CentralLookup.getDefault().add(selectionManager);

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
        controller.getDrawing().setComponentPopupMenu(PopupMenuFactory.createPopupMenu());
        controller.getDrawing().repaint();
    }

    private JSplitPane createRightPanel(Controller controller) {
        EntityTreeModel entityTreeModel = new EntityTreeModel(controller);
        JSplitPane toolsSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(new EntitiesTree(controller, entityTreeModel)), new SelectionSettingsPanel(controller));
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
