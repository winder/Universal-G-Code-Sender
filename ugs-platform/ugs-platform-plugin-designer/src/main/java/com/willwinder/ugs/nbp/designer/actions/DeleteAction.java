/*
    Copyright 2021 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * DeleteAction implements a single action where all Entities in a given
 * Selection are removed from a Drawing.
 *
 * @author Joacim Breiler
 */
@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.DeleteAction",
        category = "Edit")
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "BACK_SPACE")
})
public class DeleteAction extends AbstractAction implements SelectionListener {

    private static final String SMALL_ICON_PATH = "img/delete.svg";
    private static final String LARGE_ICON_PATH = "img/delete24.svg";
    private final transient Controller controller;

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
        setEnabled(!selectionManager.getChildren().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionManager selectionManager = controller.getSelectionManager();
        List<Entity> selection = selectionManager.getChildren();
        if (!selection.isEmpty()) {
            UndoableDeleteAction undoableAction = new UndoableDeleteAction(controller.getDrawing(), selection);
            controller.getUndoManager().addAction(undoableAction);
            undoableAction.execute();
        }
        selectionManager.clearSelection();
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(!selectionManager.getChildren().isEmpty());
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
            drawing.insertEntities(entities);
            drawing.repaint();
        }

        @Override
        public void execute() {
            drawing.removeEntities(entities);
            drawing.repaint();
        }

        @Override
        public String toString() {
            return "delete entity";
        }
    }
}
