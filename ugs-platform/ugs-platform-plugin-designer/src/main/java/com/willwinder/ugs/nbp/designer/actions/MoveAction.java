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

import java.awt.geom.Point2D;
import java.util.List;

/**
 * MoveAction implements a single action where all the Shapes in a
 * given Selection are moved.
 *
 * @author Joacim Breiler
 */
public class MoveAction implements DrawAction, UndoableAction {

    private final List<Entity> entityList;
    private Point2D movement;

    /**
     * Creates a MoveAction that moves all Shapes in the given Selection in the
     * direction given by the point. The movement is relative to the shapes
     * original position.
     *
     * @param entityList a selection which contains the shapes to be moved
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

    public void redo() {
        execute();
    }

    public void undo() {
        Point2D negativeMovement = new Point2D.Double(-movement.getX(), -movement.getY());
        entityList.forEach(entity -> entity.move(negativeMovement));
    }

    @Override
    public String toString() {
        return "move entity";
    }
}
