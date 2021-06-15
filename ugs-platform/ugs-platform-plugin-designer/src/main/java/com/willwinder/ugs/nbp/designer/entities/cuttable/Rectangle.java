package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Rectangle extends AbstractCuttable {

    private final Rectangle2D.Double shape;

    public Rectangle() {
        this(0,0);
    }

    /**
     * Creates a rectangle with the relative position to the parent
     *
     * @param relativeX the relative position to the parent
     * @param relativeY the relative position to the parent
     */
    public Rectangle(double relativeX, double relativeY) {
        super(relativeX, relativeY);
        this.shape = new Rectangle2D.Double(0, 0, 10, 10);
        setName("Rectangle");
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

    @Override
    public String toString() {
        return "Rectangle (" + getCutType().name() + ")";
    }

}
