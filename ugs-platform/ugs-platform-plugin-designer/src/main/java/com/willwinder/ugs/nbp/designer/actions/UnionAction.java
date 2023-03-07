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
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.util.List;

/**
 * An action for union multiple entities with each other.
 * This will also add the operation to the undo stack.
 *
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "UnionAction")
@ActionRegistration(
        iconBase = UnionAction.SMALL_ICON_PATH,
        displayName = "Union",
        lazy = false)
public class UnionAction extends AbstractDesignAction implements SelectionListener {
    public static final String SMALL_ICON_PATH = "img/union.svg";
    public static final String LARGE_ICON_PATH = "img/union24.svg";
    private final transient Controller controller;

    public UnionAction() {
        putValue("menuText", "Union");
        putValue(NAME, "Union");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        this.controller = ControllerFactory.getController();
        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(selectionManager.getSelection().size() >= 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Entity> selection = controller.getSelectionManager().getSelection();
        UndoableUnionAction action = new UndoableUnionAction(selection);
        controller.getUndoManager().addAction(action);
        action.redo();
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(selectionManager.getSelection().size() >= 2);
    }

    private class UndoableUnionAction implements UndoableAction {

        private final List<Entity> entities;
        private Path path;

        private UndoableUnionAction(List<Entity> entities) {
            this.entities = entities;
        }

        @Override
        public void redo() {
            Path temporaryPath = new Path();
            entities.forEach(e -> temporaryPath.append(e.getShape()));
            Area area = new Area(temporaryPath.getShape());

            path = new Path();
            path.append(area);

            if (entities.get(0) instanceof Cuttable) {
                Cuttable cuttable = (Cuttable) entities.get(0);
                path.setCutType(cuttable.getCutType());
                path.setStartDepth(cuttable.getStartDepth());
                path.setTargetDepth(cuttable.getTargetDepth());
                path.setName(cuttable.getName());
            }

            controller.getSelectionManager().clearSelection();
            controller.getDrawing().removeEntities(entities);
            controller.getDrawing().insertEntity(path);
            controller.getSelectionManager().addSelection(path);
        }

        @Override
        public void undo() {
            if (path != null) {
                controller.getSelectionManager().clearSelection();
                controller.getDrawing().removeEntity(path);
                controller.getDrawing().insertEntities(entities);
                controller.getSelectionManager().setSelection(entities);
                path = null;
            }
        }

        @Override
        public String toString() {
            return "entity union";
        }
    }
}
