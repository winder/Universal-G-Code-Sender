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
 * Defines an endpoint for a segment with the point and if it is start or end.
 * Useful to determine if the segment needs to be reversed using the point as a new start point.
 *
 * @param point   the point
 * @param isStart if it is the starting point or not
 * @param segment the segment
 */
public record SegmentEndpoint(Point2D point, boolean isStart, Segment segment) {
}
