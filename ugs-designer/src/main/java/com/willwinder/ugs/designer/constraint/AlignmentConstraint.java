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

/**
 * Lines the target's point up with the reference's point, holding no distance between them
 * along one axis: the same horizontal position lines them up vertically and the other way
 * around. It is drawn as a line through the points rather than as a dimension.
 */
public final class AlignmentConstraint extends PointConstraint {
    private final Axis axis;

    AlignmentConstraint(Entity reference, ShapePoint referenceShapePoint, Entity target, ShapePoint targetShapePoint, Axis axis) {
        super(reference, referenceShapePoint, target, targetShapePoint);
        this.axis = axis;
    }

    @Override
    public Kind getKind() {
        return Kind.ALIGNMENT;
    }

    public Axis getAxis() {
        return axis;
    }

    @Override
    public List<Axis> getAxes() {
        return List.of(axis);
    }

    /**
     * The directions the points are lined up across and along.
     */
    public AxisFrame getFrame() {
        return frameOf(axis);
    }
}
