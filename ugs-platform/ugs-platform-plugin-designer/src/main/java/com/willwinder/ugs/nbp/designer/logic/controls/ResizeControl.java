package com.willwinder.ugs.nbp.designer.logic.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.events.MouseShapeEvent;
import com.willwinder.ugs.nbp.designer.logic.events.ShapeEvent;
import com.willwinder.ugs.nbp.designer.logic.events.ShapeEventType;

import java.awt.*;
import java.awt.geom.Point2D;

public class ResizeControl extends Control {
    public static final int SIZE = 10;
    private final Rectangle shape;
    private final Location location;
    private Point2D mousePosition = new Point2D.Double();

    public ResizeControl(Entity parent, Location location) {
        super(parent);
        this.location = location;
        shape = new Rectangle(0, 0, SIZE, SIZE);

        updatePosition();
    }

    private void updatePosition() {
        Rectangle bounds = shape.getBounds();
        double centerX = (getParent().getSize().x / 2.0) - (bounds.getWidth() / 2);
        double centerY = (getParent().getSize().y / 2.0) - (bounds.getHeight() / 2);

        Point point = new Point();
        if (location == Location.LEFT) {
            point.setLocation(-bounds.getWidth(), centerY);
        } else if (location == Location.RIGHT) {
            point.setLocation(getParent().getSize().x, centerY);
        } else if (location == Location.TOP) {
            point.setLocation(centerX, -bounds.getHeight());
        } else if (location == Location.BOTTOM) {
            point.setLocation(centerX, getParent().getSize().y);
        }

        //setPosition(point);
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public void setSize(Point2D s) {

    }

    @Override
    public void drawShape(Graphics2D g) {
        /*g.setStroke(new BasicStroke(1f));
        g.setColor(Colors.LINE);


        int halfWidth = getParent().getSize().x / 2;
        int halfHeight = getParent().getSize().y / 2;

        if (location == Location.TOP) {
            g.drawLine(-halfWidth + (SIZE / 2), SIZE, halfWidth + (SIZE / 2), SIZE);
        } else if (location == Location.BOTTOM) {
            g.drawLine(-halfWidth + (SIZE / 2), 0, halfWidth + (SIZE / 2), 0);
        } else if (location == Location.LEFT) {
            g.drawLine(SIZE, -halfHeight + (SIZE / 2), SIZE, halfHeight + (SIZE / 2));
        } else if (location == Location.RIGHT) {
            g.drawLine(0, -halfHeight + (SIZE / 2), 0, halfHeight + (SIZE / 2));
        }*/

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.GRAY);
        g.fill(shape);

        Point2D c = new Point2D.Double(getParent().getPosition().getX() + getParent().getCenter().x, getParent().getPosition().getY() + getParent().getCenter().y);
        //double angle = Math.round(calcRotationAngleInDegrees(c, mouseShapeEvent.getCurrentMousePosition()));
        //getParent().setRotation(angle);


        try {
            Point2D point = toRealPoint(mousePosition);
            g.setColor(Color.RED);
            g.drawLine(0, 0, (int)point.getX(), (int)point.getY());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onShapeEvent(ShapeEvent shapeEvent) {
        if (shapeEvent.getType() == ShapeEventType.RESIZED) {
            updatePosition();
        } else if (shapeEvent instanceof MouseShapeEvent && shapeEvent.getShape() == this) {
            MouseShapeEvent mouseShapeEvent = (MouseShapeEvent) shapeEvent;
            Point2D c = new Point2D.Double(getParent().getPosition().getX() + getParent().getCenter().getX(), getParent().getPosition().getY() + getParent().getCenter().getY());
            //double angle = calcRotationAngleInDegrees(c, mouseShapeEvent.getCurrentMousePosition());
            //System.out.println(c + " " + mouseShapeEvent.getCurrentMousePosition() + " " + location);

            mousePosition = mouseShapeEvent.getCurrentMousePosition();
            Point2D point = toRealPoint(mousePosition);

            if (location == Location.LEFT) {

                /*Point s = getParent().getSize();
                s.x =  (s.x - point.x);
                getParent().setSize(s);*/



                Point2D p = getParent().getPosition();
                p.setLocation(p.getX() + point.getX(), p.getY());
                //p.y = p.y + point.y;

                System.out.println(point + " " + p);
                //getParent().setPosition(p);
            }

            if (location == Location.RIGHT) {
                Point s = getParent().getSize();
                s.setLocation(s.getX() + point.getX(), s.getY());
                getParent().setSize(s);
            }

            if (location == Location.BOTTOM) {
                Point s = getParent().getSize();
                s.setLocation(s.getX(), s.getY() + point.getY());
                getParent().setSize(s);
            }

            getParent().notifyShapeEvent(new ShapeEvent(getParent(), ShapeEventType.RESIZED));
        }
    }
}
