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
import com.willwinder.ugs.designer.entities.EntityEvent;
import com.willwinder.ugs.designer.entities.EventType;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Keeps something of the target entity in step with the reference entity. The constraint is
 * directional: the reference drives and the target follows. The {@link ConstraintManager} may
 * flip which is which to fit the constraint into the others.
 * <p>
 * The constraints between points of the entities are {@link PointConstraint}s: a
 * {@link DistanceConstraint}, an {@link AlignmentConstraint} or a {@link CoincidentConstraint}.
 * An {@link EqualSizeConstraint} keeps a size of the target the same as a size of the reference
 * instead.
 */
public abstract sealed class Constraint permits PointConstraint, EqualSizeConstraint {
    /**
     * What a constraint holds, as picked by a tool and written to a file.
     */
    public enum Kind {
        /** A distance along one axis, or the size of an entity, see {@link DistanceConstraint}. */
        DISTANCE,
        /** The points lined up along one axis, see {@link AlignmentConstraint}. */
        ALIGNMENT,
        /** The points at the same place, see {@link CoincidentConstraint}. */
        COINCIDENT,
        /** A width or height the same as another, see {@link EqualSizeConstraint}. */
        EQUAL_SIZE
    }

    /**
     * The smallest size a constraint can hold, as an entity can not be scaled back from nothing.
     */
    public static final double MINIMUM_SIZE = 1e-6;
    static final double TOLERANCE = 1e-9;

    protected Entity reference;
    protected Entity target;

    protected Constraint(Entity reference, Entity target) {
        this.reference = reference;
        this.target = target;
    }

    /**
     * Creates an alignment, lining the target's point up with the reference's point along the
     * axis.
     */
    public static AlignmentConstraint aligned(Entity reference, ShapePoint referenceShapePoint, Entity target,
                                              ShapePoint targetShapePoint, Axis axis) {
        return new AlignmentConstraint(reference, referenceShapePoint, target, targetShapePoint, axis);
    }

    /**
     * Creates a coincidence, gluing the target's point to the reference's point.
     */
    public static CoincidentConstraint coincident(Entity reference, ShapePoint referenceShapePoint, Entity target,
                                                  ShapePoint targetShapePoint) {
        return new CoincidentConstraint(reference, referenceShapePoint, target, targetShapePoint);
    }

    /**
     * Creates an equal size, keeping the target's size along the target axis the same as the
     * reference's size along the reference axis.
     */
    public static EqualSizeConstraint equalSize(Entity reference, Axis referenceAxis, Entity target, Axis targetAxis) {
        return new EqualSizeConstraint(reference, referenceAxis, target, targetAxis);
    }

    public Entity getReference() {
        return reference;
    }

    public Entity getTarget() {
        return target;
    }

    public abstract Kind getKind();

    /**
     * Every axis the constraint holds positions on, none for a constraint that holds a size.
     */
    public abstract List<Axis> getAxes();

    /**
     * Whether the constraint holds a size of a single entity rather than its position.
     */
    public boolean isSize() {
        return false;
    }

    /**
     * Whether the constraint holds where the target is, rather than a size.
     */
    public boolean holdsPosition() {
        return reference != target && !getAxes().isEmpty();
    }

    /**
     * Whether whatever the constraint refers to is still on its entities, as a point on a path
     * is gone when the path lost the segment it was on.
     */
    public boolean isLocatable() {
        return true;
    }

    /**
     * Moves or stretches the target to satisfy the constraint, the reference staying as it is.
     */
    public abstract void apply();

    public boolean involves(Entity entity) {
        return reference == entity || target == entity;
    }

    // Swaps the reference and the target, which keeps whatever is held as it is
    void flip() {
        Entity flippedTarget = reference;
        reference = target;
        target = flippedTarget;
    }

    // Scales the entity along the frame about the fixed point from the current size to the wanted one. A size of zero or
    // a flipped one can not be scaled back from, so those are left alone.
    static void stretch(Entity entity, AxisFrame frame, Point2D fixed, double current, double wanted) {
        double factor = wanted / current;
        if (Math.abs(current) < TOLERANCE || !(factor > 0) || Double.isInfinite(factor) || Math.abs(factor - 1) < TOLERANCE) {
            return;
        }
        double angle = frame.angle();
        AffineTransform stretch = new AffineTransform();
        stretch.translate(fixed.getX(), fixed.getY());
        stretch.rotate(angle);
        stretch.scale(factor, 1);
        stretch.rotate(-angle);
        stretch.translate(-fixed.getX(), -fixed.getY());
        entity.applyTransform(stretch);
        entity.notifyEvent(new EntityEvent(entity, EventType.RESIZED));
    }
}
