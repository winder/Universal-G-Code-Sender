package com.willwinder.ugs.nbp.designer.gui.entities;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public interface Entity {

    /**
     * Renders the entity using the given graphics context
     *
     * @param graphics the graphics context to render the entity into
     */
    void render(Graphics2D graphics);

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

    Point2D getCenter();

    void destroy();

    /**
     * Returns the current relative transformation
     *
     * @return a relative transformation for this entity
     */
    AffineTransform getRelativeTransform();

    /**
     * Sets a new relative transform overwriting any previous transformation
     *
     * @param transform a new transform
     */
    void setRelativeTransform(AffineTransform transform);

    /**
     * Returns the current transformation for transforming the entities shape to real coordinatesﬁ
     *
     * @return a global transform applying all parents transformations
     */
    AffineTransform getGlobalTransform();

    /**
     * Moves the entity in real space using a delta movement
     *
     * @param deltaMovement a delta movement to move the entity
     */
    void move(Point2D deltaMovement);

    /**
     * Returns the objects rotation in degrees
     *
     * @return the angle in degrees
     */
    double getRotation();

    /**
     * Rotate the object around its center point in the given degrees
     *
     * @param angle the angle in degrees
     */
    void rotate(double angle);

    /**
     * Rotates the object
     *
     * @param center the center point in real space to rotate around
     * @param angle  the angle in degrees to rotate
     */
    void rotate(Point2D center, double angle);
}
