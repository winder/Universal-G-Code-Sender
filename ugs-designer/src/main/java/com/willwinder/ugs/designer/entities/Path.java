package com.willwinder.ugs.designer.entities;

import com.willwinder.ugs.designer.cut.CutType;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class Path extends Entity {

    private final Path2D.Double shape;

    public Path() {
        super();
        this.shape = new Path2D.Double();
    }

    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);

        if(getCutSettings().getCutType() == CutType.POCKET) {
            g.fill(getShape());
        }

        g.draw(getShape());
    }

    @Override
    public java.awt.Shape getShape() {
        return getGlobalTransform().createTransformedShape(shape);
    }
    @Override
    public java.awt.Shape getRawShape() {
        return shape;
    }

    @Override
    public void setSize(Point2D s) {
        if (s.getX() < 2) {
            s.setLocation(2, s.getY());
        }

        if (s.getY() < 2) {
            s.setLocation(s.getX(), 2);
        }
    }

    public String toString() {
        return "path;" + super.toString();
    }

    public void moveTo(double x, double y) {
        shape.moveTo(x, y);
    }

    public void lineTo(double x, double y) {
        shape.lineTo(x, y);
    }

    public void quadTo(double x1, double y1, double x2, double y2) {
        shape.quadTo(x1, y1, x2, y2);
    }

    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        shape.curveTo(x1, y1, x2, y2, x3, y3);
    }


}
