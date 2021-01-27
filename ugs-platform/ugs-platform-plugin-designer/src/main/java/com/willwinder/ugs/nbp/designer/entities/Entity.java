package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.cut.CutSettings;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EntityListener;

import java.awt.Rectangle;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class Entity {

    private static final Logger LOGGER = Logger.getLogger(Entity.class.getSimpleName());
    private Entity parent;

    private CutSettings cutSettings = new CutSettings();
    private List<Entity> shapes = new ArrayList<>();
    private AffineTransform transform = new AffineTransform();

    private double rotation = 0;
    private Point2D movementPoint = new Point2D.Double();

    private AffineTransform positionTransform = new AffineTransform();
    private AffineTransform rotationTransform = new AffineTransform();
    private AffineTransform movementTransform = new AffineTransform();


    private List<EntityListener> listeners = new ArrayList<>();
    public void notifyEvent(EntityEvent entityEvent) {
        listeners.forEach(entityListener -> entityListener.onEvent(entityEvent));
    }

    public void addListener(EntityListener entityListener) {
        this.listeners.add(entityListener);
    }

    public void removeListener(EntityListener entityListener) {
        this.listeners.remove(entityListener);
    }


    public Entity() {
    }

    public Entity(double x, double y) {
        setPosition(x, y);
    }

    public List<Entity> getShapes() {
        return Collections.unmodifiableList(this.shapes);
    }

    public List<Entity> getChildrenAt(Point2D p) {

        List<Entity> result = new ArrayList<>();
        if (isWithin(p)) {
            result.add(this);
        }
        result.addAll(getShapes()
                .stream()
                .flatMap(s -> s.getChildrenAt(p).stream()).collect(Collectors.toList()));

        return result;
    }

    /**
     * Returns if the given point is within the given entity
     *
     * @param p the point to check
     * @return true if the point is within the entity
     */
    public boolean isWithin(Point2D p) {
        Shape transformedShape = getGlobalTransform().createTransformedShape(getBounds());
        return transformedShape.contains(p);
    }


    public final void draw(Graphics2D g) {
        drawShape(g);
        getShapes().forEach(node -> node.draw(g));
    }

    public abstract void drawShape(Graphics2D g);

    public Point getSize() {
        Rectangle bounds = getShape().getBounds();
        return new Point((int) bounds.getWidth(), (int) bounds.getHeight());
    }

    public abstract void setSize(Point2D s);

    public abstract Shape getShape();

    public Rectangle getBounds() {
        return getShape().getBounds();
    }

    public Point2D getPosition() {
        return getGlobalTransform().createTransformedShape(getShape())
                .getBounds()
                .getLocation();
    }

    public void setPosition(double x, double y) {
        positionTransform = new AffineTransform();
        positionTransform.translate(x, y);
    }

    public void addChild(Entity node) {
        if (!containsChild(node)) {
            shapes.add(node);
        }
    }

    public void removeChild(Entity shape) {
        shapes.remove(shape);
    }

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity shape) {
        this.parent = shape;
    }

    public boolean containsChild(Entity node) {
        return shapes.contains(node);
    }

    public Point getCenter() {
        Rectangle bounds = getGlobalTransform().createTransformedShape(getShape())
                .getBounds();

        return new Point((int) (bounds.getCenterX()), (int) (bounds.getCenterY()));
    }

    public void destroy() {
        listeners.clear();
        shapes.forEach(Entity::destroy);
    }

    public void removeAll(List<? extends Entity> shapes) {
        this.shapes.removeAll(shapes);
    }

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
    public String toString() {
        return "Shape{" +
                "parent=" + parent +
                ", position=" + getPosition() +
                ", affineTransform=" + transform +
                '}';
    }

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
        } catch (Exception e) {
            throw new RuntimeException("Couldn't move the entity", e);
        }
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double angle) {
        try {
            rotation = angle;
            rotationTransform = new AffineTransform();
            rotationTransform.rotate((rotation / 180d) * Math.PI, getShape().getBounds().getCenterX(), getShape().getBounds().getCenterY());
        } catch (Exception e) {
            throw new RuntimeException("Couldn't set the rotation", e);
        }
    }

    public CutSettings getCutSettings() {
        return cutSettings;
    }
}