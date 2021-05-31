package com.willwinder.ugs.nbp.designer.entities.cuttable;

import java.awt.*;
import java.awt.geom.Path2D;

public class Path extends AbstractCuttable {

    private final Path2D.Double shape;

    public Path() {
        super();
        this.shape = new Path2D.Double();
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

    @Override
    public String toString() {
        return "Path (" + getCutType().name() + ")";
    }
}
