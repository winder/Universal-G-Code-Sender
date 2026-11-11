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
package com.willwinder.ugs.designer.io.ugsd.v1;

import com.willwinder.ugs.designer.constraint.AlignmentConstraint;
import com.willwinder.ugs.designer.constraint.Axis;
import com.willwinder.ugs.designer.constraint.AxisFrame;
import com.willwinder.ugs.designer.constraint.CenterPoint;
import com.willwinder.ugs.designer.constraint.CoincidentConstraint;
import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.EqualSizeConstraint;
import com.willwinder.ugs.designer.constraint.PointConstraint;
import com.willwinder.ugs.designer.constraint.ShapePoint;
import com.willwinder.ugs.designer.entities.Entity;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * A constraint between two entities, referring to them by their {@link EntityV1#getId()}. The
 * kind is only written for constraints other than distances, so files without it read as
 * distance constraints, and the target axis only for an equal size between different axes.
 * The axes a constraint between points measures along are written as the angle of its
 * horizontal axis in degrees, and whether they are mirrored only when they are. Files without
 * the angle get the axes a new constraint would.
 */
public class ConstraintV1 implements Serializable {
    private String referenceId;
    private ShapePointV1 referencePoint;
    private String targetId;
    private ShapePointV1 targetPoint;
    private Axis axis;
    private Axis targetAxis;
    private double distance;
    private double offset;
    private Double frameAngle;
    private Boolean frameMirrored;
    private Constraint.Kind kind;

    public static ConstraintV1 fromInternal(Constraint constraint, String referenceId, String targetId) {
        ConstraintV1 result = new ConstraintV1();
        result.referenceId = referenceId;
        result.targetId = targetId;
        result.kind = constraint.getKind() == Constraint.Kind.DISTANCE ? null : constraint.getKind();
        switch (constraint) {
            case EqualSizeConstraint equalSize -> {
                result.referencePoint = ShapePointV1.fromInternal(CenterPoint.INSTANCE);
                result.targetPoint = ShapePointV1.fromInternal(CenterPoint.INSTANCE);
                result.axis = equalSize.getReferenceAxis();
                result.targetAxis = equalSize.getTargetAxis() == equalSize.getReferenceAxis() ? null : equalSize.getTargetAxis();
            }
            case DistanceConstraint distanceConstraint -> {
                result.writePoints(distanceConstraint);
                result.axis = distanceConstraint.getAxis();
                result.distance = distanceConstraint.getDistance();
                result.offset = distanceConstraint.getOffset();
            }
            case AlignmentConstraint alignment -> {
                result.writePoints(alignment);
                result.axis = alignment.getAxis();
            }
            case CoincidentConstraint coincidence -> {
                // Files are read with an axis for every constraint
                result.writePoints(coincidence);
                result.axis = Axis.HORIZONTAL;
            }
        }
        return result;
    }

    private void writePoints(PointConstraint constraint) {
        referencePoint = ShapePointV1.fromInternal(constraint.getReferenceShapePoint());
        targetPoint = ShapePointV1.fromInternal(constraint.getTargetShapePoint());
        AxisFrame frame = constraint.getHorizontalFrame();
        frameAngle = Math.toDegrees(frame.angle());
        frameMirrored = frame.isMirrored() ? Boolean.TRUE : null;
    }

    /**
     * Creates the constraint, or nothing if it refers to entities or points that are not in the
     * file.
     */
    public Optional<Constraint> toInternal(Map<String, Entity> entitiesById) {
        Entity reference = entitiesById.get(referenceId);
        Entity target = entitiesById.get(targetId);
        Optional<ShapePoint> fromPoint = referencePoint == null ? Optional.empty() : referencePoint.toInternal();
        Optional<ShapePoint> toPoint = targetPoint == null ? Optional.empty() : targetPoint.toInternal();
        if (reference == null || target == null || fromPoint.isEmpty() || toPoint.isEmpty() || axis == null) {
            return Optional.empty();
        }
        Constraint constraint = switch (kind == null ? Constraint.Kind.DISTANCE : kind) {
            case DISTANCE -> new DistanceConstraint(reference, fromPoint.get(), target, toPoint.get(), axis, distance, offset);
            case ALIGNMENT -> Constraint.aligned(reference, fromPoint.get(), target, toPoint.get(), axis);
            case COINCIDENT -> Constraint.coincident(reference, fromPoint.get(), target, toPoint.get());
            case EQUAL_SIZE -> Constraint.equalSize(reference, axis, target, targetAxis == null ? axis : targetAxis);
        };
        if (constraint instanceof PointConstraint pointConstraint && frameAngle != null) {
            pointConstraint.setHorizontalFrame(AxisFrame.ofAngle(Math.toRadians(frameAngle), Boolean.TRUE.equals(frameMirrored)));
        }
        return Optional.of(constraint);
    }
}
