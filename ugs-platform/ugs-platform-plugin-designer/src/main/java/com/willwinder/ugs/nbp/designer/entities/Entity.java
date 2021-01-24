package com.willwinder.ugs.nbp.designer.entities;


import com.willwinder.ugs.nbp.designer.cut.CutSettings;
import com.willwinder.ugs.nbp.designer.logic.events.ShapeEvent;
import com.willwinder.ugs.nbp.designer.logic.events.ShapeListener;

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
    private List<ShapeListener> listeners = new ArrayList<>();

    private double rotation = 0;
    private Point2D movementPoint = new Point2D.Double();

    private AffineTransform rotationTransform = new AffineTransform();
    private AffineTransform movementTransform = new AffineTransform();

    public Entity() {
    }

    public void notifyShapeEvent(ShapeEvent shapeEvent) {
        listeners.forEach(shapeListener -> shapeListener.onShapeEvent(shapeEvent));
    }

    public List<Entity> getShapes() {
        return Collections.unmodifiableList(this.shapes);
    }

    public List<Entity> getChildrenAt(Point2D p) {

        List<Entity> result = new ArrayList<>();
        result.addAll(includes(p));
        result.addAll(getShapes()
                .stream()
                .flatMap(s -> s.getChildrenAt(p).stream()).collect(Collectors.toList()));

        return result;
    }

    public List<Entity> includes(Point2D p) {
        ArrayList<Entity> shapes = new ArrayList<>();

        Shape transformedShape = getBounds();
        if (transformedShape.contains(p)) {
            shapes.add(this);
            return shapes;
        }

        return shapes;
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

    public abstract Shape getRawShape();

    public Shape getBoundingBox() {
        return getBounds();
    }

    public Rectangle getBounds() {
        return getShape().getBounds();
    }

    public Point2D getPosition() {
        return getShape().getBounds().getLocation();
    }

    public Point2D getRelativePosition() {
        return toRelativePoint(getShape().getBounds().getLocation());
    }

    public void empty() {
        shapes.clear();
    }

    public void add(Entity node) {
        if (!contains(node)) {
            shapes.add(node);
        }
    }

    public void remove(Entity shape) {
        shapes.remove(shape);
    }

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity shape) {
        this.parent = shape;
    }

    public boolean contains(Entity node) {
        return shapes.contains(node);
    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }


    public void addListener(ShapeListener shapeListener) {
        this.listeners.add(shapeListener);
    }

    public void removeListener(ShapeListener shapeListener) {
        this.listeners.remove(shapeListener);
    }

    public Point getCenter() {
        return new Point((int) (getShape().getBounds().getCenterX()), (int) (getShape().getBounds().getCenterY()));
    }

    public void addAll(List<? extends Entity> shapes) {
        this.shapes.addAll(shapes);
    }

    public void destroy() {
        listeners.clear();
        shapes.forEach(Entity::destroy);
    }

    public void removeAll(List<? extends Entity> shapes) {
        this.shapes.removeAll(shapes);
    }

    public Point2D toRealPoint(Point2D relativePoint) {
        try {
            Point2D result = new Point2D.Double();
            getGlobalTransform().transform(relativePoint, result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Point2D toRelativePoint(Point2D realPoint) {
        try {
            Point2D result = new Point2D.Double();
            getGlobalTransform().inverseTransform(realPoint, result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AffineTransform getTransform() {
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.concatenate(this.movementTransform);
        affineTransform.concatenate(this.rotationTransform);
        affineTransform.concatenate(this.transform);
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
            transformer.transform(new Point2D.Double(getRawShape().getBounds().getX(), getRawShape().getBounds().getY()), realPositionBefore);
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

        }
    }

    public void rotate(double deltaAngle) {
        rotation = +deltaAngle;
        setRotation(rotation);
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double angle) {
        try {
            // Fetch a base transformer to use
            AffineTransform transformer = new AffineTransform();
            if (parent != null) {
                transformer = getParent().getGlobalTransform();
            }
            transformer.concatenate(transform);
            //transformer.concatenate(movementTransform);

            // Figure out the real and relative position before
            Point2D.Double realPosition = new Point2D.Double();
            Point2D.Double realCenter = new Point2D.Double();
            transformer.transform(new Point2D.Double(getRawShape().getBounds().getX(), getRawShape().getBounds().getY()), realPosition);
            transformer.transform(new Point2D.Double(getRawShape().getBounds().getCenterX(), getRawShape().getBounds().getCenterY()), realCenter);

            Point2D relativePosition = new Point2D.Double();
            Point2D relativeCenter = new Point2D.Double();
            transformer.inverseTransform(realPosition, relativePosition);
            transformer.inverseTransform(realCenter, relativeCenter);

            System.out.println("\tReal pos: " + realPosition + " Real center " + realCenter + " -> Relative pos: " + relativePosition + " Relative center " + relativeCenter);

            rotation = angle;
            rotationTransform = new AffineTransform();
            rotationTransform.rotate((rotation / 180d) * Math.PI, relativeCenter.getX(), relativeCenter.getY());
        } catch (Exception e) {

        }
    }

    public CutSettings getCutSettings() {
        return cutSettings;
    }
}