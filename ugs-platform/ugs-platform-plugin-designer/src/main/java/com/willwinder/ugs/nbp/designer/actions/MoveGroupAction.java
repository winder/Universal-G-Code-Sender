/*
    Copyright 2024 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;

import java.awt.geom.Point2D;

/**
 * An undoable action for moving a group by changing its position.
 * This action handles X and Y coordinate changes relative to an anchor point.
 *
 * @author giro-dev
 */
public class MoveGroupAction implements UndoableAction {

    private final Group selectionGroup;
    private final Anchor anchor;
    private final Point2D originalPosition;
    private final Point2D newPosition;
    private final CoordinateType coordinateType;

    public enum CoordinateType {
        X, Y, BOTH
    }

    /**
     * Creates a move action for a group.
     *
     * @param selectionGroup the group to move
     * @param anchor         the anchor point for the move operation
     * @param coordinateType which coordinate is being changed (X, Y, or BOTH)
     * @param newValue       the new value for the coordinate(s)
     */
    public MoveGroupAction(Group selectionGroup, Anchor anchor, CoordinateType coordinateType, Object newValue) {
        this.selectionGroup = selectionGroup;
        this.anchor = anchor;
        this.coordinateType = coordinateType;
        this.originalPosition = selectionGroup.getPosition(anchor);

        // Calculate the new position based on coordinate type
        double newX = originalPosition.getX();
        double newY = originalPosition.getY();

        switch (coordinateType) {
            case X:
                if (newValue instanceof Double) {
                    newX = (Double) newValue;
                }
                break;
            case Y:
                if (newValue instanceof Double) {
                    newY = (Double) newValue;
                }
                break;
            case BOTH:
                if (newValue instanceof Point2D point) {
                    newX = point.getX();
                    newY = point.getY();
                }
                break;
        }

        this.newPosition = new Point2D.Double(newX, newY);
    }

    /**
     * Convenience constructor for moving by X coordinate only.
     */
    public static MoveGroupAction moveX(Group group, Anchor anchor, double newX) {
        return new MoveGroupAction(group, anchor, CoordinateType.X, newX);
    }

    /**
     * Convenience constructor for moving by Y coordinate only.
     */
    public static MoveGroupAction moveY(Group group, Anchor anchor, double newY) {
        return new MoveGroupAction(group, anchor, CoordinateType.Y, newY);
    }

    /**
     * Convenience constructor for moving by both coordinates.
     */
    public static MoveGroupAction moveTo(Group group, Anchor anchor, Point2D newPosition) {
        return new MoveGroupAction(group, anchor, CoordinateType.BOTH, newPosition);
    }

    @Override
    public void redo() {
        selectionGroup.setPosition(anchor, newPosition);
    }

    @Override
    public void undo() {
        selectionGroup.setPosition(anchor, originalPosition);
    }

    @Override
    public String toString() {
        return switch (coordinateType) {
            case X -> "Change group X position";
            case Y -> "Change group Y position";
            case BOTH -> "Move group";
        };
    }
}

