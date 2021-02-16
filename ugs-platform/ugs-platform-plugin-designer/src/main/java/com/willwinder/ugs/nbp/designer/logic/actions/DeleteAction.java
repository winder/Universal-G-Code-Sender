package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;

import java.util.List;

/**
 * DeleteAction implements a single undoable action where all Entities in a given
 * Selection are added to a Drawing.
 */
public class DeleteAction implements DrawAction, UndoableAction {

    private Drawing drawing;
    private List<Entity> entities;

    /**
     * Creates an DeleteAction that removes all shapes in the given Selection
     * from the given Drawing.
     *
     * @param drawing  the drawing from which the entities should be removed.
     * @param entities alist of entities that should be removed from the drawing
     */
    public DeleteAction(Drawing drawing, List<Entity> entities) {
        this.entities = entities;
        this.drawing = drawing;
    }

    public void execute() {
        for (Entity s : entities) {
            drawing.removeEntity(s);
        }
    }

    public void redo() {
        execute();
    }

    public void undo() {
        for (Entity s : entities) {
            drawing.insertEntity(s);
        }
    }
}
