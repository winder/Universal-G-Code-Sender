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

import java.util.List;

/**
 * Splits a straight move only where it crosses from one surface triangle into another.
 * A move that stays in one triangle, or misses the surface, is left whole.
 */
public class LineSplitterTriangleIntersections extends LineSplitter {
    private final DelaunaySurfaceInterpolator surface;

    public LineSplitterTriangleIntersections(DelaunaySurfaceInterpolator surface) {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is required.");
        }
        this.surface = surface;
    }

    @Override
    public String getHelp() {
        return "Split G0 and G1 commands where they cross into another surface triangle.";
    }

    @Override
    protected List<Double> splitParameters(Position start, Position end) {
        Position startOnSurface = start.getPositionIn(surface.getUnits());
        Position endOnSurface = end.getPositionIn(surface.getUnits());
        return surface.crossingParameters(
                startOnSurface.getX(), startOnSurface.getY(), endOnSurface.getX(), endOnSurface.getY());
    }
}
