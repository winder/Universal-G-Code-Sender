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

import com.willwinder.ugs.designer.constraint.Constraint.Kind;
import com.willwinder.ugs.designer.constraint.ShapeSnapping.Snap;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.universalgcodesender.i18n.Localization;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Places a distance constraint between points on the shapes of entities, independent of the UI
 * that drives it. Points are the vertices and centers of the shapes, shown as markers. The
 * first point picked belongs to the reference, the second to the target
 * that will follow it, and picking both on the same entity constrains its size instead. Where
 * the cursor is then decides whether the horizontal or the vertical distance is constrained,
 * the way a dimension is placed in a CAD program, and a third press places it.
 * <p>
 * An alignment is placed by the second press, on a point of another entity. The points are
 * lined up whichever way they are closest to being lined up already: vertically when they are
 * further apart up and down than sideways, otherwise horizontally. Hovering the point shows the
 * line they will be lined up on.
 * <p>
 * A coincidence is placed the same way as an alignment, gluing the second point to the first.
 * <p>
 * An equal size is placed the same way too, giving a size of the second entity the size of the
 * first, but by pressing on the entities rather than on points of them. Which size, the width or
 * the height, is picked by where the entity is pressed: the width nearer its top or bottom than
 * its sides, otherwise the height. The two sizes may be of the same entity when they differ.
 * Hovering an entity shows the size it picks.
 */
public class ConstraintPlacement {

    /**
     * How close to a point the cursor must be to pick it, in pixels.
     */
    public static final double SNAP_PX = 8;

    /**
     * Shapes with more segments than this, such as text, only show the hovered point.
     */
    private static final int MAX_VERTEX_MARKERS = 200;

    /**
     * A point that can be picked, highlighted when it is hovered or already picked.
     */
    public record Marker(Point2D position, boolean highlighted) {
    }

    /**
     * What to show of the constraint a press would place, or of the size it would pick: its
     * dimension and the text to show beside the cursor, such as the distance, or why the
     * constraint can not be added.
     */
    public record Preview(DimensionLine dimension, String readout) {
    }

    private enum Phase {PICK_REFERENCE, PICK_TARGET, PLACE}

    private final ConstraintManager constraintManager;
    private final Kind kind;
    private Phase phase = Phase.PICK_REFERENCE;
    private Snap reference;
    private Snap target;

    public ConstraintPlacement(ConstraintManager constraintManager, Kind kind) {
        this.constraintManager = constraintManager;
        this.kind = kind;
    }

    /**
     * Whether no point has been picked yet, so a press that does not pick one can be used for
     * something else, such as dragging a dimension.
     */
    public boolean isIdle() {
        return phase == Phase.PICK_REFERENCE;
    }

    /**
     * Whether both points are picked and the next press places the constraint.
     */
    public boolean isPlacing() {
        return phase == Phase.PLACE;
    }

    /**
     * Picks the point under the cursor, or starts over when there is none, or places the
     * constraint once both points are picked.
     *
     * @param snap     the point under the cursor, if any
     * @param position where the cursor is
     * @return the constraint to add, when one was placed and can be added
     */
    public Optional<Constraint> press(Optional<Snap> snap, Point2D position) {
        if (phase == Phase.PLACE) {
            return place(dimensionAt(position));
        }
        if (phase == Phase.PICK_TARGET && placesOnTarget() && snap.isPresent() && canBeTarget(snap.get())) {
            return place(constraintTo(snap.get()));
        }

        if (phase == Phase.PICK_REFERENCE) {
            constraintManager.setSelectedConstraint(null);
            snap.ifPresent(hit -> {
                reference = hit;
                phase = Phase.PICK_TARGET;
            });
        } else if (snap.isPresent() && canBeTarget(snap.get())) {
            target = snap.get();
            phase = Phase.PLACE;
        } else if (snap.isEmpty()) {
            reset();
        }
        return Optional.empty();
    }

    /**
     * What a press would place: a dimension at the position while both points are picked, or an
     * alignment, coincidence or equal size with the hovered point while picking its target. For
     * an equal size, the size picked or that the hovered point would pick otherwise.
     *
     * @param hovered  the point under the cursor, if any
     * @param position where the cursor is
     */
    public Optional<Preview> preview(Optional<Snap> hovered, Point2D position) {
        if (phase == Phase.PLACE) {
            return Optional.of(previewOf(dimensionAt(position)));
        }
        if (phase == Phase.PICK_TARGET && placesOnTarget()) {
            Optional<Preview> placed = hovered.filter(this::canBeTarget).map(snap -> previewOf(constraintTo(snap)));
            return kind == Kind.EQUAL_SIZE ? placed.or(() -> Optional.of(sizePreview(reference))) : placed;
        }
        if (kind == Kind.EQUAL_SIZE) {
            return hovered.map(ConstraintPlacement::sizePreview);
        }
        return Optional.empty();
    }

    private static Preview sizePreview(Snap snap) {
        Axis axis = sizeAxisAt(snap);
        return new Preview(DimensionLine.size(snap.entity(), axis), axis.sizeName());
    }

    private Preview previewOf(Constraint constraint) {
        DimensionLine dimension = DimensionLine.of(constraint, true);
        String readout = constraintManager.conflictMessage(constraint).orElseGet(() -> switch (constraint) {
            case DistanceConstraint _ -> dimension.label();
            case AlignmentConstraint alignment ->
                    Localization.getString(alignment.getAxis() == Axis.HORIZONTAL ? "designer.constraint.alignX" : "designer.constraint.alignY");
            case CoincidentConstraint _ -> Localization.getString("designer.constraint.glue");
            case EqualSizeConstraint equalSize -> Localization.getString("designer.constraint.equalsize.readout")
                    .replace("{0}", equalSize.getReferenceAxis().sizeName())
                    .replace("{1}", equalSize.getTargetAxis().sizeName());
        });
        return new Preview(dimension, readout);
    }

    private Optional<Constraint> place(Constraint constraint) {
        if (!constraintManager.canAdd(constraint)) {
            return Optional.empty();
        }
        reset();
        return Optional.of(constraint);
    }

    /**
     * The points to show: the vertices and center of the hovered entity, with the hovered point
     * and the points already picked highlighted. An equal size picks entities, so it has none.
     *
     * @param hovered     the point under the cursor, if any
     * @param underCursor the entity under the cursor, shown when no point of it is hovered
     */
    public List<Marker> markers(Optional<Snap> hovered, Optional<Entity> underCursor) {
        if (kind == Kind.EQUAL_SIZE) {
            return List.of();
        }
        List<Marker> markers = new ArrayList<>();
        Optional<Entity> shown = phase == Phase.PLACE ? Optional.empty() : hovered.map(Snap::entity).or(() -> underCursor);
        shown.ifPresent(entity -> {
            List<Outline.Segment> segments = Outline.segments(entity);
            if (ShapeSnapping.hasVertices(entity) && segments.size() <= MAX_VERTEX_MARKERS) {
                Outline.vertices(segments).forEach(vertex -> markers.add(new Marker(vertex, false)));
            }
            markers.add(new Marker(entity.getCenter(), false));
        });
        hovered.ifPresent(snap -> markers.add(new Marker(snap.position(), true)));
        if (reference != null) {
            markers.add(new Marker(reference.position(), true));
        }
        if (target != null) {
            markers.add(new Marker(target.position(), true));
        }
        return markers;
    }

    /**
     * What a press at the position picks: the point under the cursor, or for an equal size the
     * entity under the cursor, picked where the cursor is, as that decides its size.
     *
     * @param snap        the point under the cursor, if any
     * @param underCursor the entity under the cursor, if any
     */
    public Optional<Snap> pickAt(Optional<Snap> snap, Optional<Entity> underCursor, Point2D position) {
        if (kind == Kind.EQUAL_SIZE) {
            return underCursor.map(entity -> new Snap(entity, CenterPoint.INSTANCE, position, ShapeSnapping.Kind.CENTER));
        }
        return snap;
    }

    public void reset() {
        phase = Phase.PICK_REFERENCE;
        reference = null;
        target = null;
    }

    // Alignments, coincidences and equal sizes are placed by picking their target, as they have no dimension to place
    private boolean placesOnTarget() {
        return kind != Kind.DISTANCE;
    }

    private Constraint constraintTo(Snap snap) {
        if (kind == Kind.COINCIDENT) {
            return Constraint.coincident(reference.entity(), reference.point(), snap.entity(), snap.point());
        }
        if (kind == Kind.EQUAL_SIZE) {
            return Constraint.equalSize(reference.entity(), sizeAxisAt(reference), snap.entity(), sizeAxisAt(snap));
        }
        Axis axis = alignmentAxis(reference.position(), snap.position());
        return Constraint.aligned(reference.entity(), reference.point(), snap.entity(), snap.point(), axis);
    }

    private DistanceConstraint dimensionAt(Point2D position) {
        AxisFrame horizontal = AxisFrame.of(Axis.HORIZONTAL, reference.entity(), target.entity());
        Axis axis = DimensionLine.axisFor(horizontal, reference.position(), target.position(), position);
        AxisFrame frame = AxisFrame.of(axis, reference.entity(), target.entity());
        double offset = DimensionLine.offsetThrough(reference.position(), target.position(), frame, position);
        return DistanceConstraint.measured(reference.entity(), reference.point(), target.entity(), target.point(), axis, offset);
    }

    // The width for a point nearer the top or bottom of the entity than its sides, measured in its own axes
    private static Axis sizeAxisAt(Snap snap) {
        AxisFrame frame = AxisFrame.ownOf(snap.entity());
        Rectangle2D bounds = frame.boundsOf(snap.entity());
        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return bounds.getWidth() > 0 ? Axis.HORIZONTAL : Axis.VERTICAL;
        }
        double sideways = Math.abs(frame.coordinate(snap.position()) - bounds.getCenterX()) / bounds.getWidth();
        double upAndDown = Math.abs(frame.crossCoordinate(snap.position()) - bounds.getCenterY()) / bounds.getHeight();
        return upAndDown >= sideways ? Axis.HORIZONTAL : Axis.VERTICAL;
    }

    // Lining the points up along a vertical line holds their horizontal position and the other way around
    private static Axis alignmentAxis(Point2D reference, Point2D target) {
        double horizontal = Math.abs(target.getX() - reference.getX());
        double vertical = Math.abs(target.getY() - reference.getY());
        return vertical >= horizontal ? Axis.HORIZONTAL : Axis.VERTICAL;
    }

    private boolean canBeTarget(Snap snap) {
        if (kind == Kind.EQUAL_SIZE) {
            return snap.entity() != reference.entity() || sizeAxisAt(snap) != sizeAxisAt(reference);
        }
        if (placesOnTarget()) {
            return snap.entity() != reference.entity();
        }
        return snap.entity() != reference.entity() || !snap.point().equals(reference.point());
    }
}
