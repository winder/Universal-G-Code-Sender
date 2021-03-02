package com.willwinder.ugs.nbp.designer.gui.entities;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntity implements Entity {

    private Entity parent;
    private List<EntityListener> listeners = new ArrayList<>();

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
        Shape bounds = getGlobalTransform().createTransformedShape(getRelativeShape().getBounds());
        return bounds.contains(point);
    }

    @Override
    public Dimension getSize() {
        Rectangle bounds = getRelativeShape().getBounds();
        return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
    }

    @Override
    public Rectangle getBounds() {
        return getShape().getBounds();
    }

    @Override
    public Shape getShape() {
        return getGlobalTransform().createTransformedShape(getRelativeShape());
    }

    @Override
    public Point2D getPosition() {
        return getBounds().getLocation();
    }

    /**
     * Sets the relative position to the parent
     *
     * @param x the relative position to the parent
     * @param y the relative position to the parent
     */
    public void setRelativePosition(double x, double y) {
        //positionTransform = new AffineTransform();
        //positionTransform.translate(x, y);
        transform.translate(x, y);
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
        Rectangle bounds = getBounds();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    @Override
    public void destroy() {
        listeners.clear();
    }

    @Override
    public AffineTransform getRelativeTransform() {
        return this.transform;
    }

    @Override
    public void setRelativeTransform(AffineTransform transform) {
        // Prevent null transforms
        if (transform == null) {
            transform = new AffineTransform();
        }

        this.transform = transform;
    }

    /**
     * Returns the concatenated transform of this node. That is, this
     * node's transform preconcatenated with it's parent's transforms.
     */
    @Override
    public AffineTransform getGlobalTransform() {
        AffineTransform ctm = new AffineTransform();
        Entity node = this;
        while (node != null) {
            ctm.preConcatenate(node.getRelativeTransform());
            node = node.getParent();
        }
        return ctm;
    }

    @Override
    public void move(Point2D deltaMovement) {
        transform.preConcatenate(AffineTransform.getTranslateInstance(deltaMovement.getX(), deltaMovement.getY()));
        notifyEvent(new EntityEvent(this, EventType.MOVED));
    }

    @Override
    public double getRotation() {
        Point2D point1 = new Point2D.Double(0, 0);
        getGlobalTransform().transform(point1, point1);

        Point2D point2 = new Point2D.Double(10, 0);
        getGlobalTransform().transform(point2, point2);

        Point2D normalized = new Point2D.Double(point2.getX() - point1.getX(), point2.getY() - point1.getY());
        return Math.toDegrees(Math.atan2(normalized.getY(), normalized.getX()));
    }

    @Override
    public void rotate(double angle) {
        try {
            transform.rotate(Math.toRadians(angle), getRelativeShape().getBounds().getCenterX(), getRelativeShape().getBounds().getCenterY());
            notifyEvent(new EntityEvent(this, EventType.ROTATED));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't set the rotation", e);
        }
    }

    @Override
    public void rotate(Point2D center, double angle) {
        try {
            Point2D relativePoint = new Point2D.Double(0, 0);
            getGlobalTransform().inverseTransform(center, relativePoint);

            try {
                transform.concatenate(AffineTransform.getRotateInstance(Math.toRadians(angle), relativePoint.getX() + getRelativeShape().getBounds().getCenterX(), relativePoint.getY() + getRelativeShape().getBounds().getCenterY()));
                notifyEvent(new EntityEvent(this, EventType.ROTATED));
            } catch (Exception e) {
                throw new RuntimeException("Couldn't set the rotation", e);
            }
        } catch (NoninvertibleTransformException e) {
        }
    }

    public void applyTransform(AffineTransform transform) {
        this.transform.concatenate(transform);
        notifyEvent(new EntityEvent(this, EventType.MOVED));
    }

}
