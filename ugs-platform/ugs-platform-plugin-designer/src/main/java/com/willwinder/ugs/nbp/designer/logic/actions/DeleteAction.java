package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * DeleteAction implements a single undoable action where all Entities in a given
 * Selection are added to a Drawing.
 */
public class DeleteAction extends AbstractAction {


    private static final String ICON_BASE = "img/edit-delete.png";
    private final UndoManager undoManager;
    private final Controller controller;
    private final SelectionManager selectionManager;

    /**
     * Creates an DeleteAction that removes all shapes in the given Selection
     * from the given Drawing.
     */
    public DeleteAction() {
        undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        controller = CentralLookup.getDefault().lookup(Controller.class);
        selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Delete");
        putValue(NAME, "Delete");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!selectionManager.getChildren().isEmpty()) {
            List<Entity> entities = selectionManager.getSelectedEntities();
            UndoableDeleteAction undoableAction = new UndoableDeleteAction(controller.getDrawing(), entities);
            undoManager.addAction(undoableAction);
            undoableAction.execute();
        }
        selectionManager.removeAll();
    }

    /**
     * Stores the state of
     */
    static class UndoableDeleteAction implements UndoableAction, DrawAction {
        private final List<Entity> entities;
        private final Drawing drawing;

        public UndoableDeleteAction(Drawing drawing, List<Entity> entities) {
            this.drawing = drawing;
            this.entities = entities;
        }

        @Override
        public void redo() {
            execute();
        }

        @Override
        public void undo() {
            for (Entity s : entities) {
                drawing.insertEntity(s);
            }
            drawing.repaint();
        }

        @Override
        public void execute() {
            for (Entity s : entities) {
                drawing.removeEntity(s);
            }
            drawing.repaint();
        }
    }
}
