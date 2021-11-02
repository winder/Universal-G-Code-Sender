package com.willwinder.ugs.nbp.designer.gcode;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;

import java.awt.geom.Point2D;
import java.util.Comparator;

// compare points according to their polar radius
public class PathOrder implements Comparator<Entity> {

    public PathOrder() {

    }

    public int compare(Entity e1, Entity e2) {
        Point2D p = e1.getPosition();
        Point2D q = e2.getPosition();
        int order = 0;

        if (p.getX() < q.getX()) order -= 2;
        if (p.getX() > q.getX()) order += 2;
        if (p.getY() < q.getY() ) order -= 3;
        if (p.getY() > q.getY()) order += 3;

        return order;
    }
}