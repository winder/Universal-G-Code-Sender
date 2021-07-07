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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joacim Breiler
 */
public class EntityGroup extends AbstractEntity {
    private List<Entity> children = new ArrayList<>();

    double groupRotation = 0;

    public EntityGroup() {
        setName("Group");
    }

    @Override
    public void render(Graphics2D graphics) {
        children.forEach(node -> node.render(graphics));
    }

    @Override
    public void setSize(Dimension s) {

    }

    @Override
    public void rotate(double angle) {
        rotate(getCenter(), angle);
    }

    @Override
    public void rotate(Point2D center, double angle) {
        try {
            groupRotation += angle;
            getAllChildren().forEach(entity -> entity.rotate(center, angle));
            notifyEvent(new EntityEvent(this, EventType.ROTATED));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't set the rotation", e);
        }
    }

    @Override
    public Shape getShape() {
        Area area = new Area();
        List<Entity> allChildren = getAllChildren();
        allChildren.stream()
                .filter(c -> c != this)
                .forEach(c -> area.add(new Area(c.getShape())));

        return area.getBounds();
    }

    @Override
    public Shape getRelativeShape() {
        try {
            return getTransform().createInverse().createTransformedShape(getShape());
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Could not create inverse transformer");
        }
    }

    public void addChild(Entity node) {
        if (!containsChild(node)) {
            children.add(node);
            node.setParent(this);
        }
    }

    @Override
    public void applyTransform(AffineTransform transform) {
        children.forEach(c -> c.applyTransform(transform));
    }

    @Override
    public void move(Point2D deltaMovement) {
        try {
            applyTransform(AffineTransform.getTranslateInstance(deltaMovement.getX(), deltaMovement.getY()));
            notifyEvent(new EntityEvent(this, EventType.MOVED));
        } catch (Exception e) {
            throw new RuntimeException("Could not make inverse transform of point", e);
        }
    }

    @Override
    public void setTransform(AffineTransform transform) {
        children.forEach(c -> c.setTransform(transform));
    }

    public boolean containsChild(Entity node) {
        return children.contains(node);
    }

    public void removeChild(Entity entity) {
        children.remove(entity);
        children.stream()
                .filter(EntityGroup.class::isInstance)
                .map(EntityGroup.class::cast)
                .forEach(c -> c.removeChild(entity));
    }

    @Override
    public void destroy() {
        super.destroy();
        children.forEach(Entity::destroy);
    }

    public void removeAll(List<? extends Entity> shapes) {
        this.children.removeAll(shapes);
    }

    public void removeAll() {
        this.children.clear();
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

    /**
     * Get a list of all direct children of this group
     *
     * @return a list of children entites
     */
    public final List<Entity> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public void setRotation(double rotation) {
        Point2D center = getCenter();
        double deltaRotation = getRotation() - rotation;
        if (deltaRotation != 0) {
            children.forEach(entity -> entity.rotate(center, deltaRotation));
        }
    }

    public final List<Entity> getAllChildren() {
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
}
