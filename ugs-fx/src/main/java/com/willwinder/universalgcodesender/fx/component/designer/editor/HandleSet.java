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

import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Point;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

/**
 * The handles around the selection: eight for resizing, named after the corner or edge that
 * stays fixed while dragging them, and one above the selection for rotating. Handles keep a
 * constant size on screen, so their positions are computed from the current zoom on every use.
 */
public final class HandleSet {
    public static final double HANDLE_SIZE_PX = 8;
    public static final double RESIZE_MARGIN_PX = 6;
    public static final double ROTATE_MARGIN_PX = 16;
    private static final double HIT_FACTOR = 1.3;

    public enum Kind {
        RESIZE, ROTATE
    }

    /**
     * @param kind   what dragging the handle does
     * @param anchor the corner or edge that stays fixed for a resize handle, null for rotate
     * @param center the handle's centre in design coordinates
     * @param size   the handle's side in design units at the current zoom
     */
    public record Handle(Kind kind, Anchor anchor, Point2D center, double size) {
        public boolean contains(Point2D point) {
            double reach = size / 2 * HIT_FACTOR;
            return Math.abs(point.getX() - center.getX()) <= reach && Math.abs(point.getY() - center.getY()) <= reach;
        }
    }

    private static final Anchor[] RESIZE_ANCHORS = {
            Anchor.TOP_LEFT, Anchor.TOP_CENTER, Anchor.TOP_RIGHT,
            Anchor.LEFT_CENTER, Anchor.RIGHT_CENTER,
            Anchor.BOTTOM_LEFT, Anchor.BOTTOM_CENTER, Anchor.BOTTOM_RIGHT
    };

    private HandleSet() {
    }

    /**
     * The handles for the current selection, or none when nothing is selected or only a point
     * is, since a point has no size or rotation to change.
     */
    public static List<Handle> handles(SelectionManager selection, double worldUnitsPerPixel) {
        return handles(selection, point -> worldUnitsPerPixel);
    }

    /**
     * The handles for the current selection, sizing each one from the scale at its own position
     * so they keep the same size on screen at every depth of a perspective view.
     *
     * @param worldUnitsPerPixelAt the world units one pixel covers at a design position
     */
    public static List<Handle> handles(SelectionManager selection, ToDoubleFunction<Point2D> worldUnitsPerPixelAt) {
        List<Entity> selected = selection.getSelection();
        if (selected.isEmpty() || (selected.size() == 1 && selected.get(0) instanceof Point)) {
            return List.of();
        }
        AffineTransform transform = selection.getTransform();
        Rectangle2D bounds = selection.getRelativeShape().getBounds2D();
        Point2D center = transform.transform(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()), null);
        double marginScale = worldUnitsPerPixelAt.applyAsDouble(center);
        double margin = RESIZE_MARGIN_PX * marginScale;
        List<Handle> handles = new ArrayList<>();
        for (Anchor anchor : RESIZE_ANCHORS) {
            Point2D offset = handleOffset(anchor, bounds, margin);
            Point2D handleCenter = transform.transform(
                    new Point2D.Double(bounds.getX() + offset.getX(), bounds.getY() + offset.getY()), null);
            handles.add(new Handle(Kind.RESIZE, anchor, handleCenter,
                    HANDLE_SIZE_PX * worldUnitsPerPixelAt.applyAsDouble(handleCenter)));
        }
        Point2D rotateCenter = transform.transform(new Point2D.Double(
                bounds.getX() + bounds.getWidth() / 2,
                bounds.getY() + bounds.getHeight() + ROTATE_MARGIN_PX * marginScale), null);
        handles.add(new Handle(Kind.ROTATE, null, rotateCenter,
                HANDLE_SIZE_PX * worldUnitsPerPixelAt.applyAsDouble(rotateCenter)));
        return handles;
    }

    public static Optional<Handle> handleAt(List<Handle> handles, Point2D point) {
        return handles.stream().filter(handle -> handle.contains(point)).findFirst();
    }

    /**
     * The corners of the selection's bounding frame in design coordinates, in drawing order.
     */
    public static List<Point2D> frame(SelectionManager selection) {
        if (selection.isEmpty()) {
            return List.of();
        }
        AffineTransform transform = selection.getTransform();
        Rectangle2D bounds = selection.getRelativeShape().getBounds2D();
        List<Point2D> corners = new ArrayList<>(4);
        corners.add(transform.transform(new Point2D.Double(bounds.getMinX(), bounds.getMinY()), null));
        corners.add(transform.transform(new Point2D.Double(bounds.getMaxX(), bounds.getMinY()), null));
        corners.add(transform.transform(new Point2D.Double(bounds.getMaxX(), bounds.getMaxY()), null));
        corners.add(transform.transform(new Point2D.Double(bounds.getMinX(), bounds.getMaxY()), null));
        return corners;
    }

    /**
     * Where a resize handle sits relative to the selection's bounds. The handle is placed
     * opposite its anchor, which is the side that stays put, matching the legacy designer.
     */
    private static Point2D handleOffset(Anchor anchor, Rectangle2D bounds, double margin) {
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        return switch (anchor) {
            case TOP_LEFT -> new Point2D.Double(width + margin, -margin);
            case BOTTOM_RIGHT -> new Point2D.Double(-margin, height + margin);
            case BOTTOM_LEFT -> new Point2D.Double(width + margin, height + margin);
            case TOP_RIGHT -> new Point2D.Double(-margin, -margin);
            case BOTTOM_CENTER -> new Point2D.Double(width / 2, height + margin);
            case TOP_CENTER -> new Point2D.Double(width / 2, -margin);
            case RIGHT_CENTER -> new Point2D.Double(-margin, height / 2);
            case LEFT_CENTER -> new Point2D.Double(width + margin, height / 2);
            default -> new Point2D.Double(width / 2, height / 2);
        };
    }
}
