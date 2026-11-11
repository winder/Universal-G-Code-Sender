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

import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;

/**
 * Edits the constraints of the design the same way from every tool.
 */
final class ConstraintEditing {

    private ConstraintEditing() {
    }

    static void select(ToolContext context, Constraint constraint) {
        context.controller().selectConstraint(constraint);
        context.render();
    }

    /**
     * Asks for a new distance for the constraint.
     */
    static void editDistance(ToolContext context, DistanceConstraint constraint) {
        context.distanceEditor().accept(constraint, value -> {
            context.controller().changeConstraintDistance(constraint, value);
            context.render();
        });
    }
}
