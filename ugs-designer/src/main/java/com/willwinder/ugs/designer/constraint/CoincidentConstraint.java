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
 * Glues the target's point to the reference's point, holding no distance between them along
 * both axes at once. It counts as a constraint on each axis, so the target can not be driven by
 * another constraint on either of them.
 */
public final class CoincidentConstraint extends PointConstraint {

    CoincidentConstraint(Entity reference, ShapePoint referenceShapePoint, Entity target, ShapePoint targetShapePoint) {
        super(reference, referenceShapePoint, target, targetShapePoint);
    }

    @Override
    public Kind getKind() {
        return Kind.COINCIDENT;
    }

    @Override
    public List<Axis> getAxes() {
        return List.of(Axis.values());
    }
}
