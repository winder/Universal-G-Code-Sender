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
package com.willwinder.ugs.nbp.designer.model.path;

import java.awt.geom.PathIterator;
import java.util.List;

/**
 * A segment type to be used in the editable path
 *
 * @author Joacim Breiler
 */
public enum SegmentType {
    MOVE_TO(List.of(PointType.COORDINATE)),
    LINE_TO(List.of(PointType.COORDINATE)),
    QUAD_TO(List.of(PointType.CONTROL_POINT, PointType.COORDINATE)),
    CUBIC_TO(List.of(PointType.CONTROL_POINT, PointType.CONTROL_POINT, PointType.COORDINATE)),
    CLOSE(List.of(PointType.COORDINATE));

    private final List<PointType> pointTypes;

    SegmentType(List<PointType> pointTypes) {
        this.pointTypes = pointTypes;
    }

    public static SegmentType fromPathIteratorType(int type) {
        return switch (type) {
            case PathIterator.SEG_MOVETO -> MOVE_TO;
            case PathIterator.SEG_LINETO -> LINE_TO;
            case PathIterator.SEG_QUADTO -> QUAD_TO;
            case PathIterator.SEG_CUBICTO -> CUBIC_TO;
            case PathIterator.SEG_CLOSE -> CLOSE;
            default -> MOVE_TO;
        };
    }

    public List<PointType> getPointTypes() {
        return pointTypes;
    }
}
