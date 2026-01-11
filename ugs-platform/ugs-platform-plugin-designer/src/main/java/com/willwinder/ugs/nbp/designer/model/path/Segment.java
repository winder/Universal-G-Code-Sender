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

import java.awt.geom.Point2D;

/**
 * A segment to be used in a editable path
 *
 * @author Joacim Breiler
 */
public class Segment {
    private final SegmentType type;
    private final Point2D[] points;

    public Segment(SegmentType type, Point2D[] points) {
        this.type = type;
        this.points = points;
    }

    public SegmentType getType() {
        return type;
    }

    public Point2D[] getPoints() {
        return points;
    }

    public Point2D getPoint(int index) {
        if (index >= points.length) {
            return new Point2D.Double();
        }
        return points[index];
    }
}