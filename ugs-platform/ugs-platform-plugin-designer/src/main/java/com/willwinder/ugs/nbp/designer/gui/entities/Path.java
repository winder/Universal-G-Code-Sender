package com.willwinder.ugs.nbp.designer.gui.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

public class Path extends AbstractEntity {

    private final Path2D.Double shape;

    public Path() {
        super();
        this.shape = new Path2D.Double();
    }

    public void render(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(Color.BLACK);
        graphics.draw(getShape());
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public void setSize(Dimension s) {
        if (s.getWidth() < 2) {
            s.setSize(2, s.getWidth());
        }

        if (s.getHeight() < 2) {
            s.setSize(s.getHeight(), 2);
        }
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
