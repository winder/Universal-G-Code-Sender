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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import java.awt.geom.Point2D;
import java.util.function.DoubleSupplier;

/**
 * Snaps coordinates to the designer's grid. The grid size comes from the supplier, in the
 * application from the snap settings; zero disables snapping.
 */
public final class Snapper {
    private static final double EPSILON = 1e-9;

    private final DoubleSupplier gridSize;

    public Snapper(DoubleSupplier gridSize) {
        this.gridSize = gridSize;
    }

    public double snap(double value) {
        double grid = gridSize.getAsDouble();
        if (grid <= 0) {
            return value;
        }
        return Math.round(value / grid) * grid;
    }

    public Point2D snap(Point2D point) {
        return new Point2D.Double(snap(point.getX()), snap(point.getY()));
    }

    /**
     * Where a keyboard nudge moves a coordinate: the given number of grid lines in the given
     * direction, the first step landing on the nearest grid line past the value so a position
     * off the grid comes onto it. Without a grid it moves a millimeter per step.
     *
     * @param value     the coordinate to move
     * @param direction positive to move up the axis, negative to move down it
     * @param steps     how many grid lines, or millimeters, to move
     */
    public double nudge(double value, int direction, int steps) {
        double grid = gridSize.getAsDouble();
        double sign = Math.signum(direction);
        if (grid <= 0) {
            return value + sign * steps;
        }
        // Allow for floating point noise so a value already on a grid line counts as on it.
        double lines = value / grid;
        double first = sign > 0 ? Math.floor(lines + EPSILON) + 1 : Math.ceil(lines - EPSILON) - 1;
        return (first + sign * (steps - 1)) * grid;
    }

    /**
     * Snaps to the grid, or without a grid rounds to whole millimeters, or tenths when the user
     * asks for fine movement.
     */
    public double snapRounded(double value, boolean fine) {
        if (gridSize.getAsDouble() > 0) {
            return snap(value);
        }
        return fine ? Math.round(value * 10) / 10.0 : Math.round(value);
    }
}
