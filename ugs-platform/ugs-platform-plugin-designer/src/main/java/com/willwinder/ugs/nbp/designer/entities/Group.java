package com.willwinder.ugs.nbp.designer.entities;

import java.awt.Rectangle;
import java.awt.*;
import java.awt.geom.Point2D;

public class Group extends Entity {

    @Override
    public void drawShape(Graphics2D g) {

    }

    @Override
    public void setSize(Point2D s) {

    }

    @Override
    public Shape getShape() {
        return new Rectangle();
    }

    @Override
    public Shape getRawShape() {
        return new Rectangle();
    }
}
