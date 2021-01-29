package com.willwinder.ugs.nbp.designer.entities;

import java.awt.Rectangle;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class Group extends Entity {

    @Override
    public void drawShape(Graphics2D g) {
        AffineTransform transform = g.getTransform();
        //getShapes().forEach(node -> node.draw(g));

        g.setTransform(transform);
    }

    @Override
    public void setSize(Point2D s) {

    }

    @Override
    public Shape getShape() {
        return new Rectangle();
    }
}
