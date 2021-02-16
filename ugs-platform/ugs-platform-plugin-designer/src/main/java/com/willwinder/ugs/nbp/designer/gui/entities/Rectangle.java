package com.willwinder.ugs.nbp.designer.gui.entities;

import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EventType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class Rectangle extends AbstractEntity {

    private final Rectangle2D.Double shape;

    public Rectangle() {
        this.shape = new Rectangle2D.Double(0, 0, 10, 10);
    }

    public Rectangle(double x, double y) {
        super(x, y);
        this.shape = new Rectangle2D.Double(0, 0, 10, 10);
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

    @Override
    public void setSize(Dimension s) {
        if (s.getWidth() < 2) {
            s.setSize(2, s.getHeight());
        }

        if (s.getHeight() < 2) {
            s.setSize(s.getWidth(), 2);
        }
        shape.setFrame(0, 0, s.getWidth(), s.getHeight());
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }
    
    public void setWidth(double width) {
        shape.setFrame(0, 0, width, shape.getHeight());
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }

    public void setHeight(double height) {
        shape.setFrame(0, 0, shape.getWidth(), height);
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }

}
