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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.willwinder.ugs.designer.constraint.DimensionGeometry.*;

/**
 * How the dimension of a constraint is drawn, as lines, filled triangles and labels. Sizes on
 * screen, such as the label's and the arrowheads', are in pixels and scaled to the design with
 * the size of a pixel.
 */
public sealed interface DimensionLine permits DimensionLine.Linear, DimensionLine.Alignment, DimensionLine.Coincidence,
        DimensionLine.EqualSize {
    double LABEL_HEIGHT_PX = 18;
    double ARROW_LENGTH_PX = 7;

    /**
     * How far the line continues beyond an arrowhead that points in from the outside.
     */
    double OUTSIDE_LEADER_PX = 10;

    /**
     * The lines to draw, each as its two end points.
     */
    List<Point2D[]> lines(double worldUnitsPerPixel);

    /**
     * The triangles to fill, such as arrowheads.
     */
    List<Point2D[]> arrowheads(double worldUnitsPerPixel);

    /**
     * Where each label is centered, none when there is no label.
     */
    default List<Point2D> labelPositions(double worldUnitsPerPixel) {
        return List.of();
    }

    /**
     * Whether a label is shown.
     */
    default boolean hasLabel() {
        return false;
    }

    /**
     * The text of the label.
     */
    default String label() {
        return "";
    }

    /**
     * The width of the label's box on screen, wide enough for its text.
     */
    default double labelWidthPx() {
        return labelWidthPx(label());
    }

    /**
     * How far the point is from what is drawn, or zero when it is on a label.
     *
     * @param worldUnitsPerPixel the size of a pixel at the label, as the label is sized in pixels
     */
    double distanceTo(Point2D point, double worldUnitsPerPixel);

    /**
     * A point of the dimension, to measure the size of a pixel at.
     */
    Point2D anchor();

    /**
     * The width of a label's box on screen, wide enough for the text in the editor's font.
     */
    static double labelWidthPx(String text) {
        return text.length() * 6.5 + 8;
    }

    /**
     * The dimension of a constraint as it is drawn, and whether it is highlighted.
     */
    record Drawn(Constraint constraint, DimensionLine dimension, boolean highlighted) {
    }

    /**
     * The dimensions of the constraints, in their order, with the equals signs of each equal size
     * stacked after those of the earlier equal sizes on the same sizes, see
     * {@link #of(Constraint, boolean)}.
     */
    static List<Drawn> allOf(List<Constraint> constraints, Predicate<Constraint> highlighted) {
        Map<Entity, Map<Axis, Integer>> signsOnSize = new IdentityHashMap<>();
        List<Drawn> drawn = new ArrayList<>(constraints.size());
        for (Constraint constraint : constraints) {
            boolean isHighlighted = highlighted.test(constraint);
            DimensionLine dimension = constraint instanceof EqualSizeConstraint equalSize
                    ? EqualSize.of(equalSize, isHighlighted, nextSlot(signsOnSize, equalSize.getReference(), equalSize.getReferenceAxis()),
                    nextSlot(signsOnSize, equalSize.getTarget(), equalSize.getTargetAxis()))
                    : of(constraint, isHighlighted);
            drawn.add(new Drawn(constraint, dimension, isHighlighted));
        }
        return drawn;
    }

    private static int nextSlot(Map<Entity, Map<Axis, Integer>> signsOnSize, Entity entity, Axis axis) {
        Map<Axis, Integer> signs = signsOnSize.computeIfAbsent(entity, _ -> new EnumMap<>(Axis.class));
        int slot = signs.getOrDefault(axis, 0);
        signs.put(axis, slot + 1);
        return slot;
    }

    /**
     * The dimension of the constraint, drawn in full when it is highlighted, as when it is
     * selected or hovered. Only an equal size is drawn differently, collapsed to its equals
     * signs unless highlighted.
     */
    static DimensionLine of(Constraint constraint, boolean highlighted) {
        return switch (constraint) {
            case DistanceConstraint distance -> of(distance.getReferencePoint(), distance.getTargetPoint(), distance.getFrame(),
                    distance.getOffset(), distance.getDistance());
            case AlignmentConstraint alignment -> alignment(alignment.getReferencePoint(), alignment.getTargetPoint(), alignment.getFrame());
            case CoincidentConstraint coincidence -> coincidence(coincidence.getReferencePoint(), coincidence.getTargetPoint());
            case EqualSizeConstraint equalSize -> EqualSize.of(equalSize, highlighted, 0, 0);
        };
    }

    /**
     * @param offset where the dimension line runs, across the frame and relative to the middle
     *               of the two points
     */
    static Linear of(Point2D from, Point2D to, AxisFrame frame, double offset, double distance) {
        double across = middleAcross(from, to, frame) + offset;
        return new Linear(from, to, frame.point(frame.coordinate(from), across), frame.point(frame.coordinate(to), across), frame, distance);
    }

    /**
     * The dimension of the entity's width or height, along the edge an equal size draws its bar
     * on.
     */
    static Linear size(Entity entity, Axis axis) {
        Point2D[] bar = EqualSize.sizeBar(entity, axis);
        return of(bar[0], bar[1], AxisFrame.of(axis, entity), 0, bar[0].distance(bar[1]));
    }

    /**
     * The line of an alignment, from the reference's point to where the target's point lines up
     * with it, which is where the target's point is once the alignment is applied.
     */
    static Alignment alignment(Point2D from, Point2D to, AxisFrame frame) {
        return new Alignment(from, frame.point(frame.coordinate(from), frame.crossCoordinate(to)), frame);
    }

    /**
     * The mark of a coincidence at the reference's point, with a line to the target's point
     * until the two meet.
     */
    static Coincidence coincidence(Point2D from, Point2D to) {
        return new Coincidence(from, to);
    }

    /**
     * The axis a dimension between the two points is placed on with the cursor at the given
     * position: the horizontal distance when the cursor is above or below the points, the
     * vertical when it is beside them. Between them the longer distance wins. Above, below and
     * beside are seen along the given horizontal axis, which is turned for a rotated entity.
     */
    static Axis axisFor(AxisFrame horizontal, Point2D from, Point2D to, Point2D cursor) {
        double outsideAlong = outside(horizontal.coordinate(cursor), horizontal.coordinate(from), horizontal.coordinate(to));
        double outsideAcross = outside(horizontal.crossCoordinate(cursor), horizontal.crossCoordinate(from), horizontal.crossCoordinate(to));
        if (outsideAcross != outsideAlong) {
            return outsideAcross > outsideAlong ? Axis.HORIZONTAL : Axis.VERTICAL;
        }
        double along = Math.abs(horizontal.coordinate(to) - horizontal.coordinate(from));
        double across = Math.abs(horizontal.crossCoordinate(to) - horizontal.crossCoordinate(from));
        return along >= across ? Axis.HORIZONTAL : Axis.VERTICAL;
    }

    private static double outside(double value, double a, double b) {
        return Math.max(0, Math.max(Math.min(a, b) - value, value - Math.max(a, b)));
    }

    /**
     * The constraint whose label is under the point or whose dimension is closest to it, within
     * the tolerance, the dimensions drawn as {@link #allOf} draws them.
     *
     * @param highlighted        the constraints drawn highlighted, as the selected and hovered ones
     * @param worldUnitsPerPixel the size of a pixel, as the labels are sized in pixels
     */
    static Optional<Constraint> constraintAt(List<Constraint> constraints, Predicate<Constraint> highlighted, Point2D point,
                                             double tolerance, double worldUnitsPerPixel) {
        record Hit(Constraint constraint, double distance) {
        }
        return allOf(constraints, highlighted).stream()
                .map(drawn -> new Hit(drawn.constraint(), drawn.dimension().distanceTo(point, worldUnitsPerPixel)))
                .filter(hit -> hit.distance() <= tolerance)
                .min(Comparator.comparingDouble(Hit::distance))
                .map(Hit::constraint);
    }

    /**
     * The offset that puts the dimension line between the two points through the given
     * position, the inverse of {@link #of(Point2D, Point2D, AxisFrame, double, double)}.
     */
    static double offsetThrough(Point2D from, Point2D to, AxisFrame frame, Point2D position) {
        return frame.crossCoordinate(position) - middleAcross(from, to, frame);
    }

    private static double middleAcross(Point2D from, Point2D to, AxisFrame frame) {
        return (frame.crossCoordinate(from) + frame.crossCoordinate(to)) / 2;
    }

    /**
     * The dimension of a distance: an extension line from each point out to the dimension line,
     * which runs along the constrained axis, with an arrowhead at each end and the distance as
     * its label.
     *
     * @param from      the point on the reference
     * @param to        the point on the target
     * @param lineStart where the dimension line meets the reference's extension line
     * @param lineEnd   where the dimension line meets the target's extension line
     * @param frame     the directions the dimension line runs along and is offset across
     * @param distance  the distance to show
     */
    record Linear(Point2D from, Point2D to, Point2D lineStart, Point2D lineEnd, AxisFrame frame, double distance)
            implements DimensionLine {

        /**
         * The unit vector along the dimension line from its start towards its end, along the
         * axis when the two ends meet.
         */
        public Point2D direction() {
            return unit(lineStart, lineEnd).orElseGet(frame::along);
        }

        /**
         * Whether the arrowheads fit between the extension lines at the given zoom. When they do
         * not, they point in from the outside instead.
         */
        public boolean arrowsFitInside(double worldUnitsPerPixel) {
            return lineStart.distance(lineEnd) >= ARROW_LENGTH_PX * 2 * worldUnitsPerPixel;
        }

        /**
         * How far the dimension line reaches beyond each extension line, as it does when the
         * arrowheads are outside.
         */
        public double overhang(double worldUnitsPerPixel) {
            return arrowsFitInside(worldUnitsPerPixel) ? 0 : (ARROW_LENGTH_PX + OUTSIDE_LEADER_PX) * worldUnitsPerPixel;
        }

        /**
         * The label in the middle of the dimension line, or past the end of it when the
         * arrowheads are outside, so it does not cover them.
         */
        @Override
        public List<Point2D> labelPositions(double worldUnitsPerPixel) {
            if (arrowsFitInside(worldUnitsPerPixel)) {
                return List.of(midpoint(lineStart, lineEnd));
            }
            Point2D direction = direction();
            double halfLabel = Math.abs(direction.getX()) * labelWidthPx() / 2 + Math.abs(direction.getY()) * LABEL_HEIGHT_PX / 2;
            return List.of(offset(lineEnd, direction, overhang(worldUnitsPerPixel) + (LABEL_GAP_PX + halfLabel) * worldUnitsPerPixel));
        }

        /**
         * The extension lines, the dimension line and, when the arrowheads are outside, the
         * short lines beyond them.
         */
        @Override
        public List<Point2D[]> lines(double worldUnitsPerPixel) {
            List<Point2D[]> lines = new ArrayList<>(List.of(
                    new Point2D[]{from, lineStart},
                    new Point2D[]{to, lineEnd},
                    new Point2D[]{lineStart, lineEnd}));
            if (!arrowsFitInside(worldUnitsPerPixel)) {
                double leader = overhang(worldUnitsPerPixel);
                Point2D direction = direction();
                lines.add(new Point2D[]{lineStart, offset(lineStart, direction, -leader)});
                lines.add(new Point2D[]{lineEnd, offset(lineEnd, direction, leader)});
            }
            return lines;
        }

        /**
         * The arrowheads at the ends of the dimension line, tip first. They point outwards
         * between the extension lines, or in from the outside when they do not fit.
         */
        @Override
        public List<Point2D[]> arrowheads(double worldUnitsPerPixel) {
            Point2D direction = direction();
            Point2D backwards = reversed(direction);
            boolean inside = arrowsFitInside(worldUnitsPerPixel);
            return List.of(
                    arrowhead(lineStart, inside ? direction : backwards, worldUnitsPerPixel),
                    arrowhead(lineEnd, inside ? backwards : direction, worldUnitsPerPixel));
        }

        @Override
        public boolean hasLabel() {
            return true;
        }

        @Override
        public String label() {
            return format(distance);
        }

        /**
         * How far the point is from the dimension line, including the lines beyond outside
         * arrowheads, or zero when it is on the label.
         */
        @Override
        public double distanceTo(Point2D point, double worldUnitsPerPixel) {
            if (distanceToLabel(point, labelPositions(worldUnitsPerPixel).getFirst(), labelWidthPx(), worldUnitsPerPixel) == 0) {
                return 0;
            }
            double overhang = overhang(worldUnitsPerPixel);
            Point2D direction = direction();
            return distanceToSegment(offset(lineStart, direction, -overhang), offset(lineEnd, direction, overhang), point);
        }

        @Override
        public Point2D anchor() {
            return lineStart;
        }
    }

    /**
     * The mark of an alignment: a line through the two points, across the axis, with a diamond
     * marking each point and no label.
     *
     * @param from  the reference's point
     * @param to    where the target's point lines up with it
     * @param frame the axis the points are lined up on
     */
    record Alignment(Point2D from, Point2D to, AxisFrame frame) implements DimensionLine {

        /**
         * The unit vector from the reference's point towards the target's, across the axis when
         * they meet.
         */
        public Point2D direction() {
            return unit(from, to).orElseGet(frame::across);
        }

        @Override
        public List<Point2D[]> lines(double worldUnitsPerPixel) {
            return List.<Point2D[]>of(line(worldUnitsPerPixel));
        }

        // Through both points and a little beyond them
        private Point2D[] line(double worldUnitsPerPixel) {
            double extension = ALIGNMENT_EXTENSION_PX * worldUnitsPerPixel;
            Point2D direction = direction();
            return new Point2D[]{offset(from, direction, -extension), offset(to, direction, extension)};
        }

        @Override
        public List<Point2D[]> arrowheads(double worldUnitsPerPixel) {
            List<Point2D[]> diamonds = new ArrayList<>(diamond(from, direction(), worldUnitsPerPixel));
            diamonds.addAll(diamond(to, direction(), worldUnitsPerPixel));
            return diamonds;
        }

        @Override
        public double distanceTo(Point2D point, double worldUnitsPerPixel) {
            Point2D[] line = line(worldUnitsPerPixel);
            return distanceToSegment(line[0], line[1], point);
        }

        @Override
        public Point2D anchor() {
            return from;
        }
    }

    /**
     * The mark of a coincidence: a diamond in a diamond outline where the points meet, and a
     * line between the points before they do.
     *
     * @param from the reference's point
     * @param to   the target's point
     */
    record Coincidence(Point2D from, Point2D to) implements DimensionLine {
        private static final Point2D SIDEWAYS = new Point2D.Double(1, 0);

        @Override
        public List<Point2D[]> lines(double worldUnitsPerPixel) {
            Point2D[] corners = diamondCorners(from, SIDEWAYS, COINCIDENT_RING_PX * worldUnitsPerPixel);
            List<Point2D[]> lines = new ArrayList<>();
            for (int i = 0; i < corners.length; i++) {
                lines.add(new Point2D[]{corners[i], corners[(i + 1) % corners.length]});
            }
            if (!from.equals(to)) {
                lines.add(new Point2D[]{from, to});
            }
            return lines;
        }

        @Override
        public List<Point2D[]> arrowheads(double worldUnitsPerPixel) {
            return diamond(from, unit(from, to).orElse(SIDEWAYS), worldUnitsPerPixel);
        }

        @Override
        public double distanceTo(Point2D point, double worldUnitsPerPixel) {
            return Math.max(0, from.distance(point) - COINCIDENT_RING_PX * worldUnitsPerPixel);
        }

        @Override
        public Point2D anchor() {
            return from;
        }
    }

    /**
     * The mark of an equal size: an equals sign on each of the two sizes, in the middle of the
     * top edge of the entity for the width and of its right edge for the height. The signs of
     * several equal sizes on the same size are stacked along the edge, each in its slot, so each
     * can be picked. Unless it is collapsed, as it is until highlighted, it also has a bar with
     * arrowheads along each size and a line between the signs.
     *
     * @param referenceStart where the bar along the reference's size starts
     * @param referenceEnd   where the bar along the reference's size ends
     * @param targetStart    where the bar along the target's size starts
     * @param targetEnd      where the bar along the target's size ends
     * @param collapsed      whether only the equals signs are shown
     * @param referenceSlot  where the sign is stacked on the reference's size, zero in the middle
     * @param targetSlot     where the sign is stacked on the target's size
     */
    record EqualSize(Point2D referenceStart, Point2D referenceEnd, Point2D targetStart, Point2D targetEnd, boolean collapsed,
                     int referenceSlot, int targetSlot) implements DimensionLine {

        static EqualSize of(EqualSizeConstraint constraint, boolean highlighted, int referenceSlot, int targetSlot) {
            Point2D[] reference = sizeBar(constraint.getReference(), constraint.getReferenceAxis());
            Point2D[] target = sizeBar(constraint.getTarget(), constraint.getTargetAxis());
            return new EqualSize(reference[0], reference[1], target[0], target[1], !highlighted, referenceSlot, targetSlot);
        }

        // Along the top edge for the width and the right edge for the height, in the entity's own axes
        static Point2D[] sizeBar(Entity entity, Axis axis) {
            AxisFrame frame = AxisFrame.ownOf(entity);
            Rectangle2D bounds = frame.boundsOf(entity);
            Point2D start = axis == Axis.HORIZONTAL
                    ? frame.point(bounds.getMinX(), bounds.getMaxY())
                    : frame.point(bounds.getMaxX(), bounds.getMinY());
            return new Point2D[]{start, frame.point(bounds.getMaxX(), bounds.getMaxY())};
        }

        /**
         * The equals sign on each size.
         */
        @Override
        public List<Point2D> labelPositions(double worldUnitsPerPixel) {
            return List.of(stacked(referenceStart, referenceEnd, referenceSlot, worldUnitsPerPixel),
                    stacked(targetStart, targetEnd, targetSlot, worldUnitsPerPixel));
        }

        // Along the bar from its middle, a label and a gap further for each slot
        private Point2D stacked(Point2D start, Point2D end, int slot, double worldUnitsPerPixel) {
            Point2D middle = midpoint(start, end);
            if (slot == 0) {
                return middle;
            }
            return unit(start, end).map(direction -> {
                double stepPx = Math.abs(direction.getX()) * labelWidthPx() + Math.abs(direction.getY()) * LABEL_HEIGHT_PX + STACK_GAP_PX;
                return offset(middle, direction, slot * stepPx * worldUnitsPerPixel);
            }).orElse(middle);
        }

        /**
         * Both bars and the line between the signs, unless collapsed.
         */
        @Override
        public List<Point2D[]> lines(double worldUnitsPerPixel) {
            return collapsed ? List.of() : List.of(new Point2D[]{referenceStart, referenceEnd}, new Point2D[]{targetStart, targetEnd},
                    labelPositions(worldUnitsPerPixel).toArray(Point2D[]::new));
        }

        /**
         * The arrowheads at the ends of both bars, unless collapsed.
         */
        @Override
        public List<Point2D[]> arrowheads(double worldUnitsPerPixel) {
            if (collapsed) {
                return List.of();
            }
            List<Point2D[]> arrowheads = new ArrayList<>(barArrowheads(referenceStart, referenceEnd, worldUnitsPerPixel));
            arrowheads.addAll(barArrowheads(targetStart, targetEnd, worldUnitsPerPixel));
            return arrowheads;
        }

        private static List<Point2D[]> barArrowheads(Point2D start, Point2D end, double worldUnitsPerPixel) {
            return unit(start, end)
                    .map(direction -> List.of(arrowhead(start, direction, worldUnitsPerPixel),
                            arrowhead(end, reversed(direction), worldUnitsPerPixel)))
                    .orElse(List.of());
        }

        @Override
        public boolean hasLabel() {
            return true;
        }

        @Override
        public String label() {
            return "=";
        }

        @Override
        public double distanceTo(Point2D point, double worldUnitsPerPixel) {
            double toLabel = labelPositions(worldUnitsPerPixel).stream()
                    .mapToDouble(label -> distanceToLabel(point, label, labelWidthPx(), worldUnitsPerPixel))
                    .min()
                    .orElse(Double.MAX_VALUE);
            return lines(worldUnitsPerPixel).stream()
                    .mapToDouble(line -> distanceToSegment(line[0], line[1], point))
                    .reduce(toLabel, Math::min);
        }

        @Override
        public Point2D anchor() {
            return referenceStart;
        }
    }
}
