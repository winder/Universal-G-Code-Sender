package com.willwinder.ugs.nbp.designer.gui.entities;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Ellipse extends AbstractEntity {

    private final Ellipse2D.Double shape;

    public Ellipse(double relativeX, double relativeY) {
        super(relativeX, relativeY);
        this.shape = new Ellipse2D.Double(0, 0, 10, 10);
    }

    @Override
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
        this.shape.setFrame(0, 0, s.getWidth(), s.getHeight());
    }
}
