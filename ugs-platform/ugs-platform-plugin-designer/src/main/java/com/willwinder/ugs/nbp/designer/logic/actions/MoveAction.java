package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * MoveAction implements a single undoable action where all the Shapes in a
 * given Selection are moved.
 */
public class MoveAction implements DrawAction, UndoableAction {

    private final List<Entity> entityList;
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
    public MoveAction(List<Entity> entityList, Point2D m) {
        this.entityList = entityList;
        this.movement = m;
    }

    public void execute() {
        entityList.forEach(entity -> entity.move(movement));
    }

    public String getDescription() {
        return "Move";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        Point2D negativeMovement = new Point2D.Double(-movement.getX(), -movement.getY());
        entityList.forEach(entity -> entity.move(negativeMovement));
    }

}
