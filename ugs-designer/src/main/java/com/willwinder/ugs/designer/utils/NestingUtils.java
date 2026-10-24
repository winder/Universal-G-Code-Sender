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
package com.willwinder.ugs.designer.utils;

import com.willwinder.ugs.designer.entities.Entity;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Packs entities within a given width so that a group of shapes takes up as little stock as
 * possible. The outlines of the shapes are what is kept apart, not their bounding boxes, so a
 * shape may be tucked into the notch of another.
 * <p>
 * Each entity is placed in turn, bottom left first: every candidate position next to or on top
 * of the shapes already placed is tried, in every allowed orientation, and the lowest one wins.
 * When rotation is allowed, an entity is first turned so that its smallest enclosing rectangle
 * lines up with the axes, and is then tried in steps of {@value #ROTATION_STEP} degrees from
 * there, as well as in the orientation it already has. Orientations that need a much bigger
 * box than the tightest one, like a bar on the diagonal, are left out. The entities are packed
 * longest first and biggest first, and the tighter of the two results is kept. An entity wider
 * than the area in every orientation is placed anyway, so every entity ends up with a position.
 */
public class NestingUtils {
    public static final double ROTATION_STEP = 45;
    private static final double TOLERANCE = 0.001;
    private static final double SLIDE_STEP = 0.5;
    private static final int SLIDE_LIMIT = 200;
    private static final double FLATNESS = 0.2;
    private static final double EMPTY_SHAPE_SIZE = 0.01;
    private static final double ORIENTATION_AREA_SLACK = 1.2;

    private NestingUtils() {
    }

    /**
     * Lays out the entities from the origin upwards and to the right.
     *
     * @param entities      the entities to arrange
     * @param origin        the bottom left corner of the area
     * @param maxWidth      the width of the area to arrange the entities within
     * @param spacing       the distance kept between the outlines of the entities
     * @param allowRotation whether the entities may be rotated to make them fit better
     * @return the bottom left position and rotation for each entity
     */
    public static NestingLayout layout(List<? extends Entity> entities, Point2D origin, double maxWidth, double spacing, boolean allowRotation) {
        List<Item> items = entities.stream()
                .map(entity -> Item.of(entity, spacing, allowRotation))
                .toList();

        List<Comparator<Item>> orders = List.of(
                Comparator.comparingDouble(Item::longestSide).thenComparingDouble(Item::area).reversed(),
                Comparator.comparingDouble(Item::area).thenComparingDouble(Item::longestSide).reversed());

        return orders.stream()
                .map(order -> pack(items.stream().sorted(order).toList(), origin, maxWidth, spacing))
                .reduce((best, next) -> next.isBetterThan(best, maxWidth) ? next : best)
                .orElseThrow();
    }

    private static NestingLayout pack(List<Item> items, Point2D origin, double maxWidth, double spacing) {
        List<Area> placed = new ArrayList<>();
        Map<Entity, Placement> placements = new LinkedHashMap<>();
        Rectangle2D packed = null;

        for (Item item : items) {
            boolean limitWidth = item.fitsWidth(maxWidth);
            Candidate best = null;
            for (double y : candidateCoordinates(origin.getY(), placed, spacing, false)) {
                for (double x : candidateCoordinates(origin.getX(), placed, spacing, true)) {
                    for (Orientation orientation : item.orientations()) {
                        if (limitWidth && x + orientation.width() > origin.getX() + maxWidth + TOLERANCE) {
                            continue;
                        }
                        Candidate candidate = new Candidate(orientation, x, y);
                        if (best != null && !candidate.isBetterThan(best)) {
                            continue;
                        }
                        if (!orientation.collides(x, y, placed)) {
                            best = candidate;
                        }
                    }
                }
            }

            Candidate settled = slide(best, origin, placed);
            Area outline = settled.orientation().outlineAt(settled.x(), settled.y());
            placed.add(outline);
            packed = packed == null ? outline.getBounds2D() : packed.createUnion(outline.getBounds2D());
            placements.put(item.entity(), new Placement(new Point2D.Double(settled.x(), settled.y()), settled.orientation().rotation()));
        }

        double width = packed == null ? 0 : packed.getMaxX() - origin.getX();
        double height = packed == null ? 0 : packed.getMaxY() - origin.getY();
        return new NestingLayout(placements, width, height);
    }

    /**
     * The coordinates where a shape may be put: the origin, and next to every corner of the
     * placed outlines, either flush with it or a spacing away from it.
     */
    private static TreeSet<Double> candidateCoordinates(double origin, List<Area> placed, double spacing, boolean horizontal) {
        TreeSet<Double> coordinates = new TreeSet<>();
        coordinates.add(origin);
        for (Area area : placed) {
            for (Point2D vertex : vertices(area)) {
                double coordinate = horizontal ? vertex.getX() : vertex.getY();
                if (coordinate >= origin - TOLERANCE) {
                    coordinates.add(coordinate);
                    coordinates.add(coordinate + spacing);
                }
            }
        }
        return coordinates;
    }

    /**
     * Pushes the chosen placement down and then left as far as the placed outlines allow, so a
     * shape settles into any room beneath or beside the corner it was placed at.
     */
    private static Candidate slide(Candidate candidate, Point2D origin, List<Area> placed) {
        Candidate settled = candidate;
        boolean moved = true;
        while (moved) {
            moved = false;
            for (int i = 0; i < SLIDE_LIMIT; i++) {
                double y = settled.y() - SLIDE_STEP;
                if (y < origin.getY() - TOLERANCE || settled.orientation().collides(settled.x(), y, placed)) {
                    break;
                }
                settled = new Candidate(settled.orientation(), settled.x(), y);
                moved = true;
            }
            for (int i = 0; i < SLIDE_LIMIT; i++) {
                double x = settled.x() - SLIDE_STEP;
                if (x < origin.getX() - TOLERANCE || settled.orientation().collides(x, settled.y(), placed)) {
                    break;
                }
                settled = new Candidate(settled.orientation(), x, settled.y());
                moved = true;
            }
        }
        return settled;
    }

    private static List<Point2D> vertices(Shape shape) {
        List<Point2D> vertices = new ArrayList<>();
        double[] coordinates = new double[6];
        for (PathIterator iterator = shape.getPathIterator(null, FLATNESS); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coordinates);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                vertices.add(new Point2D.Double(coordinates[0], coordinates[1]));
            }
        }
        return vertices;
    }

    /**
     * Rotates the shape the same way the entity would be rotated, so the estimate matches what
     * happens when the placement is applied.
     */
    private static Shape rotate(Shape shape, Point2D center, double rotation) {
        if (rotation == 0) {
            return shape;
        }
        return AffineTransform.getRotateInstance(-Math.toRadians(rotation), center.getX(), center.getY())
                .createTransformedShape(shape);
    }

    /**
     * The rotations to try: the one the entity already has, and steps of {@value #ROTATION_STEP}
     * degrees from the rotation that lines its smallest enclosing rectangle up with the axes.
     * That rectangle always shares a side with the convex hull, so only the directions of the
     * hull edges need to be tried.
     */
    private static List<Double> rotationCandidates(Shape shape, Point2D center) {
        double aligned = 0;
        double smallestArea = Double.MAX_VALUE;
        List<Point2D> hull = convexHull(vertices(shape));
        for (int i = 0; i < hull.size(); i++) {
            Point2D from = hull.get(i);
            Point2D to = hull.get((i + 1) % hull.size());
            double edgeAngle = Math.toDegrees(Math.atan2(to.getY() - from.getY(), to.getX() - from.getX()));
            for (double rotation : new double[]{edgeAngle, -edgeAngle}) {
                Rectangle2D bounds = rotate(shape, center, rotation).getBounds2D();
                double area = bounds.getWidth() * bounds.getHeight();
                if (area < smallestArea - TOLERANCE) {
                    smallestArea = area;
                    aligned = rotation;
                }
            }
        }

        TreeSet<Double> rotations = new TreeSet<>();
        rotations.add(0d);
        for (double step = 0; step < 360; step += ROTATION_STEP) {
            rotations.add(normalize(aligned + step));
        }
        return List.copyOf(rotations);
    }

    /**
     * The convex hull as a counter clockwise polygon, built with the monotone chain algorithm.
     */
    private static List<Point2D> convexHull(List<Point2D> points) {
        List<Point2D> sorted = points.stream()
                .distinct()
                .sorted(Comparator.comparingDouble(Point2D::getX).thenComparingDouble(Point2D::getY))
                .toList();
        if (sorted.size() < 3) {
            return sorted;
        }

        List<Point2D> hull = new ArrayList<>();
        for (Point2D point : sorted) {
            while (hull.size() >= 2 && !turnsLeft(hull.get(hull.size() - 2), hull.getLast(), point)) {
                hull.removeLast();
            }
            hull.add(point);
        }
        int lowerSize = hull.size() + 1;
        for (int i = sorted.size() - 2; i >= 0; i--) {
            Point2D point = sorted.get(i);
            while (hull.size() >= lowerSize && !turnsLeft(hull.get(hull.size() - 2), hull.getLast(), point)) {
                hull.removeLast();
            }
            hull.add(point);
        }
        hull.removeLast();
        return hull;
    }

    private static boolean turnsLeft(Point2D a, Point2D b, Point2D c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX()) > 0;
    }

    private static double normalize(double rotation) {
        double normalized = ((rotation % 360) + 360) % 360;
        return Math.abs(normalized - 360) < TOLERANCE ? 0 : Math.round(normalized * 1000) / 1000d;
    }

    private record Item(Entity entity, List<Orientation> orientations, double longestSide, double area) {
        static Item of(Entity entity, double spacing, boolean allowRotation) {
            Shape shape = entity.getShape();
            Point2D center = entity.getCenter();
            List<Double> rotations = allowRotation ? rotationCandidates(shape, center) : List.of(0d);
            List<Orientation> all = rotations.stream()
                    .map(rotation -> Orientation.of(rotation, rotate(shape, center, rotation), spacing))
                    .toList();

            Orientation tightest = all.stream()
                    .min(Comparator.comparingDouble(Orientation::boxArea))
                    .orElseThrow();
            List<Orientation> orientations = all.stream()
                    .filter(orientation -> orientation.boxArea() <= tightest.boxArea() * ORIENTATION_AREA_SLACK + TOLERANCE)
                    .toList();
            return new Item(entity, orientations, Math.max(tightest.width(), tightest.height()), tightest.boxArea());
        }

        boolean fitsWidth(double maxWidth) {
            return orientations.stream().anyMatch(orientation -> orientation.width() <= maxWidth + TOLERANCE);
        }
    }

    /**
     * The outline of an entity in one rotation, and the same outline grown by the spacing which
     * is what must stay clear of the other outlines.
     */
    private record Orientation(double rotation, Area outline, Area clearance, Rectangle2D bounds) {
        static Orientation of(double rotation, Shape shape, double spacing) {
            Area outline = new Area(shape);
            if (outline.isEmpty()) {
                outline = new Area(nonEmptyBounds(shape));
            }
            return new Orientation(rotation, outline, inflate(outline, spacing), outline.getBounds2D());
        }

        /**
         * A line or a point has no area, so its bounding box stands in for its outline.
         */
        private static Rectangle2D nonEmptyBounds(Shape shape) {
            Rectangle2D bounds = shape.getBounds2D();
            return new Rectangle2D.Double(bounds.getX(), bounds.getY(),
                    Math.max(bounds.getWidth(), EMPTY_SHAPE_SIZE), Math.max(bounds.getHeight(), EMPTY_SHAPE_SIZE));
        }

        /**
         * Grows the outline by a hair less than the spacing, so two outlines exactly a spacing
         * apart are not reported as touching because of rounding.
         */
        private static Area inflate(Area outline, double spacing) {
            if (spacing <= TOLERANCE) {
                return outline;
            }
            float strokeWidth = (float) (2 * (spacing - TOLERANCE));
            Area inflated = new Area(outline);
            inflated.add(new Area(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND).createStrokedShape(outline)));
            return inflated;
        }

        double width() {
            return bounds.getWidth();
        }

        double height() {
            return bounds.getHeight();
        }

        double boxArea() {
            return bounds.getWidth() * bounds.getHeight();
        }

        Area outlineAt(double x, double y) {
            return outline.createTransformedArea(translationTo(x, y));
        }

        boolean collides(double x, double y, List<Area> placed) {
            AffineTransform translation = translationTo(x, y);
            Rectangle2D clearanceBounds = translation.createTransformedShape(clearance.getBounds2D()).getBounds2D();
            Area moved = null;
            for (Area other : placed) {
                if (!clearanceBounds.intersects(other.getBounds2D())) {
                    continue;
                }
                if (moved == null) {
                    moved = clearance.createTransformedArea(translation);
                }
                Area overlap = new Area(moved);
                overlap.intersect(other);
                if (!overlap.isEmpty() && overlap.getBounds2D().getWidth() > TOLERANCE && overlap.getBounds2D().getHeight() > TOLERANCE) {
                    return true;
                }
            }
            return false;
        }

        private AffineTransform translationTo(double x, double y) {
            return AffineTransform.getTranslateInstance(x - bounds.getX(), y - bounds.getY());
        }
    }

    /**
     * A possible placement, judged by how low the top of the shape ends up, then by how far
     * left it sits.
     */
    private record Candidate(Orientation orientation, double x, double y) {
        boolean isBetterThan(Candidate other) {
            if (Math.abs(top() - other.top()) > TOLERANCE) {
                return top() < other.top();
            }
            return x < other.x - TOLERANCE;
        }

        private double top() {
            return y + orientation.height();
        }
    }

    /**
     * Where an entity ends up.
     *
     * @param position the bottom left corner of the entity after it has been rotated
     * @param rotation the angle in degrees to rotate the entity around its center
     */
    public record Placement(Point2D position, double rotation) {
    }

    /**
     * The result of a nesting: where to put each entity and how much room they take up.
     *
     * @param placements the placement of each entity
     * @param width      the total width of the arranged entities
     * @param height     the total height of the arranged entities
     */
    public record NestingLayout(Map<Entity, Placement> placements, double width, double height) {
        public boolean fitsWithin(double maxWidth, double maxHeight) {
            return width <= maxWidth + TOLERANCE && height <= maxHeight + TOLERANCE;
        }

        /**
         * A layout within the width beats one that overflows it; between two that fit (or two
         * that overflow) the shorter one wins, and then the narrower one.
         */
        boolean isBetterThan(NestingLayout other, double maxWidth) {
            boolean fits = width <= maxWidth + TOLERANCE;
            boolean otherFits = other.width <= maxWidth + TOLERANCE;
            if (fits != otherFits) {
                return fits;
            }
            if (Math.abs(height - other.height) > TOLERANCE) {
                return height < other.height;
            }
            return width < other.width - TOLERANCE;
        }
    }
}
