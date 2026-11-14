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
import com.willwinder.ugs.designer.actions.UndoableAction;
import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityEvent;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.controls.ResizeUtils;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Drags one of the resize handles. The anchor is the corner or edge that stays fixed. Corner
 * handles scale uniformly, edge handles scale one axis unless Shift or a locked ratio asks
 * for uniform scaling. Sizes are in tenths of a millimeter and the dragged edges snap to the
 * grid lines, or a rotated item's size to whole grid steps.
 * <p>
 * A single selected item is resized along its own, possibly rotated, axes, the same frame its
 * handles are drawn on, so a rotated shape is stretched rather than skewed. Several selected
 * items are resized along the design's axes. The resize is applied live and, on release,
 * undone and re-applied through one undoable action so undo and redo reproduce it exactly.
 */
final class ResizeGesture implements Gesture {
    private static final double MIN_SIZE = 0.1;

    private final ToolContext context;
    private final Anchor anchor;
    private Size originalSize;
    private Size currentSize;
    private Point2D originalPosition;
    private Point2D startDesign;

    // Only set while resizing a single item along its own axes
    private HandleSet.Frame frame;
    private List<Entity> entities;
    private List<AffineTransform> originalTransforms;
    private AffineTransform currentTransform;

    ResizeGesture(ToolContext context, Anchor anchor) {
        this.context = context;
        this.anchor = anchor;
    }

    @Override
    public void begin(PointerEvent event, Point2D design) {
        SelectionManager selection = context.selection();
        startDesign = design;
        if (selection.getChildren().size() == 1) {
            frame = HandleSet.frameOf(selection);
            originalSize = new Size(frame.bounds().getWidth(), frame.bounds().getHeight());
            entities = List.copyOf(selection.getSelection());
            originalTransforms = entities.stream().map(entity -> new AffineTransform(entity.getTransform())).toList();
            currentTransform = new AffineTransform();
        } else {
            frame = null;
            originalSize = selection.getSize();
            originalPosition = selection.getPosition();
        }
        currentSize = originalSize;
    }

    @Override
    public void drag(PointerEvent event, Point2D design) {
        if (originalSize == null) {
            return;
        }
        Size newSize = newSize(event, design);
        if (isTooSmall(originalSize.getWidth(), newSize.getWidth())
                || isTooSmall(originalSize.getHeight(), newSize.getHeight())) {
            return;
        }
        if (frame != null) {
            currentTransform = localScaling(newSize);
            restoreOriginalTransforms();
            entities.forEach(entity -> entity.applyTransform(currentTransform));
            entities.forEach(ResizeGesture::notifyResized);
        } else {
            ResizeUtils.performScaling(context.selection(), anchor, currentSize, newSize);
        }
        currentSize = newSize;
        context.state().setReadout("%.1f × %.1f".formatted(newSize.getWidth(), newSize.getHeight()), design);
        context.render();
    }

    @Override
    public void end(PointerEvent event, Point2D design) {
        if (originalSize == null) {
            return;
        }
        if (frame != null) {
            restoreOriginalTransforms();
            entities.forEach(ResizeGesture::notifyResized);
            if (!currentSize.equals(originalSize)) {
                TransformAction action = new TransformAction(entities, currentTransform);
                action.redo();
                context.undoManager().addAction(action);
            }
        } else {
            SelectionManager selection = context.selection();
            ResizeUtils.performScaling(selection, anchor, currentSize, originalSize);
            selection.setPosition(originalPosition);
            if (!currentSize.equals(originalSize)) {
                List<Entity> selected = new ArrayList<>(selection.getSelection());
                ResizeAction action = new ResizeAction(selected, anchor, originalSize, currentSize);
                action.redo();
                context.undoManager().addAction(action);
            }
        }
        originalSize = null;
        frame = null;
        context.state().setReadout(null, null);
        context.render();
    }

    private void restoreOriginalTransforms() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).setTransform(new AffineTransform(originalTransforms.get(i)));
        }
    }

    /**
     * Scales the frame's box to the new size, along the frame's own axes, keeping the anchor
     * fixed. The result is expressed in design coordinates, ready to apply to the entities.
     */
    private AffineTransform localScaling(Size newSize) {
        Rectangle2D bounds = frame.bounds();
        double sx = originalSize.getWidth() > 0 ? newSize.getWidth() / originalSize.getWidth() : 1;
        double sy = originalSize.getHeight() > 0 ? newSize.getHeight() / originalSize.getHeight() : 1;
        Point2D fixed = anchorPoint(bounds);
        AffineTransform transform = new AffineTransform(frame.transform());
        transform.translate(fixed.getX(), fixed.getY());
        transform.scale(sx, sy);
        transform.translate(-fixed.getX(), -fixed.getY());
        transform.concatenate(inverse(frame.transform()));
        return transform;
    }

    /**
     * The point of the box that stays put, with Y growing upwards so top is the maximum.
     */
    private Point2D anchorPoint(Rectangle2D bounds) {
        double x = switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT, LEFT_CENTER -> bounds.getMinX();
            case TOP_RIGHT, BOTTOM_RIGHT, RIGHT_CENTER -> bounds.getMaxX();
            default -> bounds.getCenterX();
        };
        double y = switch (anchor) {
            case TOP_LEFT, TOP_RIGHT, TOP_CENTER -> bounds.getMaxY();
            case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER -> bounds.getMinY();
            default -> bounds.getCenterY();
        };
        return new Point2D.Double(x, y);
    }

    private Size newSize(PointerEvent event, Point2D design) {
        Point2D delta = movement(design);
        Rectangle2D bounds = originalBounds();
        Point2D fixed = anchorPoint(bounds);
        double deltaX = snapEdge(draggedX(bounds), fixed.getX(), delta.getX());
        double deltaY = snapEdge(draggedY(bounds), fixed.getY(), delta.getY());
        double relativeX = originalSize.getWidth() > 0 ? deltaX / originalSize.getWidth() : 0;
        double relativeY = originalSize.getHeight() > 0 ? deltaY / originalSize.getHeight() : 0;
        Point2D factor = scaleFactor(relativeX, relativeY);
        if (event.isShiftDown() || isRatioLocked()) {
            double uniform = isCorner() ? factor.getX() : (factor.getX() != 1 ? factor.getX() : factor.getY());
            factor = new Point2D.Double(uniform, uniform);
        }
        return new Size(scaleSide(originalSize.getWidth(), factor.getX()),
                scaleSide(originalSize.getHeight(), factor.getY()));
    }

    /**
     * The selection's box before the resize: in the frame's own axes for a single item, in
     * design coordinates for several.
     */
    private Rectangle2D originalBounds() {
        if (frame != null) {
            return frame.bounds();
        }
        return new Rectangle2D.Double(originalPosition.getX(), originalPosition.getY(),
                originalSize.getWidth(), originalSize.getHeight());
    }

    /**
     * The coordinate of the vertical edge the handle drags, NaN when it drags neither.
     */
    private double draggedX(Rectangle2D bounds) {
        return switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT, LEFT_CENTER -> bounds.getMaxX();
            case TOP_RIGHT, BOTTOM_RIGHT, RIGHT_CENTER -> bounds.getMinX();
            default -> Double.NaN;
        };
    }

    /**
     * The coordinate of the horizontal edge the handle drags, NaN when it drags neither.
     */
    private double draggedY(Rectangle2D bounds) {
        return switch (anchor) {
            case TOP_LEFT, TOP_RIGHT, TOP_CENTER -> bounds.getMinY();
            case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER -> bounds.getMaxY();
            default -> Double.NaN;
        };
    }

    /**
     * How far the dragged edge moves so that it lands on the grid rather than a grid step away
     * from where it started. An item that is not rotated has its edge snapped to the design's
     * grid lines. A rotated item's edges cannot line up with the grid, so its size is snapped
     * instead, measured from the edge that stays fixed.
     */
    private double snapEdge(double edge, double fixed, double movement) {
        if (Double.isNaN(edge)) {
            return roundTenth(movement);
        }
        double origin = frame == null || frame.transform().isIdentity() ? 0 : fixed;
        double moved = roundTenth(edge + movement);
        return context.snapper().snap(moved - origin) + origin - edge;
    }

    /**
     * How far the cursor has moved since the press, along the axes the selection is resized in.
     */
    private Point2D movement(Point2D design) {
        Point2D delta = new Point2D.Double(design.getX() - startDesign.getX(), design.getY() - startDesign.getY());
        if (frame == null) {
            return delta;
        }
        return inverse(frame.transform()).deltaTransform(delta, null);
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
        return selected.size() == 1 && selected.getFirst().isLockRatio();
    }

    /**
     * Whether a side would be shrunk below the smallest size. When resizing a single item, a
     * side that was already below it, like the thickness of a straight line, does not count.
     */
    private boolean isTooSmall(double original, double size) {
        // Several items are resized through their common box, which cannot have a side of zero
        boolean flatSide = original < MIN_SIZE && frame != null;
        return !flatSide && size < MIN_SIZE;
    }

    /**
     * Scales a side, rounded to tenths of a millimeter unless it is thinner than that, so the
     * thickness of a nearly straight line is not rounded away.
     */
    private static double scaleSide(double side, double factor) {
        double scaled = side * factor;
        return side < MIN_SIZE ? scaled : roundTenth(scaled);
    }

    private static double roundTenth(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private static AffineTransform inverse(AffineTransform transform) {
        try {
            return transform.createInverse();
        } catch (NoninvertibleTransformException e) {
            return new AffineTransform();
        }
    }

    private static void notifyResized(Entity entity) {
        entity.notifyEvent(new EntityEvent(entity, EventType.RESIZED));
    }

    /**
     * Applies a transform to the entities on redo and its inverse on undo.
     */
    private record TransformAction(List<Entity> entities, AffineTransform transform) implements UndoableAction {
        TransformAction {
            entities = List.copyOf(entities);
            transform = new AffineTransform(transform);
        }

        @Override
        public void redo() {
            apply(transform);
        }

        @Override
        public void undo() {
            apply(inverse(transform));
        }

        private void apply(AffineTransform change) {
            entities.forEach(entity -> entity.applyTransform(change));
            entities.forEach(ResizeGesture::notifyResized);
        }

        @Override
        public String toString() {
            return "resize entity";
        }
    }
}
