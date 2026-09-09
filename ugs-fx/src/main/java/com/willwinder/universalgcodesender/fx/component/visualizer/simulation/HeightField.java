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

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;

import java.util.Arrays;

/**
 * The stock as a grid of surface heights. Nodes are spaced {@link #cellSize()} apart, starting at
 * the stock's minimum X and Y corner, and every node starts at the stock top. Cutting can only lower
 * a node, and never below the stock bottom, so the field is a monotone record of what has been
 * removed regardless of the order the cuts are applied in.
 */
public final class HeightField {
    private final Bounds3 bounds;
    private final double cellSize;
    private final int columns;
    private final int rows;
    private final float[] heights;

    public HeightField(Bounds3 bounds, double cellSize) {
        if (cellSize <= 0) {
            throw new IllegalArgumentException("Cell size must be positive, was " + cellSize);
        }
        this.bounds = bounds;
        this.cellSize = cellSize;
        this.columns = Math.max(2, (int) Math.ceil(bounds.width() / cellSize) + 1);
        this.rows = Math.max(2, (int) Math.ceil(bounds.height() / cellSize) + 1);
        this.heights = new float[columns * rows];
        Arrays.fill(heights, (float) bounds.maxZ());
    }

    private HeightField(HeightField other) {
        this.bounds = other.bounds;
        this.cellSize = other.cellSize;
        this.columns = other.columns;
        this.rows = other.rows;
        this.heights = other.heights.clone();
    }

    public HeightField copy() {
        return new HeightField(this);
    }

    public Bounds3 bounds() {
        return bounds;
    }

    public double cellSize() {
        return cellSize;
    }

    /**
     * Number of nodes along X.
     */
    public int columns() {
        return columns;
    }

    /**
     * Number of nodes along Y.
     */
    public int rows() {
        return rows;
    }

    public int nodeCount() {
        return heights.length;
    }

    public double topZ() {
        return bounds.maxZ();
    }

    public double bottomZ() {
        return bounds.minZ();
    }

    public double x(int column) {
        return bounds.minX() + column * cellSize;
    }

    public double y(int row) {
        return bounds.minY() + row * cellSize;
    }

    /**
     * The column of the nearest node at or below {@code x}, clamped to the grid.
     */
    public int columnFloor(double x) {
        return clamp((int) Math.floor((x - bounds.minX()) / cellSize), columns - 1);
    }

    /**
     * The column of the nearest node at or above {@code x}, clamped to the grid.
     */
    public int columnCeil(double x) {
        return clamp((int) Math.ceil((x - bounds.minX()) / cellSize), columns - 1);
    }

    public int rowFloor(double y) {
        return clamp((int) Math.floor((y - bounds.minY()) / cellSize), rows - 1);
    }

    public int rowCeil(double y) {
        return clamp((int) Math.ceil((y - bounds.minY()) / cellSize), rows - 1);
    }

    public float heightAt(int column, int row) {
        return heights[row * columns + column];
    }

    /**
     * Lowers the node to {@code z} if it is currently higher. Cuts below the stock bottom stop at
     * the bottom.
     */
    public void lower(int column, int row, double z) {
        int index = row * columns + column;
        float clamped = (float) Math.max(z, bounds.minZ());
        if (clamped < heights[index]) {
            heights[index] = clamped;
        }
    }

    /**
     * True when the node has been cut down to the stock bottom, so the material is gone there.
     */
    public boolean isThrough(int column, int row) {
        return heights[row * columns + column] <= (float) bounds.minZ();
    }

    /**
     * True while no node has been lowered.
     */
    public boolean isUntouched() {
        float top = (float) bounds.maxZ();
        for (float height : heights) {
            if (height < top) {
                return false;
            }
        }
        return true;
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }
}
