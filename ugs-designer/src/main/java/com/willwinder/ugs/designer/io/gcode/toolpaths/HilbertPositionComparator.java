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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.Comparator;

/**
 * A comparator for ordering geometries to minimize the rapid movement between them.
 * <p>
 * The geometries are ordered by how far along a Hilbert curve they lie. The curve fills the work area
 * without ever jumping, and it never leaves a part of the area to come back to it later, so
 * geometries lying close to each other end up next to each other in the order no matter how they are
 * spread out over the work area.
 *
 * @author Joacim Breiler
 */
public class HilbertPositionComparator implements Comparator<Geometry> {
    /**
     * The number of steps along each axis of the curve. A finer curve tells positions close to each
     * other apart more accurately and only costs a few more turns of the loop walking the curve.
     */
    private static final int RESOLUTION = 1 << 16;

    private final Envelope envelope;

    public HilbertPositionComparator(Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    public int compare(Geometry first, Geometry second) {
        return Long.compare(index(first), index(second));
    }

    /**
     * Returns how far along the curve the given position lies. Positions are ordered by this, so it
     * can be used to order anything that has a position and not just geometries.
     *
     * @param x the position along the X axis
     * @param y the position along the Y axis
     * @return the distance along the curve
     */
    public long index(double x, double y) {
        int curveX = toCurve(x - envelope.getMinX(), envelope.getWidth());
        int curveY = toCurve(y - envelope.getMinY(), envelope.getHeight());

        long index = 0;
        for (int quadrantSize = RESOLUTION / 2; quadrantSize > 0; quadrantSize /= 2) {
            int quadrantX = (curveX & quadrantSize) > 0 ? 1 : 0;
            int quadrantY = (curveY & quadrantSize) > 0 ? 1 : 0;
            index += (long) quadrantSize * quadrantSize * ((3 * quadrantX) ^ quadrantY);

            // Rotate the quadrant so that the curve within it picks up where the previous one ended
            if (quadrantY == 0) {
                if (quadrantX == 1) {
                    curveX = quadrantSize - 1 - curveX;
                    curveY = quadrantSize - 1 - curveY;
                }

                int swap = curveX;
                curveX = curveY;
                curveY = swap;
            }
        }

        return index;
    }

    private long index(Geometry geometry) {
        Coordinate coordinate = geometry.getCoordinate();
        return coordinate == null ? 0 : index(coordinate.getX(), coordinate.getY());
    }

    /**
     * Places a position within the work area on the grid that the curve is walked on. Positions
     * outside of the work area are kept at its edge.
     */
    private static int toCurve(double position, double size) {
        if (size <= 0) {
            return 0;
        }

        return (int) Math.min(RESOLUTION - 1d, Math.max(0, position / size * (RESOLUTION - 1)));
    }
}
