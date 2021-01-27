package com.willwinder.ugs.nbp.designer.actions;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;

import java.util.List;

/**
 * DeleteAction implements a single undoable action where all Shapes in a given
 * Selection are added to a Drawing.
 */
public class DeleteAction implements DrawAction {

    private Drawing d;
    private List<Entity> entities;

    /**
     * Creates an DeleteAction that removes all shapes in the given Selection
     * from the given Drawing.
     *
     * @param drawing  the drawing into which the shape should be added.
     * @param entities the shape to be added.
     */
    public DeleteAction(Drawing drawing, List<Entity> entities) {
        this.entities = entities;
        this.d = drawing;
    }

    public void execute() {
        for (Entity s : entities) {
            d.removeShape(s);
        }
    }

    public void redo() {
        execute();
    }

    public void undo() {
        for (Entity s : entities) {
            d.insertShape(s);
        }
    }
}
