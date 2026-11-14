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

import java.util.List;
import java.util.OptionalDouble;

/**
 * Keeps the target's point at a fixed distance from the reference's point along one axis, so
 * moving the reference moves the target while the target cannot be moved away on that axis.
 * <p>
 * When both points are on the same entity it holds a size of it instead, such as the width of
 * a rectangle. The size is measured along the entity's own axis, so it follows the entity's
 * rotation, and the entity is stretched along that axis while its reference point stays put.
 * <p>
 * The distance is signed, positive when the target's point lies to the right of or above the
 * reference's point.
 */
public final class DistanceConstraint extends PointConstraint {
    private final Axis axis;
    private double distance;
    private double offset;

    /**
     * @param distance the signed distance from the reference point to the target point
     * @param offset   where the dimension is drawn, across the axis and relative to the
     *                 midpoint of the two points
     */
    public DistanceConstraint(Entity reference, ShapePoint referenceShapePoint, Entity target, ShapePoint targetShapePoint,
                              Axis axis, double distance, double offset) {
        super(reference, referenceShapePoint, target, targetShapePoint);
        this.axis = axis;
        this.distance = distance;
        this.offset = offset;
    }

    /**
     * Creates a constraint holding the distance the two points currently have.
     *
     * @throws IllegalArgumentException if a point is not on its entity
     */
    public static DistanceConstraint measured(Entity reference, ShapePoint referenceShapePoint, Entity target,
                                              ShapePoint targetShapePoint, Axis axis, double offset) {
        DistanceConstraint constraint = new DistanceConstraint(reference, referenceShapePoint, target, targetShapePoint, axis, 0, offset);
        if (!constraint.isLocatable()) {
            throw new IllegalArgumentException("The points are not on the entities");
        }
        constraint.setDistance(constraint.measure());
        return constraint;
    }

    @Override
    public Kind getKind() {
        return Kind.DISTANCE;
    }

    public Axis getAxis() {
        return axis;
    }

    @Override
    public List<Axis> getAxes() {
        return List.of(axis);
    }

    /**
     * The directions the distance is measured along and the dimension is offset across.
     */
    public AxisFrame getFrame() {
        return frameOf(axis);
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    protected double heldDistance() {
        return distance;
    }

    /**
     * The distance to hold when the given one is entered, or empty when it can not be held. The
     * target stays on the side of the reference it is on, unless a negative distance is entered
     * to flip it over. A size can not be flipped or made zero, so its sign is ignored and a size
     * below {@link #MINIMUM_SIZE} is refused.
     */
    public OptionalDouble distanceFor(double entered) {
        double magnitude = isSize() ? Math.abs(entered) : entered;
        if (isSize() && magnitude < MINIMUM_SIZE) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(distance < 0 ? -magnitude : magnitude);
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * The signed distance the points have right now, which differs from
     * {@link #getDistance()} until the constraint has been applied.
     */
    public double measure() {
        return measure(axis);
    }

    /**
     * Whether both points are on the same entity, so the constraint holds a size of it rather
     * than its position.
     */
    @Override
    public boolean isSize() {
        return reference == target;
    }

    @Override
    public void apply() {
        if (isSize()) {
            stretch(target, getFrame(), getReferencePoint(), measure(), distance);
        } else {
            super.apply();
        }
    }

    @Override
    void flip() {
        super.flip();
        distance = -distance;
    }
}
