package com.willwinder.ugs.designer.entities;

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
    public java.awt.Shape getShape() {
        return new Rectangle();
    }

    @Override
    public java.awt.Shape getRawShape() {
        return new Rectangle();
    }
}
