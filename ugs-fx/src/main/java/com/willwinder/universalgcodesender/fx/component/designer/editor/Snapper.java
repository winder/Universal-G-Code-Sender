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
 * Snaps coordinates to the designer's grid. The grid size comes from the drawing, where the
 * snap to grid actions set it; zero disables snapping.
 */
public final class Snapper {
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
     * The distance a keyboard nudge moves the selection: the grid size, or one millimeter when
     * snapping is off.
     */
    public double nudgeStep() {
        double grid = gridSize.getAsDouble();
        return grid > 0 ? grid : 1;
    }

    /**
     * Rounds to whole millimeters, or tenths when the user asks for fine movement, then snaps.
     */
    public double snapRounded(double value, boolean fine) {
        double rounded = fine ? Math.round(value * 10) / 10.0 : Math.round(value);
        return snap(rounded);
    }
}
