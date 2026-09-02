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

import com.willwinder.ugs.designer.actions.MoveAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Drags the selection. The position follows the cursor in whole millimeters, or tenths with
 * Alt held, and snaps to the grid; Shift constrains the move to the dominant axis. The move is
 * applied live and recorded as one {@link MoveAction} on release.
 */
final class MoveGesture implements Gesture {
    private final ToolContext context;
    private Point2D startPosition;
    private Point2D startDesign;

    MoveGesture(ToolContext context) {
        this.context = context;
    }

    @Override
    public void begin(PointerEvent event, Point2D design) {
        startPosition = context.selection().getPosition();
        startDesign = design;
    }

    @Override
    public void drag(PointerEvent event, Point2D design) {
        if (startPosition == null) {
            return;
        }
        double deltaX = design.getX() - startDesign.getX();
        double deltaY = design.getY() - startDesign.getY();
        if (event.isShiftDown()) {
            if (Math.abs(deltaX) >= Math.abs(deltaY)) {
                deltaY = 0;
            } else {
                deltaX = 0;
            }
        }
        boolean fine = event.isAltDown();
        Point2D position = new Point2D.Double(
                context.snapper().snapRounded(startPosition.getX() + deltaX, fine),
                context.snapper().snapRounded(startPosition.getY() + deltaY, fine));
        context.selection().setPosition(position);
        context.state().setReadout("%.1f, %.1f".formatted(position.getX(), position.getY()), design);
        context.render();
    }

    /**
     * A click on a single selected entity selects the next entity under the cursor instead, so a
     * shape hidden behind or around the one that got picked can be reached by clicking again.
     */
    private void cycleSelection(Point2D design) {
        SelectionManager selection = context.selection();
        if (selection.getChildren().size() != 1) {
            return;
        }
        List<Entity> candidates = context.hitTester().entitiesAt(design, context.pickTolerance(design));
        int index = candidates.indexOf(selection.getChildren().getFirst());
        if (index < 0 || candidates.size() < 2) {
            return;
        }
        selection.setSelection(List.of(candidates.get((index + 1) % candidates.size())));
    }

    @Override
    public void end(PointerEvent event, Point2D design) {
        if (startPosition == null) {
            return;
        }
        SelectionManager selection = context.selection();
        Point2D position = selection.getPosition();
        Point2D total = new Point2D.Double(position.getX() - startPosition.getX(), position.getY() - startPosition.getY());
        if (total.getX() != 0 || total.getY() != 0) {
            // The move is already applied; the action only records it for undo.
            List<Entity> entities = new ArrayList<>(selection.getSelection());
            context.undoManager().addAction(new MoveAction(entities, total));
        } else {
            cycleSelection(design);
        }
        startPosition = null;
        context.state().setReadout(null, null);
        context.render();
    }
}
