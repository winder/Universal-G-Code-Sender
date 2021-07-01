package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * RotateAction implements a single undoable action where all the Shapes in a
 * given Selection are rotated.
 */
public class RotateAction implements DrawAction, UndoableAction {

    private final List<Entity> entityList;
    private final Point2D center;
    private double rotation;

    /**
     * Creates a MoveAction that moves all Shapes in the given Selection in the
     * direction given by the point. The movement is relative to the shapes
     * original position.
     *  @param entityList a selection which contains the shapes to be moved
     * @param center
     * @param rotation the amount the shapes should be rotated, relative to the
     */
    public RotateAction(List<Entity> entityList, Point2D center, double rotation) {
        this.entityList = entityList;
        this.rotation = rotation;
        this.center = center;
    }

    public void execute() {
        entityList.forEach(entity -> entity.rotate(center, rotation));
    }

    public void redo() {
        execute();
    }

    public void undo() {
        entityList.forEach(entity -> entity.rotate(center, -rotation));
    }

    @Override
    public String toString() {
        return "rotate entity";
    }
}
