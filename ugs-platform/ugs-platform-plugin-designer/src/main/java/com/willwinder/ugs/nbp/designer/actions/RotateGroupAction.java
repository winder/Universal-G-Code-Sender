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

import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;

import java.awt.geom.Point2D;

/**
 * An undoable action for rotating a group.
 * This action handles rotation angle changes for groups.
 *
 * @author giro-dev
 */
public class RotateGroupAction implements UndoableAction {

    private final Group selectionGroup;
    private final double originalRotation;
    private final double newRotation;
    private final Point2D originalPivotPoint;

    /**
     * Creates a rotate action for a group.
     *
     * @param selectionGroup the group to rotate
     * @param newRotation    the new rotation angle in degrees
     */
    public RotateGroupAction(Group selectionGroup, double newRotation) {
        this.selectionGroup = selectionGroup;
        this.originalRotation = selectionGroup.getRotation();
        this.newRotation = normalizeAngle(newRotation);
        this.originalPivotPoint = selectionGroup.getPivotPoint();
    }

    /**
     * Creates a rotate action with relative rotation.
     *
     * @param selectionGroup the group to rotate
     * @param deltaRotation  the rotation delta in degrees
     * @param relative       if true, adds to current rotation; if false, sets absolute rotation
     */
    public RotateGroupAction(Group selectionGroup, double deltaRotation, boolean relative) {
        this.selectionGroup = selectionGroup;
        this.originalRotation = selectionGroup.getRotation();
        this.originalPivotPoint = selectionGroup.getPivotPoint();

        if (relative) {

            this.newRotation = normalizeAngle(originalRotation + deltaRotation);
        } else {
            this.newRotation = normalizeAngle(deltaRotation);
        }
    }

    /**
     * Convenience factory method for relative rotation.
     */
    public static RotateGroupAction rotateBy(Group group, double degrees) {
        return new RotateGroupAction(group, degrees, true);
    }

    /**
     * Convenience factory method for absolute rotation.
     */
    public static RotateGroupAction rotateTo(Group group, double degrees) {
        return new RotateGroupAction(group, degrees, false);
    }

    /**
     * Normalize angle to be within 0-360 range.
     */
    private double normalizeAngle(double angle) {
        double normalized = angle % 360;
        if (normalized < 0) {
            normalized += 360;
        }
        return normalized;
    }

    @Override
    public void redo() {
        selectionGroup.setPivotPoint(this.originalPivotPoint);
        selectionGroup.setRotation(newRotation);
    }

    @Override
    public void undo() {
        selectionGroup.setPivotPoint(this.originalPivotPoint);
        selectionGroup.setRotation(originalRotation);
    }

    @Override
    public String toString() {
        double delta = newRotation - originalRotation;
        if (Math.abs(delta) < 0.001) {
            return "Rotate group";
        }
        return String.format("Rotate group %.1f°", delta);
    }

    /**
     * Get the rotation delta applied by this action.
     */
    public double getRotationDelta() {
        return newRotation - originalRotation;
    }
}

