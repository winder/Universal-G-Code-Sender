package com.willwinder.ugs.nbp.designer.logic.selection;

import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.controls.Control;
import com.willwinder.ugs.nbp.designer.gui.controls.ModifyControls;
import com.willwinder.ugs.nbp.designer.gui.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.gui.entities.EventType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SelectionManager extends AbstractEntity {

    private List<SelectionListener> listeners = new ArrayList<>();
    private List<Entity> selectedEntities = new ArrayList<>();
    private ModifyControls modifyControls;

    public SelectionManager() {
        super();
        modifyControls = new ModifyControls(this, this);
    }

    @Override
    public final void render(Graphics2D graphics) {
        // Highlight the selected models
        selectedEntities.forEach(entity -> {
            graphics.setStroke(new BasicStroke(1f, 0, 0, 1, new float[]{1, 1}, 0));
            graphics.setColor(Colors.CONTROL_BORDER);
            graphics.draw(entity.getShape());
        });

        if (!selectedEntities.isEmpty()) {
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

    public void addSelectionListener(SelectionListener selectionListener) {
        if (!this.listeners.contains(selectionListener)) {
            this.listeners.add(selectionListener);
        }
    }

    private void fireSelectionEvent(SelectionEvent selectionEvent) {
        this.listeners.forEach(listener -> listener.onSelectionEvent(selectionEvent));
    }

    public boolean isSelected(Entity shape) {
        return selectedEntities.contains(shape);
    }

    public List<Entity> getSelection() {
        return Collections.unmodifiableList(new ArrayList<>(selectedEntities));
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
        try {
            selectedEntities.forEach(entity -> {
                entity.rotate(getCenter(), angle);
            });
            notifyEvent(new EntityEvent(this, EventType.ROTATED));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't set the rotation", e);
        }

    }

    @Override
    public void setSize(Dimension s) {
        System.out.println("SelectionManager.setSize() is not implemented");
    }
}
