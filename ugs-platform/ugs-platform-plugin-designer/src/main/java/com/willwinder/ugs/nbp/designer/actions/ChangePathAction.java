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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.EntityException;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;

/**
 * An action that takes a target cuttable and replaces its shape.
 *
 * @author Joacim Breiler
 */
public class ChangePathAction implements UndoableAction {
    private final Controller controller;
    private final Path target;
    private final Path2D originalShape;
    private final Path2D newShape;

    public ChangePathAction(Controller controller, Path target, Path2D originalShape, Path2D newShape) {
        this.controller = controller;
        this.target = target;
        this.originalShape = originalShape;
        this.newShape = newShape;
    }

    @Override
    public void redo() {
        setShape(newShape);
    }

    private void setShape(Path2D newShape) {
        try {
            AffineTransform transform = target.getTransform();
            target.setShape((Path2D) transform.createInverse().createTransformedShape(newShape));
            controller.getSelectionManager().setSelection(controller.getSelectionManager().getSelection());
        } catch (NoninvertibleTransformException e) {
            throw new EntityException("Could not set the new path o", e);
        }
    }

    @Override
    public void undo() {
        setShape(originalShape);
    }

    @Override
    public String toString() {
        return "modify path";
    }
}
