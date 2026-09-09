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
package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A straight cutting move in millimeters, ready for the simulation. Rapids are not cutting moves
 * and are dropped by {@link #fromLineSegments(List)}. Moves around rotational axes are simulated
 * through their cartesian projection, which is an approximation.
 */
public record CutSegment(double x0, double y0, double z0, double x1, double y1, double z1,
                         int toolNumber, int commandNumber) {

    public static List<CutSegment> fromLineSegments(List<LineSegment> segments) {
        List<CutSegment> cuts = new ArrayList<>();
        for (LineSegment segment : segments) {
            if (segment.isFastTraverse()) {
                continue;
            }
            LineSegment cartesian = VisualizerUtils.toCartesian(segment);
            Position start = cartesian.getStart().getPositionIn(UnitUtils.Units.MM);
            Position end = cartesian.getEnd().getPositionIn(UnitUtils.Units.MM);
            if (isUndefined(start) || isUndefined(end)) {
                continue;
            }
            cuts.add(new CutSegment(start.getX(), start.getY(), start.getZ(),
                    end.getX(), end.getY(), end.getZ(),
                    segment.getToolNumber(), segment.getLineNumber()));
        }
        return cuts;
    }

    private static boolean isUndefined(Position position) {
        return Double.isNaN(position.getX()) || Double.isNaN(position.getY()) || Double.isNaN(position.getZ());
    }

    /**
     * True for a move straight down or up with no XY travel, such as a plunge or a retract at feed
     * rate. These start above the stock and say nothing about where its top is.
     */
    public boolean isVertical() {
        return x0 == x1 && y0 == y1;
    }

    public double minZ() {
        return Math.min(z0, z1);
    }

    public double maxZ() {
        return Math.max(z0, z1);
    }
}
