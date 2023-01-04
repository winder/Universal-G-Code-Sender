/*
    Copyright 2020-2023 Will Winder

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
package com.willwinder.universalgcodesender.visualizer;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A mapper for converting a linesegment to a stream of partial positions in millimeters
 *
 * @author Joacim Breiler
 */
public class LineSegmentToPartialPositionMapper implements Function<LineSegment, Stream<? extends PartialPosition>> {
    @Override
    public Stream<? extends PartialPosition> apply(LineSegment lineSegment) {
        PartialPosition start = PartialPosition.fromXY(lineSegment.getStart().getPositionIn(UnitUtils.Units.MM));
        PartialPosition end = PartialPosition.fromXY(lineSegment.getEnd().getPositionIn(UnitUtils.Units.MM));
        return Stream.of(start, end);
    }
}
