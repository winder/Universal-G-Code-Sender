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

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joacim Breiler
 */
public class EntityGroup extends AbstractEntity implements EntityListener {
    private final List<Entity> children = Collections.synchronizedList(new ArrayList<>());

    private double groupRotation = 0;
    private Point2D cachedCenter = new Point2D.Double(0, 0);

    public EntityGroup() {
        super();
        setName("Group");
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        children.forEach(node -> node.render(graphics, drawing));
    }

    @Override
    public void setSize(Size size) {
        Size originalSize = getSize();
        scale(size.getWidth() / originalSize.getWidth(), size.getHeight() / originalSize.getHeight());
    }

    @Override
    public void rotate(double angle) {
        try {
            groupRotation += angle;
            getAllChildren().forEach(entity -> entity.rotate(getCenter(), angle));
            notifyEvent(new EntityEvent(this, EventType.ROTATED));
        } catch (Exception e) {
            throw new EntityException("Couldn't set the rotation", e);
        }
    }

    @Override
    public void rotate(Point2D center, double angle) {
        try {
            groupRotation += angle;
            getAllChildren().forEach(entity -> entity.rotate(center, angle));
            notifyEvent(new EntityEvent(this, EventType.ROTATED));
            invalidateCenter();
        } catch (Exception e) {
            throw new EntityException("Couldn't set the rotation", e);
        }
    }

    @Override
    public Shape getShape() {
        final Area area = new Area();
        List<Entity> allChildren = getAllChildren();
        allChildren.stream()
                .filter(c -> c != this)
                .forEach(c -> area.add(new Area(c.getBounds())));
        return area.getBounds2D();
    }

    @Override
    public Shape getRelativeShape() {
        try {
            return getTransform().createInverse().createTransformedShape(getShape());
        } catch (NoninvertibleTransformException e) {
            throw new EntityException("Could not create inverse transformer");
        }
    }

    public void addChild(Entity node) {
        if (!containsChild(node)) {
            children.add(node);
            node.addListener(this);
            invalidateCenter();
        }
    }

    private void invalidateCenter() {
        cachedCenter = null;
    }

    @Override
    public Point2D getCenter() {
        if (cachedCenter == null) {
            Rectangle2D bounds = getShape().getBounds2D();
            cachedCenter = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        }

        return cachedCenter;
    }

    public void addAll(List<Entity> entities) {
        entities.forEach(entity -> {
            if (!containsChild(entity)) {
                children.add(entity);
                entity.addListener(this);
            }
        });
        invalidateCenter();
    }

    @Override
    public void applyTransform(AffineTransform transform) {
        if (children != null) {
            children.forEach(c -> c.applyTransform(transform));
        }
        invalidateCenter();
    }

    @Override
    public void move(Point2D deltaMovement) {
        try {
            applyTransform(AffineTransform.getTranslateInstance(deltaMovement.getX(), deltaMovement.getY()));
            notifyEvent(new EntityEvent(this, EventType.MOVED));
            invalidateCenter();
        } catch (Exception e) {
            throw new EntityException("Could not make inverse transform of point", e);
        }
    }

    @Override
    public void setTransform(AffineTransform transform) {
        children.forEach(c -> c.setTransform(transform));
        invalidateCenter();
    }

    public boolean containsChild(Entity node) {
        return children.contains(node);
    }

    public void removeChild(Entity entity) {
        entity.removeListener(this);
        children.remove(entity);
        children.stream()
                .filter(EntityGroup.class::isInstance)
                .map(EntityGroup.class::cast)
                .forEach(c -> c.removeChild(entity));

        invalidateCenter();
    }

    @Override
    public void destroy() {
        super.destroy();
        children.forEach(Entity::destroy);
    }

    public void removeAll() {
        this.groupRotation = 0;
        this.children.forEach(entity -> entity.removeListener(this));
        this.children.clear();
        invalidateCenter();
    }

    public List<Entity> getChildrenAt(Point2D p) {
        List<Entity> result = this.children
                .stream()
                .flatMap(s -> {
                    if (s instanceof EntityGroup) {
                        return ((EntityGroup) s).getChildrenAt(p).stream();
                    } else if (s.isWithin(p)) {
                        return Stream.of(s);
                    } else {
                        return Stream.empty();
                    }
                }).collect(Collectors.toList());

        return Collections.unmodifiableList(result);
    }

    public List<Entity> getChildrenIntersecting(Shape shape) {
        List<Entity> result = this.children
                .stream()
                .flatMap(s -> {
                    if (s instanceof EntityGroup) {
                        return ((EntityGroup) s).getChildrenIntersecting(shape).stream();
                    } else if (s.isIntersecting(shape)) {
                        return Stream.of(s);
                    } else {
                        return Stream.empty();
                    }
                }).collect(Collectors.toList());

        return Collections.unmodifiableList(result);
    }

    /**
     * Get a list of all direct children of this group
     *
     * @return a list of children entities
     */
    public final List<Entity> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public void setRotation(double rotation) {
        Point2D center = getCenter();
        double deltaRotation = rotation - getRotation();
        if (deltaRotation != 0) {
            children.forEach(entity -> entity.rotate(center, deltaRotation));
        }
        groupRotation += deltaRotation;
        notifyEvent(new EntityEvent(this, EventType.ROTATED));
        invalidateCenter();
    }

    @Override
    public double getRotation() {
        if (children.size() == 1) {
            groupRotation = children.get(0).getRotation();
        }
        return Utils.normalizeRotation(groupRotation);
    }

    public final List<Entity> getAllChildren() {
        if (this.children == null) {
            return Collections.emptyList();
        }

        List<Entity> result = this.children
                .stream()
                .flatMap(s -> {
                    if (s instanceof EntityGroup) {
                        return ((EntityGroup) s).getAllChildren().stream();
                    } else {
                        return Stream.of(s);
                    }
                }).collect(Collectors.toList());

        return Collections.unmodifiableList(result);
    }

    @Override
    public void scale(double sx, double sy) {
        Point2D originalPosition = getPosition();
        this.children.forEach(child -> {
            Point2D childOriginalPosition = child.getPosition();
            Point2D relativePosition = new Point2D.Double(childOriginalPosition.getX() - originalPosition.getX(), childOriginalPosition.getY() - originalPosition.getY());
            child.scale(sx, sy);
            child.setPosition(new Point2D.Double(originalPosition.getX() + (relativePosition.getX() * sx), originalPosition.getY() + (relativePosition.getY() * sy)));
        });
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
        invalidateCenter();
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        notifyEvent(entityEvent);
    }

    @Override
    public Entity copy() {
        EntityGroup group = new EntityGroup();
        getChildren().stream().map(Entity::copy).forEach(group::addChild);
        return group;
    }
}
