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

import com.willwinder.ugs.designer.constraint.CenterPoint;
import com.willwinder.ugs.designer.constraint.ShapePoint;
import com.willwinder.ugs.designer.constraint.VertexPoint;

import java.io.Serializable;
import java.util.Optional;

/**
 * A point of an entity's shape that a constraint measures from: its center or a vertex of its
 * outline.
 */
public class ShapePointV1 implements Serializable {
    private static final String CENTER = "CENTER";
    private static final String VERTEX = "VERTEX";

    private String type;
    private Integer segment;
    private Boolean end;

    public static ShapePointV1 fromInternal(ShapePoint point) {
        ShapePointV1 result = new ShapePointV1();
        switch (point) {
            case CenterPoint _ -> result.type = CENTER;
            case VertexPoint vertex -> {
                result.type = VERTEX;
                result.segment = vertex.segment();
                result.end = vertex.end() ? Boolean.TRUE : null;
            }
        }
        return result;
    }

    public Optional<ShapePoint> toInternal() {
        if (CENTER.equals(type)) {
            return Optional.of(CenterPoint.INSTANCE);
        }
        if (VERTEX.equals(type) && segment != null) {
            return Optional.of(new VertexPoint(segment, Boolean.TRUE.equals(end)));
        }
        return Optional.empty();
    }
}
