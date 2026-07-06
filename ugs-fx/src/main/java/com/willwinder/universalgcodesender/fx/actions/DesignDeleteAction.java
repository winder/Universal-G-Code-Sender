/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.designer.actions.UndoableAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.universalgcodesender.i18n.Localization;

import java.util.ArrayList;
import java.util.List;

public class DesignDeleteAction extends AbstractDesignEditAction implements SelectionListener {
    public static final String ICON_BASE = "icons/delete.svg";

    public DesignDeleteAction() {
        super(Localization.getString("platform.designer.delete"), ICON_BASE);
        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        enabledProperty().set(!selectionManager.getChildren().isEmpty());
    }

    @Override
    protected void performAction() {
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
        setEnabledLater(!controller.getSelectionManager().getChildren().isEmpty());
    }

    /**
     * Removes the given entities from the drawing and restores them on undo.
     */
    private static class UndoableDeleteAction implements UndoableAction {
        private final transient Drawing drawing;
        private final transient List<Entity> entities;

        UndoableDeleteAction(Drawing drawing, List<Entity> entities) {
            this.drawing = drawing;
            this.entities = new ArrayList<>(entities);
        }

        void execute() {
            drawing.removeEntities(entities);
            drawing.repaint();
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
        public String toString() {
            return "delete entity";
        }
    }
}
