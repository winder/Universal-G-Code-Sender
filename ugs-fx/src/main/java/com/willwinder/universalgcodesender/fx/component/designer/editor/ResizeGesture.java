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

import com.willwinder.ugs.designer.actions.ResizeAction;
import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.controls.ResizeUtils;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Drags one of the resize handles. The anchor is the corner or edge that stays fixed. Corner
 * handles scale uniformly, edge handles scale one axis unless Shift or a locked ratio asks
 * for uniform scaling. Sizes are in tenths of a millimeter and snap to the grid. The resize is
 * applied live and, on release, undone and re-applied through one {@link ResizeAction} so
 * undo and redo reproduce it exactly.
 */
final class ResizeGesture implements Gesture {
    private static final double MIN_SIZE = 0.1;

    private final ToolContext context;
    private final Anchor anchor;
    private Size originalSize;
    private Size currentSize;
    private Point2D originalPosition;
    private Point2D startDesign;

    ResizeGesture(ToolContext context, Anchor anchor) {
        this.context = context;
        this.anchor = anchor;
    }

    @Override
    public void begin(PointerEvent event, Point2D design) {
        SelectionManager selection = context.selection();
        originalSize = selection.getSize();
        currentSize = originalSize;
        originalPosition = selection.getPosition();
        startDesign = design;
    }

    @Override
    public void drag(PointerEvent event, Point2D design) {
        if (originalSize == null) {
            return;
        }
        Size newSize = newSize(event, design);
        if (newSize.getWidth() < MIN_SIZE || newSize.getHeight() < MIN_SIZE) {
            return;
        }
        ResizeUtils.performScaling(context.selection(), anchor, currentSize, newSize);
        currentSize = newSize;
        context.state().setReadout("%.1f × %.1f".formatted(newSize.getWidth(), newSize.getHeight()), design);
        context.render();
    }

    @Override
    public void end(PointerEvent event, Point2D design) {
        if (originalSize == null) {
            return;
        }
        SelectionManager selection = context.selection();
        ResizeUtils.performScaling(selection, anchor, currentSize, originalSize);
        selection.setPosition(originalPosition);
        if (!currentSize.equals(originalSize)) {
            List<Entity> entities = new ArrayList<>(selection.getSelection());
            ResizeAction action = new ResizeAction(entities, anchor, originalSize, currentSize);
            action.redo();
            context.undoManager().addAction(action);
        }
        originalSize = null;
        context.state().setReadout(null, null);
        context.render();
    }

    private Size newSize(PointerEvent event, Point2D design) {
        double deltaX = context.snapper().snap(roundTenth(design.getX() - startDesign.getX()));
        double deltaY = context.snapper().snap(roundTenth(design.getY() - startDesign.getY()));
        double relativeX = originalSize.getWidth() > 0 ? deltaX / originalSize.getWidth() : 0;
        double relativeY = originalSize.getHeight() > 0 ? deltaY / originalSize.getHeight() : 0;
        Point2D factor = scaleFactor(relativeX, relativeY);
        if (event.isShiftDown() || isRatioLocked()) {
            double uniform = isCorner() ? factor.getX() : (factor.getX() != 1 ? factor.getX() : factor.getY());
            factor = new Point2D.Double(uniform, uniform);
        }
        return new Size(roundTenth(originalSize.getWidth() * factor.getX()),
                roundTenth(originalSize.getHeight() * factor.getY()));
    }

    /**
     * How the cursor's movement scales the selection for each anchor, as the legacy designer
     * defines it: corners scale uniformly by the horizontal movement.
     */
    private Point2D scaleFactor(double deltaX, double deltaY) {
        return switch (anchor) {
            case TOP_RIGHT, BOTTOM_RIGHT -> new Point2D.Double(1 - deltaX, 1 - deltaX);
            case BOTTOM_LEFT, TOP_LEFT -> new Point2D.Double(1 + deltaX, 1 + deltaX);
            case RIGHT_CENTER -> new Point2D.Double(1 - deltaX, 1);
            case LEFT_CENTER -> new Point2D.Double(1 + deltaX, 1);
            case TOP_CENTER -> new Point2D.Double(1, 1 - deltaY);
            case BOTTOM_CENTER -> new Point2D.Double(1, 1 + deltaY);
            default -> new Point2D.Double(1, 1);
        };
    }

    private boolean isCorner() {
        return anchor == Anchor.TOP_LEFT || anchor == Anchor.TOP_RIGHT
                || anchor == Anchor.BOTTOM_LEFT || anchor == Anchor.BOTTOM_RIGHT;
    }

    private boolean isRatioLocked() {
        List<Entity> selected = context.selection().getSelection();
        return selected.size() == 1 && selected.get(0).isLockRatio();
    }

    private static double roundTenth(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
