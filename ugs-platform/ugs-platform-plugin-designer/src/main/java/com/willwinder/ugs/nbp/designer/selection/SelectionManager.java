package com.willwinder.ugs.nbp.designer.selection;

import com.willwinder.ugs.nbp.designer.controls.Control;
import com.willwinder.ugs.nbp.designer.controls.ModifyControls;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.Group;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectionManager extends Group {

    private List<SelectionListener> listeners = new ArrayList<>();
    private List<Entity> selectedEntities = new ArrayList<>();

    public SelectionManager() {
        super();
    }

    @Override
    public Shape getShape() {
        Area area = new Area();
        selectedEntities.stream()
                .filter(c -> c != this)
                .forEach(c -> area.add(new Area(c.getShape())));

        return area.getBounds();
    }

    public void removeAll() {
        super.removeAll();
        selectedEntities.clear();
        fireSelectionEvent(new SelectionEvent());
    }

    public void add(Entity entity) {
        selectedEntities.add(entity);
        addChild(new ModifyControls(entity, this));
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

    public boolean contains(Entity shape) {
        return selectedEntities.contains(shape);
    }

    public List<Control> getControls() {
        return getAllChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .collect(Collectors.toList());
    }
}
