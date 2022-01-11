/*
    Copyright 2021 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * An entity is something that can be drawn in a {@link Drawing} which has a position, rotation and size.
 *
 * @author Joacim Breiler
 */
public interface Entity {

    /**
     * Renders the entity using the given graphics context
     *
     * @param graphics the graphics context to render the entity into
     * @param drawing  the current drawing where the entity is displayed
     */
    void render(Graphics2D graphics, Drawing drawing);

    /**
     * Returns the entity shape in relative space without any applied affine transformations
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

    /**
     * Adds an entity listener to be notified on entity events
     *
     * @param entityListener a listener that will receive events
     */
    void addListener(EntityListener entityListener);

    /**
     * Removes an entity listener that no longer will notified on entity events
     *
     * @param entityListener the listener to remove
     */
    void removeListener(EntityListener entityListener);

    /**
     * Returns if the given point is within the given entity
     *
     * @param point the point to check
     * @return true if the point is within the entity
     */
    boolean isWithin(Point2D point);

    /**
     * Returns the size of the entity in real space using its bounds
     *
     * @return the size of the entity
     */
    Size getSize();

    /**
     * Sets the size of the entity in real space forming its new bounds
     *
     * @param size the new size
     */
    void setSize(Size size);

    /**
     * Gets the bounds of the entity with the position and size in real space
     *
     * @return the bounds in real space
     */
    Rectangle2D getBounds();

    /**
     * Returns the real position of the entity
     *
     * @return the real position
     */
    Point2D getPosition();

    /**
     * Sets the real position of the entity
     *
     * @param position the new position
     */
    void setPosition(Point2D position);

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
     * Applies a new transformation to this entity
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
     * Returns the object rotation in degrees
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
     * Sets the rotation of the object in degrees
     *
     * @param rotation the rotation in degrees
     */
    void setRotation(double rotation);

    /**
     * Gets the center point of this object using its real bounds
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

    /**
     * Returns the name of the entity which can be used by the user to describe it in a list of entities.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the name of the entity
     *
     * @param name the name
     */
    void setName(String name);

    /**
     * Returns true if the object intersects with the given shape
     *
     * @param shape the shape to check against
     * @return true if it is intersecting
     */
    boolean isIntersecting(Shape shape);

    /**
     * Makes a copy of this entity
     *
     * @return a copy of the entity
     */
    Entity copy();
}
