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
package com.willwinder.ugs.nbp.designer.entities.selection;

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityException;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Point;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joacim Breiler
 */
public class SelectionManager extends AbstractEntity implements EntityListener {

    private final Set<SelectionListener> listeners = Sets.newConcurrentHashSet();
    private final EntityGroup entityGroup;

    public SelectionManager() {
        super();
        entityGroup = new EntityGroup();
        entityGroup.addListener(this);
    }

    @Override
    public final void render(Graphics2D graphics, Drawing drawing) {
        if (isEmpty()) {
            return;
        }

        // Highlight the selected models
        float strokeWidth = 1.6f / (float) drawing.getScale();
        float dashWidth = 2f / (float) drawing.getScale();
        graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{dashWidth, dashWidth}, 0));
        getSelection().forEach(entity -> drawEntity(graphics, entity));
    }

    private void drawEntity(Graphics2D graphics, Entity entity) {
        boolean isHidden = entity instanceof Cuttable && ((Cuttable) entity).isHidden();
        Color color = isHidden ? Colors.SHAPE_HINT : Colors.SHAPE_OUTLINE;
        graphics.setColor(color);
        graphics.draw(entity.getShape());
    }

    @Override
    public Shape getShape() {
        return entityGroup.getShape();
    }

    @Override
    public Shape getRelativeShape() {
        try {
            return getTransform().createInverse().createTransformedShape(getShape());
        } catch (NoninvertibleTransformException e) {
            throw new SelectionException("Could not create inverse transformer", e);
        }
    }

    public void clearSelection() {
        entityGroup.removeAll();
        setTransform(new AffineTransform());
        fireSelectionEvent(new SelectionEvent());
    }

    public void addSelection(Entity entity) {
        if (entity == this || entity instanceof Control) {
            return;
        }
        entityGroup.addChild(entity);
        fireSelectionEvent(new SelectionEvent());
    }

    public void addSelection(List<Entity> entities) {
        entityGroup.addAll(entities.stream()
                .filter(entity -> entity != this || !(entity instanceof Control))
                .collect(Collectors.toList()));

        fireSelectionEvent(new SelectionEvent());
    }

    public void removeSelection(Entity entity) {
        entityGroup.removeChild(entity);
        entity.removeListener(this);
        fireSelectionEvent(new SelectionEvent());
    }

    public void addSelectionListener(SelectionListener selectionListener) {
        this.listeners.add(selectionListener);
    }

    public void removeSelectionListener(SelectionListener selectionListener) {
        listeners.remove(selectionListener);
    }

    private void fireSelectionEvent(SelectionEvent selectionEvent) {
        listeners.forEach(listener -> listener.
                onSelectionEvent(selectionEvent));
    }

    public boolean isSelected(Entity entity) {
        return entityGroup.getChildren().contains(entity);
    }

    public List<Entity> getSelection() {
        return entityGroup.getChildren().stream()
                .flatMap(entity -> {
                    if (entity instanceof EntityGroup) {
                        return ((EntityGroup) entity).getAllChildren().stream();
                    } else {
                        return Stream.of(entity);
                    }
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public void setSelection(List<Entity> entities) {
        List<Entity> selection = entities.stream()
                .filter(e -> e != this)
                .filter(e -> !(e instanceof Control))
                .collect(Collectors.toList());

        entityGroup.removeAll();
        entityGroup.addAll(selection);
        fireSelectionEvent(new SelectionEvent());
    }

    public List<Entity> getChildren() {
        return entityGroup.getChildren();
    }

    @Override
    public Point2D getCenter() {
        return entityGroup.getCenter();
    }

    @Override
    public void move(Point2D deltaMovement) {
        entityGroup.move(deltaMovement);
    }

    @Override
    public void rotate(double angle) {
        entityGroup.rotate(angle);
    }

    @Override
    public void rotate(Point2D center, double angle) {
        entityGroup.rotate(center, angle);
    }

    @Override
    public double getRotation() {
        return entityGroup.getRotation();
    }

    @Override
    public void setRotation(double rotation) {
        entityGroup.setRotation(rotation);
    }

    @Override
    public void scale(double sx, double sy) {
        entityGroup.scale(sx, sy);
    }

    @Override
    public Size getSize() {
        if (entityGroup.getChildren().size() == 1 && entityGroup.getChildren().get(0) instanceof Point) {
            return new Size(0, 0);
        }
        return entityGroup.getSize();
    }

    @Override
    public void setSize(Size size) {
        entityGroup.setSize(size);
    }

    public void toggleSelection(Entity entity) {
        if (isSelected(entity)) {
            removeSelection(entity);
        } else {
            addSelection(entity);
        }
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        notifyEvent(entityEvent);
    }

    public boolean isEmpty() {
        return entityGroup.getChildren().isEmpty();
    }

    @Override
    public Entity copy() {
        throw new EntityException("Not implemented");
    }
}
