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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;

public class HilbertPositionComparatorTest {

    @Test
    public void compare_shouldOrderTheCornersOfTheWorkAreaAlongTheCurve() {
        List<Geometry> geometries = new ArrayList<>(List.of(
                point(10, 10), point(0, 10), point(10, 0), point(0, 0)));

        geometries.sort(new HilbertPositionComparator(new Envelope(0, 10, 0, 10)));

        // The curve sets off in the lower left corner and ends up in the lower right one, without
        // ever crossing the work area
        assertPosition(geometries.get(0), 0, 0);
        assertPosition(geometries.get(1), 0, 10);
        assertPosition(geometries.get(2), 10, 10);
        assertPosition(geometries.get(3), 10, 0);
    }

    @Test
    public void compare_shouldKeepPositionsCloseToEachOtherNextToEachOtherInTheOrder() {
        List<Geometry> geometries = new ArrayList<>(List.of(
                point(101, 100), point(2, 3), point(103, 104), point(0, 0), point(100, 102), point(1, 1)));

        geometries.sort(new HilbertPositionComparator(new Envelope(0, 110, 0, 110)));

        // Both of the clusters are worked through before the tool moves over to the other one
        assertTrue(geometries.subList(0, 3).stream().allMatch(geometry -> geometry.getCoordinate().getX() < 50));
        assertTrue(geometries.subList(3, 6).stream().allMatch(geometry -> geometry.getCoordinate().getX() > 50));
    }

    @Test
    public void index_shouldPlacePositionsOutsideOfTheWorkAreaAtItsEdge() {
        HilbertPositionComparator comparator = new HilbertPositionComparator(new Envelope(0, 10, 0, 10));

        long index = comparator.index(-5, -5);

        assertEquals(comparator.index(0, 0), index);
    }

    @Test
    public void index_shouldHandleAWorkAreaWithoutASize() {
        HilbertPositionComparator comparator = new HilbertPositionComparator(new Envelope(5, 5, 5, 5));

        long index = comparator.index(5, 5);

        assertEquals(0, index);
    }

    @Test
    public void shouldSortEntitiesForOptimizedDistanceToLargeModels() {
        List<Geometry> entities = generateEntities(100);

        double totalDistance = 0;
        for (int i = 1; i < entities.size(); i++) {
            totalDistance += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }

        double totalDistanceSorted = 0;
        entities.sort(new HilbertPositionComparator(new Envelope(0d, 100d, 0d, 100d)));
        for (int i = 1; i < entities.size(); i++) {
            totalDistanceSorted += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }
        assertTrue("Expected optimization of the total distance " + totalDistance + " to be optimized 15 times shorter but was " + totalDistanceSorted, totalDistance / 15 > totalDistanceSorted);
    }

    @Test
    public void shouldSortEntitiesForOptimizedDistanceToSmallModels() {
        List<Geometry> entities = generateEntities(1);

        double totalDistance = 0;
        for (int i = 1; i < entities.size(); i++) {
            totalDistance += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }

        double totalDistanceSorted = 0;
        entities.sort(new HilbertPositionComparator(new Envelope(0d, 1d, 0d, 1d)));
        for (int i = 1; i < entities.size(); i++) {
            totalDistanceSorted += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }
        assertTrue("Expected optimization of the total distance " + totalDistance + " to be optimized 15 times shorter but was " + totalDistanceSorted, totalDistance / 15 > totalDistanceSorted);
    }

    private List<Geometry> generateEntities(int widthAndHeight) {
        List<Geometry> entities = new ArrayList<>();
        double count = 0;

        while (count++ < 1000) {
            entities.add(ToolPathUtils.GEOMETRY_FACTORY.createPoint(new Coordinate(RandomUtils.nextDouble(0, widthAndHeight), RandomUtils.nextDouble(0, widthAndHeight), 0d)));
        }
        return entities;
    }

    private static void assertPosition(Geometry geometry, double x, double y) {
        assertEquals(x, geometry.getCoordinate().getX(), 0.001);
        assertEquals(y, geometry.getCoordinate().getY(), 0.001);
    }

    private static Geometry point(double x, double y) {
        return ToolPathUtils.GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
    }
}
