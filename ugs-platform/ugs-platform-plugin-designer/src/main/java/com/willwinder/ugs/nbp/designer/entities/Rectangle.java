package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.cut.CutType;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEventType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Rectangle extends Entity {

    private final Rectangle2D.Double shape;

    public Rectangle(double x, double y) {
        super(x, y);
        this.shape = new Rectangle2D.Double(0, 0, 10, 10);
    }

    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);

        Shape transformedShape = getGlobalTransform().createTransformedShape(shape);

        if (getCutSettings().getCutType() == CutType.POCKET) {
            g.fill(transformedShape);
        }

        g.draw(transformedShape);
    }

    @Override
    public Shape getShape() {
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
        shape.setFrame(0, 0, s.getX(), s.getY());
        notifyEvent(new EntityEvent(this, EntityEventType.RESIZED));
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void setWidth(double width) {
        shape.setFrame(0, 0, width, shape.getHeight());
        notifyEvent(new EntityEvent(this, EntityEventType.RESIZED));
    }

    public void setHeight(double height) {
        shape.setFrame(0, 0, shape.getWidth(), height);
        notifyEvent(new EntityEvent(this, EntityEventType.RESIZED));
    }

}
