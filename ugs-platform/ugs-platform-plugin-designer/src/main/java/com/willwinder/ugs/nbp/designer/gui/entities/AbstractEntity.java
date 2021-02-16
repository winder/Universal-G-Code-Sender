package com.willwinder.ugs.nbp.designer.gui.entities;

import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EntityListener;
import com.willwinder.ugs.nbp.designer.logic.events.EventType;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractEntity implements Entity {

    private static final Logger LOGGER = Logger.getLogger(AbstractEntity.class.getSimpleName());
    private Entity parent;
    private AffineTransform transform = new AffineTransform();

    private double rotation = 0;
    private Point2D movementPoint = new Point2D.Double();

    private AffineTransform positionTransform = new AffineTransform();
    private AffineTransform rotationTransform = new AffineTransform();
    private AffineTransform movementTransform = new AffineTransform();


    private List<EntityListener> listeners = new ArrayList<>();

    public AbstractEntity() {
    }

    public AbstractEntity(double x, double y) {
        setPosition(x, y);
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

    public void setPosition(double x, double y) {
        positionTransform = new AffineTransform();
        positionTransform.translate(x, y);
    }

    @Override
    public Entity getParent() {
        return parent;
    }

    @Override
    public void setParent(Entity shape) {
        this.parent = shape;
    }

    @Override
    public Point getCenter() {
        Rectangle bounds = getBounds();
        return new Point((int) (bounds.getCenterX()), (int) (bounds.getCenterY()));
    }

    @Override
    public void destroy() {
        listeners.clear();
    }

    @Override
    public AffineTransform getTransform() {
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.concatenate(this.movementTransform);
        affineTransform.concatenate(this.positionTransform);
        affineTransform.concatenate(this.transform);
        affineTransform.concatenate(this.rotationTransform);
        return affineTransform;
    }

    public void setTransform(AffineTransform transform) {
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
            ctm.preConcatenate(node.getTransform());
            node = node.getParent();
        }
        return ctm;
    }

    @Override
    public void move(Point2D deltaMovement) {
        try {
            // Fetch a base transformer to use
            AffineTransform transformer = transform;
            if (parent != null) {
                transformer = getParent().getGlobalTransform();
            }

            // Figure out the real and relative position before
            Point2D.Double realPositionBefore = new Point2D.Double();
            Point2D relativePositionBefore = new Point2D.Double();
            transformer.transform(new Point2D.Double(getShape().getBounds().getX(), getShape().getBounds().getY()), realPositionBefore);
            transformer.inverseTransform(realPositionBefore, relativePositionBefore);

            // Figure out the real and relative position after
            Point2D.Double realPositionAfter = new Point2D.Double(realPositionBefore.getX() + deltaMovement.getX(), realPositionBefore.getY() + deltaMovement.getY());
            Point2D relativePositionAfter = new Point2D.Double();
            transformer.inverseTransform(realPositionAfter, relativePositionAfter);

            // Calculate relative delta
            Point2D.Double realDelta = new Point2D.Double(realPositionAfter.getX() - realPositionBefore.getX(), realPositionAfter.getY() - realPositionBefore.getY());
            Point2D relativeDelta = new Point2D.Double(relativePositionAfter.getX() - relativePositionBefore.getX(), relativePositionAfter.getY() - relativePositionBefore.getY());
            LOGGER.finest("Real delta " + realDelta + " -> Relative delta " + relativeDelta);

            movementPoint.setLocation(movementPoint.getX() + relativeDelta.getX(), movementPoint.getY() + relativeDelta.getY());
            movementTransform = new AffineTransform();
            movementTransform.translate(movementPoint.getX(), movementPoint.getY());
            notifyEvent(new EntityEvent(this, EventType.MOVED));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't move the entity", e);
        }
    }

    @Override
    public double getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(double angle) {
        try {
            rotation = angle;
            rotationTransform = new AffineTransform();
            rotationTransform.rotate((rotation / 180d) * Math.PI, getRelativeShape().getBounds().getCenterX(), getRelativeShape().getBounds().getCenterY());
            notifyEvent(new EntityEvent(this, EventType.ROTATED));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't set the rotation", e);
        }
    }
}
