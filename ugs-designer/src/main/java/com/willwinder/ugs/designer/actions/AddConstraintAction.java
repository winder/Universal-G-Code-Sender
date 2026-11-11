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

import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.ConstraintManager;

import java.util.List;

/**
 * Adds a constraint to the design, moving its target to satisfy it. Undo flips back the
 * constraints that were flipped to fit it in.
 */
public class AddConstraintAction implements UndoableAction {
    private final ConstraintManager constraintManager;
    private final Constraint constraint;
    private List<Constraint> flipped = List.of();

    public AddConstraintAction(ConstraintManager constraintManager, Constraint constraint) {
        this.constraintManager = constraintManager;
        this.constraint = constraint;
    }

    public void execute() {
        flipped = constraintManager.add(constraint);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public void undo() {
        constraintManager.remove(constraint);
        constraintManager.flip(flipped);
    }

    @Override
    public String toString() {
        return "add constraint";
    }
}
