package com.willwinder.ugs.nbp.designer.entities;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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

    Rectangle2D getBounds();

    /**
     * Returns the real position of the entity
     *
     * @return the real position
     */
    Point2D getPosition();

    void setPosition(Point2D position);

    Entity getParent();

    void setParent(Entity shape);

    /**
     * Destroys this entity freeing up any allocated resources
     */
    void destroy();

    /**
     * Returns the current transformation
     *
     * @return a transformation for this entity
     */
    AffineTransform getTransform();

    /**
     * Sets a new transform overwriting any previous transformation
     *
     * @param transform a new transform
     */
    void setTransform(AffineTransform transform);

    /**
     * Applies a new generic transformation to this entity
     *
     * @param transform the new transformation to add to this
     */
    void applyTransform(AffineTransform transform);

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

    /**
     * Gets the center point of this object
     *
     * @return the center point
     */
    Point2D getCenter();

    /**
     * Moves the object placing its center to the given point
     *
     * @param center the new center
     */
    void setCenter(Point2D center);

    /**
     * Scales the entity using the given scale factors
     *
     * @param sx the x scale factor
     * @param sy the y scale factor
     */
    void scale(double sx, double sy);
}
