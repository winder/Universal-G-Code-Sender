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
 * Moves where the dimension of a constraint is drawn.
 */
public class ChangeConstraintOffsetAction implements UndoableAction {
    private final ConstraintManager constraintManager;
    private final DistanceConstraint constraint;
    private final double previousOffset;
    private final double offset;

    public ChangeConstraintOffsetAction(ConstraintManager constraintManager, DistanceConstraint constraint,
                                        double previousOffset, double offset) {
        this.constraintManager = constraintManager;
        this.constraint = constraint;
        this.previousOffset = previousOffset;
        this.offset = offset;
    }

    public void execute() {
        constraintManager.setOffset(constraint, offset);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public void undo() {
        constraintManager.setOffset(constraint, previousOffset);
    }

    @Override
    public String toString() {
        return "move dimension";
    }
}
