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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
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
    public static final double ROTATE_MARGIN_PX = 16;
    /**
     * Below this size on screen the resize handles move outside the frame instead of sitting
     * centred on it, so they do not cover a small selection.
     */
    public static final double MIN_CENTERED_FRAME_PX = 3 * HANDLE_SIZE_PX;
    private static final double HIT_FACTOR = 1.3;
    private static final double SMALL_FRAME_GAP_PX = 1;

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

        /**
         * Whether the other handle is the same control as this one. Handles are recomputed on
         * every use and their position and size follow the zoom, so they are identified by what
         * they do rather than by where they are.
         */
        public boolean isSameHandle(Handle other) {
            return other != null && kind == other.kind && anchor == other.anchor;
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
        return handles(selection, _ -> worldUnitsPerPixel);
    }

    /**
     * The handles for the current selection, sizing each one from the scale at its own position
     * so they keep the same size on screen at every depth of a perspective view.
     *
     * @param worldUnitsPerPixelAt the world units one pixel covers at a design position
     */
    public static List<Handle> handles(SelectionManager selection, ToDoubleFunction<Point2D> worldUnitsPerPixelAt) {
        List<Entity> selected = selection.getSelection();
        if (selected.isEmpty() || (selected.size() == 1 && selected.getFirst() instanceof Point)) {
            return List.of();
        }
        Frame frame = frameOf(selection);
        AffineTransform transform = frame.transform();
        Rectangle2D bounds = frame.bounds();
        Point2D center = transform.transform(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()), null);
        double marginScale = worldUnitsPerPixelAt.applyAsDouble(center);
        double margin = resizeMargin(bounds, marginScale);
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
     * The frame follows the rotation of a single selected item, see {@link #frameOf}.
     */
    public static List<Point2D> frame(SelectionManager selection) {
        if (selection.isEmpty()) {
            return List.of();
        }
        Frame frame = frameOf(selection);
        AffineTransform transform = frame.transform();
        Rectangle2D bounds = frame.bounds();
        List<Point2D> corners = new ArrayList<>(4);
        corners.add(transform.transform(new Point2D.Double(bounds.getMinX(), bounds.getMinY()), null));
        corners.add(transform.transform(new Point2D.Double(bounds.getMaxX(), bounds.getMinY()), null));
        corners.add(transform.transform(new Point2D.Double(bounds.getMaxX(), bounds.getMaxY()), null));
        corners.add(transform.transform(new Point2D.Double(bounds.getMinX(), bounds.getMaxY()), null));
        return corners;
    }

    /**
     * Whether a design position lies inside the selection's frame.
     */
    public static boolean frameContains(SelectionManager selection, Point2D point) {
        List<Point2D> corners = frame(selection);
        if (corners.isEmpty()) {
            return false;
        }
        Path2D path = new Path2D.Double();
        path.moveTo(corners.getFirst().getX(), corners.getFirst().getY());
        for (int i = 1; i < corners.size(); i++) {
            path.lineTo(corners.get(i).getX(), corners.get(i).getY());
        }
        path.closePath();
        return path.contains(point);
    }

    /**
     * A box in its own coordinate system and the transform that places it in the design.
     */
    record Frame(AffineTransform transform, Rectangle2D bounds) {
    }

    /**
     * The selection's frame. A single selected item gets a box turned with its rotation, fitted
     * tightly around its outline in that rotated system. Several selected items keep the
     * selection's own, unrotated box.
     */
    static Frame frameOf(SelectionManager selection) {
        if (selection.getChildren().size() != 1) {
            return new Frame(selection.getTransform(), selection.getRelativeShape().getBounds2D());
        }
        // The selection keeps its bounds until one of its entities changes, so the same bounds
        // object means the frame fitted around the entities is still the same
        Shape selectionBounds = selection.getShape();
        double rotation = selection.getRotation();
        List<Entity> children = selection.getChildren();
        CachedFrame cached = cachedFrame;
        if (cached != null && cached.selection() == selection && cached.selectionBounds() == selectionBounds
                && cached.rotation() == rotation && cached.children().equals(children)) {
            return cached.frame();
        }
        Frame frame = fitFrame(selection, rotation);
        cachedFrame = new CachedFrame(selection, selectionBounds, rotation, List.copyOf(children), frame);
        return frame;
    }

    /**
     * The last frame fitted around a single selected item and what it was fitted for, as
     * fitting it transforms every outline of the item and the frame is asked for on every
     * render and mouse move. Only used on the JavaFX thread.
     */
    private record CachedFrame(SelectionManager selection, Shape selectionBounds, double rotation,
                               List<Entity> children, Frame frame) {
    }

    private static CachedFrame cachedFrame;

    private static Frame fitFrame(SelectionManager selection, double rotation) {
        double radians = -Math.toRadians(rotation);
        AffineTransform toDesign = AffineTransform.getRotateInstance(radians);
        AffineTransform toFrame = AffineTransform.getRotateInstance(-radians);
        Rectangle2D bounds = null;
        for (Entity entity : selection.getSelection()) {
            Rectangle2D entityBounds = toFrame.createTransformedShape(entity.getShape()).getBounds2D();
            if (bounds == null) {
                bounds = entityBounds;
            } else {
                bounds.add(entityBounds);
            }
        }
        return new Frame(toDesign, bounds == null ? new Rectangle2D.Double() : bounds);
    }

    /**
     * How far outside the frame the resize handles are pushed. Normally none, so they are
     * centred on the frame. When the frame is too small on screen the handles would cover it
     * and make it impossible to click or move, so they are pushed out until their reach stops
     * just short of the frame.
     */
    private static double resizeMargin(Rectangle2D bounds, double worldUnitsPerPixel) {
        double smallestSidePx = Math.min(bounds.getWidth(), bounds.getHeight()) / worldUnitsPerPixel;
        if (smallestSidePx >= MIN_CENTERED_FRAME_PX) {
            return 0;
        }
        return (HANDLE_SIZE_PX / 2 * HIT_FACTOR + SMALL_FRAME_GAP_PX) * worldUnitsPerPixel;
    }

    /**
     * Where a resize handle sits relative to the selection's bounds: on the frame's corner or
     * edge midpoint opposite its anchor, which is the side that stays put, matching the legacy
     * designer, pushed outwards by the margin.
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
