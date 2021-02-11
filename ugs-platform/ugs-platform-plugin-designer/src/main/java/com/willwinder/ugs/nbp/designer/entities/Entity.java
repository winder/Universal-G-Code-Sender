package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.logic.events.EntityListener;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public interface Entity {
    void render(Graphics2D g);

    /**
     * Returns the shape model in relative space without any applied affine transformations
     *
     * @return a shape using relative coordinates
     */
    Shape getRelativeShape();

    /**
     * Returns a shape that has been globally transformed from relative coordinates to the real coordinate applying all
     * affine transformations.
     *
     * @return a transformed shape
     */
    Shape getShape();

    void addListener(EntityListener entityListener);

    void removeListener(EntityListener entityListener);

    /**
     * Returns if the given point is within the given entity
     *
     * @param point the point to check
     * @return true if the point is within the entity
     */
    boolean isWithin(Point2D point);

    Dimension getSize();

    void setSize(Dimension s);

    Rectangle getBounds();

    /**
     * Returns the real position of the entity
     *
     * @return the real position
     */
    Point2D getPosition();

    Entity getParent();

    void setParent(Entity shape);

    Point getCenter();

    void destroy();

    AffineTransform getTransform();

    AffineTransform getGlobalTransform();

    void move(Point2D deltaMovement);

    double getRotation();

    void setRotation(double angle);
}
