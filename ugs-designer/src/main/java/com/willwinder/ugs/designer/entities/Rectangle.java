package com.willwinder.ugs.designer.entities;

import com.willwinder.ugs.designer.cut.CutType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Rectangle extends Entity {

    private final Rectangle2D.Double shape;

    public Rectangle(double x, double y) {
        super();
        this.shape = new Rectangle2D.Double(x, y, 10, 10);
    }

    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(4));
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


    public java.awt.Shape getBoundingBox() {
        java.awt.Rectangle bounds = shape.getBounds();
        bounds.grow(5, 5);
        return getGlobalTransform().createTransformedShape(bounds);
    }

    @Override
    public java.awt.Rectangle getBounds() {
        return getShape().getBounds();
    }

    @Override
    public void setSize(Point2D s) {
        if (s.getX() < 2) {
            s.setLocation(2, s.getY());
        }

        if (s.getY() < 2) {
            s.setLocation(s.getX(), 2);
        }
        shape.setFrame(shape.getX(), shape.getY(), s.getX(), s.getY());
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void setWidth(double width) {
        shape.setFrame(shape.getX(), shape.getY(), width, shape.getHeight());
    }

    public void setHeight(double height) {
        shape.setFrame(shape.getX(), shape.getY(), shape.getWidth(), height);
    }

}
