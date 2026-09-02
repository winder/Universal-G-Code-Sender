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

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * A press on empty space: a drag selects everything the rubber band touches, a click selects
 * the smallest entity under the cursor or clears the selection. Shift toggles instead of
 * replacing.
 */
final class MarqueeGesture implements Gesture {
    /** A drag shorter than this in millimeters counts as a click. */
    private static final double CLICK_EXTENT = 1;

    private final ToolContext context;
    private Point2D start;

    MarqueeGesture(ToolContext context) {
        this.context = context;
    }

    @Override
    public void begin(PointerEvent event, Point2D design) {
        start = design;
        context.state().setRubberBand(null);
    }

    @Override
    public void drag(PointerEvent event, Point2D design) {
        if (start == null) {
            return;
        }
        context.state().setRubberBand(rectangle(start, design));
        context.render();
    }

    @Override
    public void end(PointerEvent event, Point2D design) {
        if (start == null) {
            return;
        }
        Rectangle2D rectangle = rectangle(start, design);
        if (rectangle.getWidth() > CLICK_EXTENT && rectangle.getHeight() > CLICK_EXTENT) {
            selectIntersecting(rectangle, event.isShiftDown());
        } else {
            selectOne(design, event.isShiftDown());
        }
        start = null;
        context.state().setRubberBand(null);
        context.render();
    }

    private void selectIntersecting(Rectangle2D rectangle, boolean toggle) {
        List<Entity> entities = context.hitTester().entitiesIntersecting(rectangle);
        SelectionManager selection = context.selection();
        if (toggle) {
            selection.toggleSelection(new HashSet<>(entities));
        } else {
            selection.setSelection(entities);
        }
    }

    private void selectOne(Point2D design, boolean toggle) {
        Optional<Entity> hit = context.hitTester().entityAt(design, context.pickTolerance(design));
        SelectionManager selection = context.selection();
        if (toggle) {
            hit.ifPresent(selection::toggleSelection);
        } else if (hit.isPresent()) {
            if (!selection.isSelected(hit.get())) {
                selection.setSelection(List.of(hit.get()));
            }
        } else {
            selection.clearSelection();
        }
    }

    private static Rectangle2D rectangle(Point2D a, Point2D b) {
        double x = Math.min(a.getX(), b.getX());
        double y = Math.min(a.getY(), b.getY());
        return new Rectangle2D.Double(x, y, Math.abs(b.getX() - a.getX()), Math.abs(b.getY() - a.getY()));
    }
}
