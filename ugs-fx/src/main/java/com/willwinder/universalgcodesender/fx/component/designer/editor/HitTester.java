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
import com.willwinder.ugs.designer.constraint.DimensionLine;
import com.willwinder.ugs.designer.constraint.ShapeSnapping;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.logic.Controller;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Finds the design entities under a point or inside a rectangle, in design coordinates.
 * Picking uses a tolerance derived from the zoom, so thin outlines stay clickable when zoomed
 * out, and among several hits the smallest wins so shapes drawn inside others stay reachable.
 */
public final class HitTester {
    private final Controller controller;

    public HitTester(Controller controller) {
        this.controller = controller;
    }

    /**
     * The smallest visible entity within {@code tolerance} millimeters of the point.
     */
    public Optional<Entity> entityAt(Point2D point, double tolerance) {
        return entitiesAt(point, tolerance).stream().findFirst();
    }

    /**
     * Every visible entity within {@code tolerance} millimeters of the point, smallest first.
     */
    public List<Entity> entitiesAt(Point2D point, double tolerance) {
        return candidates().stream()
                .filter(entity -> hits(entity, point, tolerance))
                .sorted(Comparator.comparingDouble(ShapeSnapping::area))
                .toList();
    }

    /**
     * Every visible entity whose bounds intersect the rectangle.
     */
    public List<Entity> entitiesIntersecting(Rectangle2D rectangle) {
        return candidates().stream()
                .filter(entity -> entity.isIntersecting(rectangle))
                .toList();
    }

    /**
     * The best point of a visible entity's shape near the point, see {@link ShapeSnapping#snapAt}.
     */
    public Optional<ShapeSnapping.Snap> snapAt(Point2D point, double tolerance) {
        return ShapeSnapping.snapAt(candidates(), point, tolerance);
    }

    /**
     * The constraint whose label is under the point or whose dimension line is closest to it,
     * within {@code tolerance} millimeters, the dimensions drawn as they are on screen.
     *
     * @param highlighted        the constraints drawn highlighted, as the selected and hovered ones
     * @param worldUnitsPerPixel the size of a pixel, as the labels are sized in pixels
     */
    public Optional<Constraint> constraintAt(Point2D point, Predicate<Constraint> highlighted, double tolerance, double worldUnitsPerPixel) {
        return DimensionLine.constraintAt(controller.getModel().getConstraintManager().getActiveConstraints(), highlighted, point, tolerance,
                worldUnitsPerPixel);
    }

    private List<Entity> candidates() {
        return controller.getModel().getPickableEntities();
    }

    private static boolean hits(Entity entity, Point2D point, double tolerance) {
        Shape shape = entity.getShape();
        if (shape == null) {
            return false;
        }
        if (shape.contains(point)) {
            return true;
        }
        if (tolerance <= 0) {
            return false;
        }
        Shape outline = new BasicStroke((float) (tolerance * 2)).createStrokedShape(shape);
        return outline.contains(point);
    }

}
