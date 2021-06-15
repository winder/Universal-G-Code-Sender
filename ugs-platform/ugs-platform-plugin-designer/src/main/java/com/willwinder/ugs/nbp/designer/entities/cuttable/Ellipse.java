package com.willwinder.ugs.nbp.designer.entities.cuttable;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Ellipse extends AbstractCuttable {

    private final Ellipse2D.Double shape;

    public Ellipse(double relativeX, double relativeY) {
        super(relativeX, relativeY);
        setName("Ellipse");
        this.shape = new Ellipse2D.Double(0, 0, 10, 10);
    }

    public Ellipse() {
        this.shape = new Ellipse2D.Double(0, 0, 10, 10);
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public void setSize(Dimension s) {
        this.shape.setFrame(0, 0, s.getWidth(), s.getHeight());
    }


    @Override
    public String toString() {
        return "Ellipse (" + getCutType().name() + ")";
    }
}
