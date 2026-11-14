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
package com.willwinder.ugs.designer.constraint;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Point;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Finds the point of an entity's shape a constraint should measure from, near a position such as
 * the cursor: one of its vertices or its center, vertices winning over the center, as long as
 * they are within the tolerance. A point entity only has its center, as its outline is just how
 * it is drawn.
 */
public final class ShapeSnapping {

    /**
     * What a snap landed on, in the order they win over each other.
     */
    public enum Kind {VERTEX, CENTER}

    /**
     * A point of the entity's shape and where it is.
     */
    public record Snap(Entity entity, ShapePoint point, Point2D position, Kind kind) {
    }

    private static final double EXACT_TOLERANCE = 1e-6;

    private ShapeSnapping() {
    }

    public static Optional<Snap> snap(Entity entity, Point2D position, double tolerance) {
        Rectangle2D bounds = entity.getBounds();
        if (bounds == null || !expanded(bounds, tolerance).contains(position)) {
            return Optional.empty();
        }
        Optional<Snap> vertex = hasVertices(entity) ? nearestVertex(entity, position, tolerance) : Optional.empty();
        return vertex.or(() -> center(entity, bounds, position, tolerance));
    }

    /**
     * The best point of the entities' shapes near the position, within the tolerance: a vertex
     * before the center, the closest one of a kind first. Among equally good points the smaller
     * entity's wins.
     */
    public static Optional<Snap> snapAt(Collection<Entity> entities, Point2D position, double tolerance) {
        return entities.stream()
                .map(entity -> snap(entity, position, tolerance))
                .flatMap(Optional::stream)
                .min(Comparator.comparing(Snap::kind)
                        .thenComparingDouble(snap -> snap.position().distance(position))
                        .thenComparingDouble(snap -> area(snap.entity())));
    }

    /**
     * Whether the vertices of the entity's outline can be picked, as they can for every entity
     * but a point, which only has its center.
     */
    public static boolean hasVertices(Entity entity) {
        return !(entity instanceof Point);
    }

    /**
     * The area of the entity's bounds, which tells a smaller entity from a larger one.
     */
    public static double area(Entity entity) {
        Rectangle2D bounds = entity.getBounds();
        return bounds == null ? Double.MAX_VALUE : bounds.getWidth() * bounds.getHeight();
    }

    /**
     * The vertex or center of the shape at the position, for picking a point programmatically.
     *
     * @throws IllegalArgumentException if no vertex and not the center of the shape is at the position
     */
    public static ShapePoint pointAt(Entity entity, Point2D position) {
        return snap(entity, position, EXACT_TOLERANCE)
                .orElseThrow(() -> new IllegalArgumentException("No vertex of the shape at " + position))
                .point();
    }

    // Where a segment starts wins over where another one ends at the same place
    private static Optional<Snap> nearestVertex(Entity entity, Point2D position, double tolerance) {
        List<Outline.Segment> segments = Outline.segments(entity);
        Snap best = null;
        double bestDistance = tolerance;
        for (boolean end : new boolean[]{false, true}) {
            for (int index = 0; index < segments.size(); index++) {
                Point2D vertex = end ? segments.get(index).end() : segments.get(index).start();
                double distance = vertex.distance(position);
                if (distance < bestDistance || (best == null && distance <= bestDistance)) {
                    best = new Snap(entity, new VertexPoint(index, end), vertex, Kind.VERTEX);
                    bestDistance = distance;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static Optional<Snap> center(Entity entity, Rectangle2D bounds, Point2D position, double tolerance) {
        Point2D center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        return center.distance(position) <= tolerance
                ? Optional.of(new Snap(entity, CenterPoint.INSTANCE, center, Kind.CENTER))
                : Optional.empty();
    }

    private static Rectangle2D expanded(Rectangle2D bounds, double margin) {
        return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin,
                bounds.getWidth() + margin * 2, bounds.getHeight() + margin * 2);
    }
}
