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

import com.willwinder.ugs.nbp.designer.entities.*;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;
import com.willwinder.ugs.nbp.designer.entities.controls.ModifyControls;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joacim Breiler
 */
public class SelectionManager extends AbstractEntity {

    private static final Logger LOGGER = Logger.getLogger(SelectionManager.class.getSimpleName());
    private final Set<SelectionListener> listeners = new HashSet<>();
    private final EntityGroup entityGroup;
    private final ModifyControls modifyControls;

    public SelectionManager() {
        super();
        entityGroup = new EntityGroup();
        modifyControls = new ModifyControls(this);
    }

    @Override
    public final void render(Graphics2D graphics) {
        if (!entityGroup.getChildren().isEmpty()) {
            // Highlight the selected models
            getSelection().forEach(entity -> {
                graphics.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{1, 1}, 0));
                graphics.setColor(Colors.CONTROL_BORDER);
                graphics.draw(entity.getShape());
            });

            modifyControls.render(graphics);
        }
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
        if (entity == this) {
            return;
        }
        entityGroup.addChild(entity);
        fireSelectionEvent(new SelectionEvent());
    }

    public void setSelection(List<Entity> entities) {
        entityGroup.removeAll();
        entityGroup.addAll(entities);
        fireSelectionEvent(new SelectionEvent());
    }

    public void removeSelection(Entity entity) {
        entityGroup.removeChild(entity);
        fireSelectionEvent(new SelectionEvent());
    }

    public void addSelectionListener(SelectionListener selectionListener) {
        this.listeners.add(selectionListener);
    }


    public void removeSelectionListener(SelectionListener selectionListener) {
        if (!this.listeners.contains(selectionListener)) {
            this.listeners.remove(selectionListener);
        }
    }

    private void fireSelectionEvent(SelectionEvent selectionEvent) {
        new ArrayList<>(this.listeners)
                .forEach(listener -> listener.
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

    public List<Control> getControls() {
        return modifyControls.getAllChildren().stream()
                .filter(Control.class::isInstance)
                .map(Control.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Point2D getCenter() {
        return entityGroup.getCenter();
    }

    @Override
    public void move(Point2D deltaMovement) {
        entityGroup.move(deltaMovement);
        notifyEvent(new EntityEvent(this, EventType.MOVED));
    }

    @Override
    public void rotate(double angle) {
        entityGroup.rotate(angle);
        notifyEvent(new EntityEvent(this, EventType.ROTATED));
    }

    @Override
    public void rotate(Point2D center, double angle) {
        entityGroup.rotate(center, angle);
        notifyEvent(new EntityEvent(this, EventType.ROTATED));
    }

    @Override
    public double getRotation() {
        return entityGroup.getRotation();
    }

    @Override
    public void setRotation(double rotation) {
        entityGroup.setRotation(rotation);
        notifyEvent(new EntityEvent(this, EventType.ROTATED));
    }

    @Override
    public void scale(double sx, double sy) {
        entityGroup.scale(sx, sy);
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }

    @Override
    public Size getSize() {
        if(entityGroup.getChildren().size() == 1) {
            return entityGroup.getChildren().get(0).getSize();
        }

        Rectangle2D bounds = getShape().getBounds2D();
        return new Size(bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(Size size) {
        LOGGER.warning("SelectionManager.setSize() is not implemented");
    }

    public void toggleSelection(Entity entity) {
        if (isSelected(entity)) {
            removeSelection(entity);
        } else {
            addSelection(entity);
        }
    }
}
