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
package com.willwinder.ugs.designer.io.stl;

import java.awt.geom.Rectangle2D;

/**
 * An immutable triangle soup read from an STL file. Coordinates are stored flat, nine floats per
 * triangle, as {@code x0, y0, z0, x1, y1, z1, x2, y2, z2}.
 *
 * @author Joacim Breiler
 */
public class StlMesh {
    public static final int COORDINATES_PER_TRIANGLE = 9;

    private final float[] coordinates;
    private final Rectangle2D.Double bounds;
    private final double minZ;
    private final double maxZ;

    public StlMesh(float[] coordinates) {
        if (coordinates.length % COORDINATES_PER_TRIANGLE != 0) {
            throw new IllegalArgumentException("The number of coordinates must be a multiple of " + COORDINATES_PER_TRIANGLE);
        }
        this.coordinates = coordinates;

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double lowestZ = Double.POSITIVE_INFINITY;
        double highestZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < coordinates.length; i += 3) {
            minX = Math.min(minX, coordinates[i]);
            maxX = Math.max(maxX, coordinates[i]);
            minY = Math.min(minY, coordinates[i + 1]);
            maxY = Math.max(maxY, coordinates[i + 1]);
            lowestZ = Math.min(lowestZ, coordinates[i + 2]);
            highestZ = Math.max(highestZ, coordinates[i + 2]);
        }

        if (coordinates.length == 0) {
            this.bounds = new Rectangle2D.Double();
            this.minZ = 0;
            this.maxZ = 0;
        } else {
            this.bounds = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
            this.minZ = lowestZ;
            this.maxZ = highestZ;
        }
    }

    public boolean isEmpty() {
        return coordinates.length == 0;
    }

    public int getTriangleCount() {
        return coordinates.length / COORDINATES_PER_TRIANGLE;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    /**
     * Returns the extents of the mesh projected onto the XY plane.
     *
     * @return the bounds in the XY plane
     */
    public Rectangle2D.Double getBounds() {
        return (Rectangle2D.Double) bounds.clone();
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getHeight() {
        return maxZ - minZ;
    }
}
