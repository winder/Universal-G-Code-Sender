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

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractEntity implements Entity {

    private final Set<EntityListener> listeners = Sets.newConcurrentHashSet();

    private AffineTransform transform = new AffineTransform();
    private String name = "AbstractEntity";

    protected AbstractEntity() {
        this(0, 0);
    }

    /**
     * Creates an entity with the relative position to the parent
     *
     * @param x the position in real space
     * @param y the position in real space
     */
    protected AbstractEntity(double x, double y) {
        applyTransform(AffineTransform.getTranslateInstance(x, y));
    }

    public void notifyEvent(EntityEvent entityEvent) {
        listeners.forEach(entityListener -> entityListener.onEvent(entityEvent));
    }

    @Override
    public void addListener(EntityListener entityListener) {
        this.listeners.add(entityListener);
    }

    @Override
    public void removeListener(EntityListener entityListener) {
        this.listeners.remove(entityListener);
    }

    @Override
    public boolean isWithin(Point2D point) {
        return getShape().contains(point);
    }

    public boolean isIntersecting(Shape shape) {
        return shape.intersects(getShape().getBounds2D());
    }

    @Override
    public Size getSize() {
        Rectangle2D bounds = getShape().getBounds2D();
        return new Size(bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(Size size) {
        if (size.getWidth() < 0.1) {
            size = new Size(0.1, size.getHeight());
        }

        if (size.getHeight() < 0.1) {
            size = new Size(size.getWidth(), 0.1);
        }

        Size currentSize = getSize();
        scale(size.getWidth() / currentSize.getWidth(), size.getHeight() / currentSize.getHeight());
    }

    @Override
    public Rectangle2D getBounds() {
        return getShape().getBounds2D();
    }

    @Override
    public Shape getShape() {
        return getTransform().createTransformedShape(getRelativeShape());
    }

    @Override
    public Point2D getPosition() {
        Rectangle2D bounds = getBounds();
        return new Point2D.Double(bounds.getX(), bounds.getY());
    }

    @Override
    public void setPosition(Point2D position) {
        Point2D currentPosition = getPosition();
        move(new Point2D.Double(position.getX() - currentPosition.getX(), position.getY() - currentPosition.getY()));
    }

    @Override
    public Point2D getCenter() {
        Rectangle2D bounds = getBounds();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    @Override
    public void setCenter(Point2D center) {
        Size size = getSize();
        setPosition(new Point2D.Double(center.getX() - (size.getWidth() / 2d), center.getY() - (size.getHeight() / 2d)));
    }


    @Override
    public void destroy() {
        listeners.clear();
    }

    @Override
    public AffineTransform getTransform() {
        return this.transform;
    }

    @Override
    public void setTransform(AffineTransform transform) {
        // Prevent null transforms
        if (transform == null) {
            transform = new AffineTransform();
        }

        this.transform = transform;
    }

    @Override
    public void move(Point2D deltaMovement) {
        try {
            transform.preConcatenate(AffineTransform.getTranslateInstance(deltaMovement.getX(), deltaMovement.getY()));
            notifyEvent(new EntityEvent(this, EventType.MOVED));
        } catch (Exception e) {
            throw new EntityException("Could not make inverse transform of point", e);
        }
    }

    @Override
    public double getRotation() {
        Point2D point1 = new Point2D.Double(0, 0);
        getTransform().transform(point1, point1);

        Point2D point2 = new Point2D.Double(10, 0);
        getTransform().transform(point2, point2);

        Point2D normalized = new Point2D.Double(point2.getX() - point1.getX(), point2.getY() - point1.getY());
        return Utils.normalizeRotation(-Math.toDegrees(Math.atan2(normalized.getY(), normalized.getX())));
    }

    @Override
    public void rotate(double angle) {
        rotate(getCenter(), angle);
    }

    @Override
    public void scale(double sx, double sy) {
        Point2D originalPosition = getPosition();
        transform.preConcatenate(AffineTransform.getScaleInstance(sx, sy));

        // Restore position
        Point2D currentPosition = getPosition();
        transform.preConcatenate(AffineTransform.getTranslateInstance(originalPosition.getX() - currentPosition.getX(), originalPosition.getY() - currentPosition.getY()));
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }

    @Override
    public void setRotation(double rotation) {
        double deltaRotation = rotation - getRotation();
        if (deltaRotation != 0) {
            rotate(deltaRotation);
        }
    }

    @Override
    public void rotate(Point2D center, double angle) {
        transform.preConcatenate(AffineTransform.getRotateInstance(-Math.toRadians(angle), center.getX(), center.getY()));
        notifyEvent(new EntityEvent(this, EventType.ROTATED));
    }

    @Override
    public void applyTransform(AffineTransform transform) {
        this.transform.preConcatenate(transform);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return getName();
    }

    public void setWidth(double width) {
        Size size = getSize();
        setSize(new Size(width, size.getHeight()));
    }

    public void setHeight(double height) {
        Size size = getSize();
        setSize(new Size(size.getWidth(), height));
    }
}
