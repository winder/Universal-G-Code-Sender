/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gcode;

import com.willwinder.ugs.nbp.designer.entities.Entity;

import java.util.Comparator;

/**
 * A comparator for ordering entities to minimize the rapid movement between
 * entities.
 *
 * This is done by creating grids of the work area and sorts the entities so
 * that it will process one grid at the time.Within each grid it will attempt
 * to sort the entities with the squared distance.
 *
 * Small tests show that this could shorten the travel distance up to 10 times
 * between many entities.
 *
 * @author Joacim Breiler
 */
public class EntityComparator implements Comparator<Entity> {

    public static final int NUMBER_OF_GRIDS = 10;
    private final double width;
    private final double height;

    public EntityComparator(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public int compare(Entity e1, Entity e2) {
        int order = 0;
        double squaredDistance = e1.getCenter().distanceSq(e2.getCenter());
        if (squaredDistance < 0) order -= 1;
        if (squaredDistance > 0) order += 1;

        double gridWidth = width / NUMBER_OF_GRIDS;
        double gridHeight = height / NUMBER_OF_GRIDS;

        long e1GridX = Math.round(e1.getCenter().getX() / gridWidth);
        long e1GridY = Math.round(e1.getCenter().getY() / gridHeight);

        long e2GridX = Math.round(e2.getCenter().getX() / gridWidth);
        long e2GridY = Math.round(e2.getCenter().getY() / gridHeight);

        if (e1GridX < e2GridX) order -= 10;
        if (e1GridX > e2GridX) order += 10;
        if (e1GridY < e2GridY) order -= 100;
        if (e1GridY > e2GridY) order += 100;

        return order;
    }
}