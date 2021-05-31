package com.willwinder.ugs.nbp.designer.entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEntity implements Entity {

    private Entity parent;
    private Set<EntityListener> listeners = new HashSet<>();

    private AffineTransform transform = new AffineTransform();

    public AbstractEntity() {
    }

    /**
     * Creates an entity with the relative position to the parent
     *
     * @param relativeX the relative position to the parent
     * @param relativeY the relative position to the parent
     */
    public AbstractEntity(double relativeX, double relativeY) {
        applyTransform(AffineTransform.getTranslateInstance(relativeX, relativeY));
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

    @Override
    public Dimension getSize() {
        Rectangle2D bounds = getShape().getBounds2D();
        return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
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
        return new Point2D.Double(getBounds().getX(), getBounds().getY());
    }

    @Override
    public void setPosition(Point2D position) {
        Point2D currentPosition = getPosition();
        transform.translate(position.getX() - currentPosition.getX(), position.getY() - currentPosition.getY());
    }

    @Override
    public Entity getParent() {
        return parent;
    }

    @Override
    public void setParent(Entity entity) {
        this.parent = entity;
    }

    @Override
    public Point2D getCenter() {
        Rectangle2D bounds = getBounds();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    @Override
    public void setCenter(Point2D center) {
        setPosition(new Point2D.Double(center.getX() - (getSize().width / 2d), center.getY() - (getSize().height / 2d)));
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
            throw new RuntimeException("Could not make inverse transform of point", e);
        }
    }

    @Override
    public double getRotation() {
        Point2D point1 = new Point2D.Double(0, 0);
        getTransform().transform(point1, point1);

        Point2D point2 = new Point2D.Double(10, 0);
        getTransform().transform(point2, point2);

        Point2D normalized = new Point2D.Double(point2.getX() - point1.getX(), point2.getY() - point1.getY());
        return Math.toDegrees(Math.atan2(normalized.getY(), normalized.getX()));
    }

    @Override
    public void rotate(double angle) {
        transform.preConcatenate(AffineTransform.getRotateInstance(Math.toRadians(angle), getRelativeShape().getBounds().getCenterX(), getRelativeShape().getBounds().getCenterY()));
        notifyEvent(new EntityEvent(this, EventType.ROTATED));
    }

    @Override
    public void scale(double sx, double sy) {
        transform.preConcatenate(AffineTransform.getScaleInstance(sx, sy));
    }


    @Override
    public void rotate(Point2D center, double angle) {
        transform.preConcatenate(AffineTransform.getRotateInstance(Math.toRadians(angle), center.getX(), center.getY()));
        notifyEvent(new EntityEvent(this, EventType.ROTATED));
    }

    @Override
    public void applyTransform(AffineTransform transform) {
        this.transform.preConcatenate(transform);
    }
}
