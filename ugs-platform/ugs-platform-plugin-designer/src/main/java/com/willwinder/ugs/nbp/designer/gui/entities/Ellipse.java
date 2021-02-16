package com.willwinder.ugs.nbp.designer.gui.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Ellipse extends AbstractEntity {

    private final Ellipse2D shape;

    public Ellipse(double x, double y) {
        super(x, y);
        this.shape = new Ellipse2D.Double(0, 0, 1, 1);
    }

    public void render(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        g.draw(getShape());
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    public String toString() {
        return "circ;" + super.toString();
    }

    @Override
    public void setSize(Dimension s) {
        this.shape.setFrame(0, 0, s.getWidth(), s.getHeight());
    }
}
