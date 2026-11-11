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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.actions.UndoableAction;
import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.DimensionDrag;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;

import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * Selects a dimension and drags it closer to or further from the entities it measures. The move
 * is applied live and recorded as one undoable change on release. A double click without
 * dragging edits the distance instead.
 */
final class DimensionDragGesture implements Gesture {
    private final ToolContext context;
    private final Constraint constraint;
    private DimensionDrag drag;
    private boolean doubleClick;

    DimensionDragGesture(ToolContext context, Constraint constraint) {
        this.context = context;
        this.constraint = constraint;
    }

    @Override
    public void begin(PointerEvent event, Point2D design) {
        ConstraintEditing.select(context, constraint);
        drag = constraint instanceof DistanceConstraint distance ? new DimensionDrag(context.constraintManager(), distance, design) : null;
        doubleClick = event.clickCount() == 2;
    }

    @Override
    public void drag(PointerEvent event, Point2D design) {
        if (drag != null) {
            drag.drag(design);
            context.render();
        }
    }

    @Override
    public void end(PointerEvent event, Point2D design) {
        Optional<UndoableAction> change = drag == null ? Optional.empty() : drag.finish();
        change.ifPresentOrElse(context.undoManager()::addAction, () -> {
            if (doubleClick && constraint instanceof DistanceConstraint distance) {
                ConstraintEditing.editDistance(context, distance);
            }
        });
        context.render();
    }
}
