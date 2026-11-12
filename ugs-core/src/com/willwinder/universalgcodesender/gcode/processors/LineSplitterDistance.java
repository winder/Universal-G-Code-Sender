/*
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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a straight move into pieces no longer than a fixed distance.
 */
public class LineSplitterDistance extends LineSplitter {
    private final double maxSegmentLength;

    /**
     * @param segmentLength length of the longest single line.
     */
    public LineSplitterDistance(double segmentLength) {
        this.maxSegmentLength = segmentLength;
    }

    @Override
    protected List<Double> splitParameters(Position start, Position end) {
        double length = start.distanceXYZ(end);
        if (!(length > maxSegmentLength)) {
            return List.of();
        }
        int numSegments = (int) Math.ceil(length / maxSegmentLength);
        double segmentLength = length / Math.ceil(length / maxSegmentLength);
        List<Double> parameters = new ArrayList<>(numSegments - 1);
        for (int i = 1; i < numSegments; i++) {
            parameters.add(1 / (length / (i * segmentLength)));
        }
        return parameters;
    }
}
