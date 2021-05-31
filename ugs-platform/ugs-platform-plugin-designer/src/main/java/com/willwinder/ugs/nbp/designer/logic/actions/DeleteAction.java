package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * DeleteAction implements a single undoable action where all Entities in a given
 * Selection are added to a Drawing.
 */
public class DeleteAction extends AbstractAction implements SelectionListener {

    private static final String SMALL_ICON_PATH = "img/delete.svg";
    private static final String LARGE_ICON_PATH = "img/delete32.svg";
    private final Controller controller;

    /**
     * Creates an DeleteAction that removes all shapes in the given Selection
     * from the given Drawing.
     */
    public DeleteAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Delete");
        putValue(NAME, "Delete");

        this.controller = controller;

        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionManager selectionManager = controller.getSelectionManager();
        if (!selectionManager.getSelection().isEmpty()) {
            List<Entity> entities = selectionManager.getSelection();
            UndoableDeleteAction undoableAction = new UndoableDeleteAction(controller.getDrawing(), entities);
            controller.getUndoManager().addAction(undoableAction);
            undoableAction.execute();
        }
        selectionManager.clearSelection();
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    /**
     * Stores the state of the deletion
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

        @Override
        public String toString() {
            return "delete entity";
        }
    }
}
