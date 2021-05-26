package com.willwinder.ugs.nbp.designer.logic.actions;


import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.entities.Entity;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * AddAction implements a single undoable action where an entity is added to a
 * Drawing.
 */
public class AddAction extends AbstractAction implements DrawAction, UndoableAction {

    private Drawing drawing;
    private Entity shape;

    /**
     * Creates an AddAction that adds the given Entity to the given Drawing.
     *
     * @param drawing the drawing into which the shape should be added.
     * @param shape   the shape to be added.
     */
    public AddAction(Drawing drawing, Entity shape) {
        this.drawing = drawing;
        this.shape = shape;
    }

    public void execute() {
        drawing.insertEntity(shape);
    }

    public void redo() {
        this.execute();
    }

    public void undo() {
        drawing.removeEntity(shape);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        execute();
    }

    @Override
    public String toString() {
        return "add entity";
    }
}
