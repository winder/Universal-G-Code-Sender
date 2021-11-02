package com.willwinder.ugs.nbp.designer.gcode;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class EntityComparatorTest {

    @Test
    public void shouldSortEntitiesForOptimizedDistanceToLargeModels() {
        List<Cuttable> entities = generateEntities(100);

        double totalDistance = 0;
        for (int i = 1; i < entities.size(); i++) {
            totalDistance += entities.get(i - 1).getCenter().distance(entities.get(i).getCenter());
        }

        double totalDistanceSorted = 0;
        entities.sort(new EntityComparator(100, 100));
        for (int i = 1; i < entities.size(); i++) {
            totalDistanceSorted += entities.get(i - 1).getCenter().distance(entities.get(i).getCenter());
        }
        assertTrue("Expected optimization of the total distance " + totalDistance + " to be optimized 5 times shorter but was " + totalDistanceSorted, totalDistance / 5 > totalDistanceSorted);
    }

    @Test
    public void shouldSortEntitiesForOptimizedDistanceToSmallModels() {
        List<Cuttable> entities = generateEntities(1);

        double totalDistance = 0;
        for (int i = 1; i < entities.size(); i++) {
            totalDistance += entities.get(i - 1).getCenter().distance(entities.get(i).getCenter());
        }

        double totalDistanceSorted = 0;
        entities.sort(new EntityComparator(1, 1));
        for (int i = 1; i < entities.size(); i++) {
            totalDistanceSorted += entities.get(i - 1).getCenter().distance(entities.get(i).getCenter());
        }
        assertTrue("Expected optimization of the total distance " + totalDistance + " to be optimized 5 times shorter but was " + totalDistanceSorted, totalDistance / 5 > totalDistanceSorted);
    }

    private List<Cuttable> generateEntities(int widthAndHeight) {
        List<Cuttable> entities = new ArrayList<>();
        double count = 0;
        while (count++ < 1000) {
            Ellipse ellipse = new Ellipse(RandomUtils.nextDouble(0, widthAndHeight), RandomUtils.nextDouble(0, widthAndHeight));
            ellipse.setSize(new Size(0.1, 0.1));
            entities.add(ellipse);
        }
        return entities;
    }
}