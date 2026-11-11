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

/**
 * A constraint between a point of the reference and a point of the target, on their shapes,
 * a vertex or the center.
 * <p>
 * It measures along axes of its own. A new constraint between entities rotated alike, or on a
 * single entity, measures along their own axes, and otherwise along the axes of the design, see
 * {@link AxisFrame#of(Axis, Entity, Entity)}. The axes are turned and mirrored with the entities
 * when both are turned or mirrored alike, as when they are rotated together in a group, and stay
 * as they are when only one of them is, see {@link #followEntities()}.
 */
public abstract sealed class PointConstraint extends Constraint permits DistanceConstraint, AlignmentConstraint, CoincidentConstraint {
    private ShapePoint referenceShapePoint;
    private ShapePoint targetShapePoint;
    private AxisFrame horizontalFrame;
    private AxisFrame referenceFrame;
    private AxisFrame targetFrame;
    private Turn pendingTurn;

    // An entity turned or mirrored from one of its own frames to another
    private record Turn(Entity entity, AxisFrame from, AxisFrame to) {
        boolean isAlike(Turn other) {
            return from.carry(other.from, to).isAlike(other.to);
        }

        AxisFrame carry(AxisFrame frame) {
            return from.carry(frame, to);
        }
    }

    protected PointConstraint(Entity reference, ShapePoint referenceShapePoint, Entity target, ShapePoint targetShapePoint) {
        super(reference, target);
        this.referenceShapePoint = referenceShapePoint;
        this.targetShapePoint = targetShapePoint;
        this.horizontalFrame = AxisFrame.of(Axis.HORIZONTAL, reference, target);
        this.referenceFrame = AxisFrame.ownOf(reference);
        this.targetFrame = AxisFrame.ownOf(target);
    }

    public ShapePoint getReferenceShapePoint() {
        return referenceShapePoint;
    }

    public ShapePoint getTargetShapePoint() {
        return targetShapePoint;
    }

    /**
     * The directions the constraint measures its horizontal axis along and draws it across.
     */
    public AxisFrame getHorizontalFrame() {
        return horizontalFrame;
    }

    /**
     * Sets the axes the constraint measures along, as when it is read from a file, see
     * {@link #getHorizontalFrame()}.
     */
    public void setHorizontalFrame(AxisFrame horizontalFrame) {
        this.horizontalFrame = horizontalFrame;
    }

    /**
     * The directions the constraint measures the axis along and draws it across.
     */
    public AxisFrame frameOf(Axis axis) {
        return axis == Axis.HORIZONTAL ? horizontalFrame : horizontalFrame.swapped();
    }

    /**
     * Turns or mirrors the axes the constraint measures along with the entities, when both have
     * been turned or mirrored alike since it last looked. The entities of a group are turned one
     * at a time, so a turn of one of them waits for the same turn of the other, and is left alone
     * if the other turns differently or is turned again first.
     */
    void followEntities() {
        Turn referenceTurn = turnOf(reference, referenceFrame);
        Turn targetTurn = reference == target ? referenceTurn : turnOf(target, targetFrame);
        referenceFrame = AxisFrame.ownOf(reference);
        targetFrame = AxisFrame.ownOf(target);
        if (referenceTurn != null && targetTurn != null) {
            if (referenceTurn.isAlike(targetTurn)) {
                horizontalFrame = referenceTurn.carry(horizontalFrame);
            }
            pendingTurn = null;
        } else if (referenceTurn != null || targetTurn != null) {
            Turn turn = referenceTurn != null ? referenceTurn : targetTurn;
            if (pendingTurn != null && pendingTurn.entity() != turn.entity() && pendingTurn.isAlike(turn)) {
                horizontalFrame = turn.carry(horizontalFrame);
                pendingTurn = null;
            } else {
                pendingTurn = turn;
            }
        }
    }

    private static Turn turnOf(Entity entity, AxisFrame seen) {
        AxisFrame now = AxisFrame.ownOf(entity);
        return seen.isAlike(now) ? null : new Turn(entity, seen, now);
    }

    /**
     * Where the reference point is right now.
     *
     * @throws IllegalStateException if the point is no longer on the entity, see {@link #isLocatable()}
     */
    public Point2D getReferencePoint() {
        return referenceShapePoint.locate(reference).orElseThrow(() -> new IllegalStateException("The reference point is gone"));
    }

    /**
     * Where the target point is right now.
     *
     * @throws IllegalStateException if the point is no longer on the entity, see {@link #isLocatable()}
     */
    public Point2D getTargetPoint() {
        return targetShapePoint.locate(target).orElseThrow(() -> new IllegalStateException("The target point is gone"));
    }

    @Override
    public boolean isLocatable() {
        return referenceShapePoint.locate(reference).isPresent() && targetShapePoint.locate(target).isPresent();
    }

    /**
     * The signed distance the points have right now along the axis.
     */
    public double measure(Axis axis) {
        AxisFrame frame = frameOf(axis);
        return frame.coordinate(getTargetPoint()) - frame.coordinate(getReferencePoint());
    }

    /**
     * The signed distance the points are to be at along the axis.
     */
    protected double heldDistance() {
        return 0;
    }

    @Override
    public void apply() {
        getAxes().forEach(each -> applyMoving(target, each));
    }

    /**
     * Moves one of the entities along one of the constraint's axes so the points are at the
     * distance, the target as {@link #apply()} does or the reference the other way, as when the
     * target was moved.
     *
     * @return whether the entity was moved
     */
    public boolean applyMoving(Entity entity, Axis movedAxis) {
        AxisFrame frame = frameOf(movedAxis);
        double correction = heldDistance() - measure(movedAxis);
        if (Math.abs(correction) <= TOLERANCE) {
            return false;
        }
        if (entity == target) {
            target.move(frame.delta(correction));
        } else {
            reference.move(frame.delta(-correction));
        }
        return true;
    }

    @Override
    void flip() {
        super.flip();
        ShapePoint flippedTargetShapePoint = referenceShapePoint;
        referenceShapePoint = targetShapePoint;
        targetShapePoint = flippedTargetShapePoint;
        AxisFrame flippedTargetFrame = referenceFrame;
        referenceFrame = targetFrame;
        targetFrame = flippedTargetFrame;
    }
}
