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
package com.willwinder.ugs.designer.constraint;

import com.willwinder.ugs.designer.actions.ChangeConstraintOffsetAction;
import com.willwinder.ugs.designer.actions.UndoableAction;

import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * Drags a dimension closer to or further from the entities it measures. The move is applied
 * live and can be recorded as one undoable change when the drag is over. An alignment or a
 * coincidence has no dimension to move, so dragging it changes nothing.
 */
public class DimensionDrag {
    private final ConstraintManager constraintManager;
    private final DistanceConstraint constraint;
    private final double startOffset;
    private final double startAcross;

    public DimensionDrag(ConstraintManager constraintManager, DistanceConstraint constraint, Point2D start) {
        this.constraintManager = constraintManager;
        this.constraint = constraint;
        this.startOffset = constraint.getOffset();
        this.startAcross = constraint.getFrame().crossCoordinate(start);
    }

    public DistanceConstraint getConstraint() {
        return constraint;
    }

    public void drag(Point2D position) {
        double across = constraint.getFrame().crossCoordinate(position);
        constraintManager.setOffset(constraint, startOffset + across - startAcross);
    }

    /**
     * The change the drag made, already applied, to record for undo.
     *
     * @return the change, or empty if the dimension was not moved
     */
    public Optional<UndoableAction> finish() {
        double offset = constraint.getOffset();
        return offset == startOffset
                ? Optional.empty()
                : Optional.of(new ChangeConstraintOffsetAction(constraintManager, constraint, startOffset, offset));
    }
}
