package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.cut.CutType;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class Ellipse extends Entity {

    private final Ellipse2D shape;

    public Ellipse(double x, double y) {
        super(x, y);
        this.shape = new Ellipse2D.Double(0, 0, 1, 1);
    }

    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        Shape transformedShape = getGlobalTransform().createTransformedShape(shape);
        if(getCutSettings().getCutType() == CutType.POCKET) {
            g.fill(transformedShape);
        }
        g.draw(transformedShape);
    }


    @Override
    public Shape getShape() {
        return shape;
    }

    public String toString() {
        return "circ;" + super.toString();
    }

    @Override
    public void setSize(Point2D s) {
        this.shape.setFrame(0, 0, s.getX(), s.getY());
    }
}
