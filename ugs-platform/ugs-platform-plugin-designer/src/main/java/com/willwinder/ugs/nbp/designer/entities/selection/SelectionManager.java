package com.willwinder.ugs.nbp.designer.entities.selection;

import com.willwinder.ugs.nbp.designer.entities.*;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;
import com.willwinder.ugs.nbp.designer.entities.controls.ModifyControls;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectionManager extends AbstractEntity {

    private Set<SelectionListener> listeners = new HashSet<>();
    private Set<Entity> selectedEntities = new HashSet<>();
    private ModifyControls modifyControls;

    public SelectionManager() {
        super();
        modifyControls = new ModifyControls(this);
    }

    @Override
    public final void render(Graphics2D graphics) {
        if (!selectedEntities.isEmpty()) {
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
        Area area = new Area();
        selectedEntities.stream()
                .filter(c -> c != this)
                .forEach(c -> area.add(new Area(c.getShape())));

        return area.getBounds2D();
    }

    @Override
    public Shape getRelativeShape() {
        try {
            return getTransform().createInverse().createTransformedShape(getShape());
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Could not create inverse transformer");
        }
    }

    public void clearSelection() {
        selectedEntities.clear();
        setTransform(new AffineTransform());
        fireSelectionEvent(new SelectionEvent());
    }

    public void addSelection(Entity entity) {
        selectedEntities.add(entity);
        fireSelectionEvent(new SelectionEvent());
    }

    public void setSelection(List<Entity> entities) {
        selectedEntities.clear();
        selectedEntities.addAll(entities);
        fireSelectionEvent(new SelectionEvent());
    }

    public void removeSelection(Entity entity) {
        selectedEntities.remove(entity);
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
                .forEach(listener -> listener.onSelectionEvent(selectionEvent));
    }

    public boolean isSelected(Entity shape) {
        return selectedEntities.contains(shape);
    }

    public List<Entity> getSelection() {
        return selectedEntities.stream()
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
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .collect(Collectors.toList());
    }

    @Override
    public void move(Point2D deltaMovement) {
        getSelection().forEach(e -> e.move(deltaMovement));
    }

    @Override
    public double getRotation() {
        if (getSelection().size() == 1) {
            return getSelection().get(0).getRotation();
        }
        return super.getRotation();
    }

    @Override
    public void rotate(double angle) {
        rotate(getCenter(), angle);
    }

    @Override
    public void rotate(Point2D center, double angle) {
        try {
            selectedEntities.forEach(entity -> entity.rotate(center, angle));
            notifyEvent(new EntityEvent(this, EventType.ROTATED));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't set the rotation", e);
        }
    }


    @Override
    public void scale(double sx, double sy) {
        getSelection().forEach(e -> e.scale(sx, sy));
    }

    @Override
    public void setSize(Dimension s) {
        System.out.println("SelectionManager.setSize() is not implemented");
    }

    public void toggleSelection(Entity entity) {
        if (isSelected(entity)) {
            removeSelection(entity);
        } else {
            addSelection(entity);
        }
    }
}
