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
 * Removes a constraint from the design, leaving its entities where they are. Undo adds it back,
 * which may flip other constraints to fit it in, and redo flips those back.
 */
public class RemoveConstraintAction implements UndoableAction {
    private final ConstraintManager constraintManager;
    private final Constraint constraint;
    private List<Constraint> flipped = List.of();

    public RemoveConstraintAction(ConstraintManager constraintManager, Constraint constraint) {
        this.constraintManager = constraintManager;
        this.constraint = constraint;
    }

    public void execute() {
        constraintManager.remove(constraint);
    }

    @Override
    public void redo() {
        execute();
        constraintManager.flip(flipped);
        flipped = List.of();
    }

    @Override
    public void undo() {
        flipped = constraintManager.add(constraint);
    }

    @Override
    public String toString() {
        return "remove constraint";
    }
}
