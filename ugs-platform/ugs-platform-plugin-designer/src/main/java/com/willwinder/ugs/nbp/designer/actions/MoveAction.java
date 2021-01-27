package com.willwinder.ugs.nbp.designer.actions;



import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.awt.geom.Point2D;

/**
 * MoveAction implements a single undoable action where all the Shapes in a
 * given Selection are moved.
 */
public class MoveAction implements DrawAction {

    private SelectionManager selectionManager;
    private Point2D movement;

    /**
     * Creates a MoveAction that moves all Shapes in the given Selection in the
     * direction given by the point. The movement is relative to the shapes
     * original position.
     *
     * @param s a selection which contains the shapes to be moved
     * @param m the amount the shapes should be moved, relative to the
     *          original position
     */
    public MoveAction(SelectionManager s, Point2D m) {
        this.selectionManager = s;
        this.movement = m;
    }

    public void execute() {
        for (Entity s : selectionManager.getShapes()) {
            Point2D position = s.getPosition();
            //s.setPosition(position.getX() + movement.getX(), position.getY() + movement.getY());
        }
    }

    public String getDescription() {
        return null;
    }

    public void redo() {
        execute();
    }

    public void undo() {
        for (Entity s : selectionManager.getShapes()) {
            Point2D position = s.getPosition();
            //s.setPosition(position.getX() - movement.getX(), position.getY() - movement.getY());
        }
    }

}
