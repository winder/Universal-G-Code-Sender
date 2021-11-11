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
 * RotateAction implements an action where all the entities in a
 * given Selection are rotated.
 *
 * @author Joacim Breiler
 */
public class RotateAction implements DrawAction, UndoableAction {

    private final List<Entity> entityList;
    private final Point2D center;
    private double rotation;

    /**
     * Creates a MoveAction that moves all Shapes in the given Selection in the
     * direction given by the point. The movement is relative to the shapes
     * original position.
     *
     * @param entityList a selection which contains the shapes to be moved
     * @param center     the center to rotate around
     * @param rotation   the amount the shapes should be rotated, relative to the
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
