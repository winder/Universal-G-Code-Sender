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
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.logic.Controller;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
                .sorted(Comparator.comparingDouble(HitTester::area))
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

    private List<Entity> candidates() {
        return controller.getModel().getEntities().stream()
                .filter(entity -> !(entity instanceof EntityGroup))
                .filter(entity -> !(entity instanceof Cuttable cuttable && cuttable.isHidden()))
                .toList();
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

    private static double area(Entity entity) {
        Rectangle2D bounds = entity.getBounds();
        return bounds == null ? Double.MAX_VALUE : bounds.getWidth() * bounds.getHeight();
    }
}
