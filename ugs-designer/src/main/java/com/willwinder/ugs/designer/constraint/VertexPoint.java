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
import java.util.List;
import java.util.Optional;

/**
 * A vertex of the outline of an entity, where one of the segments of its shape starts or ends.
 *
 * @param segment the index of the segment in {@link Outline#segments(Entity)}
 * @param end     whether the vertex is where the segment ends, rather than where it starts
 */
public record VertexPoint(int segment, boolean end) implements ShapePoint {
    @Override
    public Optional<Point2D> locate(Entity entity) {
        List<Outline.Segment> segments = Outline.segments(entity);
        if (segment < 0 || segment >= segments.size()) {
            return Optional.empty();
        }
        Outline.Segment located = segments.get(segment);
        return Optional.of(end ? located.end() : located.start());
    }
}
