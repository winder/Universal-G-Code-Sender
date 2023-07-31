package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class GeometryPositionComparatorTest {

    @Test
    public void shouldSortEntitiesForOptimizedDistanceToLargeModels() {
        List<Geometry> entities = generateEntities(100);

        double totalDistance = 0;
        for (int i = 1; i < entities.size(); i++) {
            totalDistance += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }

        double totalDistanceSorted = 0;
        entities.sort(new GeometryPositionComparator(new Envelope(0d, 100d, 0d, 100d)));
        for (int i = 1; i < entities.size(); i++) {
            totalDistanceSorted += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }
        assertTrue("Expected optimization of the total distance " + totalDistance + " to be optimized 5 times shorter but was " + totalDistanceSorted, totalDistance / 5 > totalDistanceSorted);
    }

    @Test
    public void shouldSortEntitiesForOptimizedDistanceToSmallModels() {
        List<Geometry> entities = generateEntities(1);

        double totalDistance = 0;
        for (int i = 1; i < entities.size(); i++) {
            totalDistance += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }

        double totalDistanceSorted = 0;
        entities.sort(new GeometryPositionComparator(new Envelope(0d, 1d, 0d, 1d)));
        for (int i = 1; i < entities.size(); i++) {
            totalDistanceSorted += entities.get(i - 1).getCentroid().distance(entities.get(i).getCentroid());
        }
        assertTrue("Expected optimization of the total distance " + totalDistance + " to be optimized 5 times shorter but was " + totalDistanceSorted, totalDistance / 5 > totalDistanceSorted);
    }

    private List<Geometry> generateEntities(int widthAndHeight) {
        List<Geometry> entities = new ArrayList<>();
        double count = 0;

        while (count++ < 1000) {
            entities.add(ToolPathUtils.GEOMETRY_FACTORY.createPoint(new Coordinate(RandomUtils.nextDouble(0, widthAndHeight), RandomUtils.nextDouble(0, widthAndHeight), 0d)));
        }
        return entities;
    }
}
