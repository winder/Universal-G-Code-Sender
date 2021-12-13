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
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

/**
 * AddAction implements a single undoable action where an entity is added to a
 * Drawing.
 *
 * @author Joacim Breiler
 */
public class AddAction extends AbstractAction implements DrawAction, UndoableAction {

    private final transient Controller controller;
    private final transient List<Entity> entities;

    /**
     * Creates an AddAction that adds the given Entity to the given Drawing.
     *
     * @param controller the controller for the drawing.
     * @param entity     the entity to be added.
     */
    public AddAction(Controller controller, Entity entity) {
        this.controller = controller;
        this.entities = Collections.singletonList(entity);
    }

    /**
     * Creates an AddAction that adds the given Entity to the given Drawing.
     *
     * @param controller the controller for the drawing.
     * @param entities   the entities to be added.
     */
    public AddAction(Controller controller, List<Entity> entities) {
        this.controller = controller;
        this.entities = entities;
    }

    public void execute() {
        controller.getSelectionManager().clearSelection();
        controller.getDrawing().insertEntities(entities);
    }

    public void redo() {
        this.execute();
    }

    public void undo() {
        controller.getSelectionManager().clearSelection();
        controller.getDrawing().removeEntities(entities);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controller.getUndoManager().addAction(this);
        execute();
    }

    @Override
    public String toString() {
        if (entities.size() > 1) {
            return "add entities";
        } else {
            return "add entity";
        }
    }
}
