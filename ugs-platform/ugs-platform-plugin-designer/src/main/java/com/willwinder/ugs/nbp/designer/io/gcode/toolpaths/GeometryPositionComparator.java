/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.Comparator;

/**
 * A comparator for ordering entities to minimize the rapid movement between
 * entities.
 * <p>
 * This is done by creating grids of the work area and sorts the entities so
 * that it will process one grid at the time.Within each grid it will attempt
 * to sort the entities with the squared distance.
 * <p>
 * Small tests show that this could shorten the travel distance up to 10 times
 * between many entities.
 *
 * @author Joacim Breiler
 */
public class GeometryPositionComparator implements Comparator<Geometry> {

    public static final int NUMBER_OF_GRIDS = 10;
    private final Envelope envelope;

    public GeometryPositionComparator(Envelope envelope) {
        this.envelope = envelope;
    }

    /**
     * Returns the first coordinate in the geometry
     *
     * @param geometry the geometry to get the coordinate from
     * @return the first coordinate
     */
    private static Coordinate getFirstCoordinate(Geometry geometry) {
        return geometry.getCoordinate();
    }

    /**
     * Returns the last coordinate in the geometry
     *
     * @param geometry the geometry to get the coordinate from
     * @return the last coordinate
     */
    private static Coordinate getLastCoordinate(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        return coordinates[coordinates.length - 1];
    }

    @Override
    public int compare(Geometry o1, Geometry o2) {

        int order = 0;
        try {
            Coordinate lastCoordinate = getLastCoordinate(o1);
            Coordinate nextCoordinate = getFirstCoordinate(o2);

            double gridWidth = envelope.getWidth() / NUMBER_OF_GRIDS;
            double gridHeight = envelope.getHeight() / NUMBER_OF_GRIDS;

            long e1GridX = Math.round((lastCoordinate.getX() - envelope.getMinX()) / gridWidth);
            long e1GridY = Math.round((lastCoordinate.getY() - envelope.getMinY()) / gridHeight);

            long e2GridX = Math.round((nextCoordinate.getX() - envelope.getMinX()) / gridWidth);
            long e2GridY = Math.round((nextCoordinate.getY() - envelope.getMinY()) / gridHeight);

            if (e1GridX < e2GridX) order -= 1;
            if (e1GridX > e2GridX) order += 1;
            if (e1GridY < e2GridY) order -= 10;
            if (e1GridY > e2GridY) order += 10;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return order;
    }
}
