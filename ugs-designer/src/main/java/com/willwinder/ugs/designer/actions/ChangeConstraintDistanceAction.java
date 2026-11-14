/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.constraint.ConstraintManager;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;

/**
 * Changes the distance a constraint holds, moving its target and whatever follows it.
 */
public class ChangeConstraintDistanceAction implements UndoableAction {
    private final ConstraintManager constraintManager;
    private final DistanceConstraint constraint;
    private final double previousDistance;
    private final double distance;

    public ChangeConstraintDistanceAction(ConstraintManager constraintManager, DistanceConstraint constraint, double distance) {
        this.constraintManager = constraintManager;
        this.constraint = constraint;
        this.previousDistance = constraint.getDistance();
        this.distance = distance;
    }

    public void execute() {
        constraintManager.setDistance(constraint, distance);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public void undo() {
        constraintManager.setDistance(constraint, previousDistance);
    }

    @Override
    public String toString() {
        return "change constraint";
    }
}
