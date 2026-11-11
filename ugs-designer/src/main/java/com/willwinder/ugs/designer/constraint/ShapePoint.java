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
import java.util.Optional;

/**
 * A point of an entity that a constraint measures from, defined relative to the entity's shape
 * so it follows the entity as it is moved, resized or rotated.
 */
public sealed interface ShapePoint permits CenterPoint, VertexPoint {

    /**
     * Where the point is on the entity right now, or empty if the entity's shape no longer has
     * it, as when the path it was on lost nodes.
     */
    Optional<Point2D> locate(Entity entity);
}
