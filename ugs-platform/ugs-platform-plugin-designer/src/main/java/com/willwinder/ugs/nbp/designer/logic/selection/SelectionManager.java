package com.willwinder.ugs.nbp.designer.logic.selection;



import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.controls.Control;
import com.willwinder.ugs.nbp.designer.logic.controls.ModifyControls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectionManager {

    private List<SelectionListener> listeners = new ArrayList<>();
    private List<Entity> shapes = new ArrayList<>();
    public SelectionManager() {
        super();
    }

    public void empty() {
        shapes.forEach(this::traverseAndRemoveControls);
        shapes.clear();
        fireSelectionEvent(new SelectionEvent());
    }

    private void traverseAndRemoveControls(Entity shape) {
        List<Entity> toBeRemoved = new ArrayList<>();
        shape.getShapes().forEach(child -> {
            if(child instanceof Control) {
                child.destroy();
                toBeRemoved.add(child);
            }

            traverseAndRemoveControls(child);
        });

        shape.removeAll(toBeRemoved);
    }

    public void add(Entity shape) {
        shape.add(new ModifyControls(shape));
        shapes.add(shape);
        fireSelectionEvent(new SelectionEvent());
    }

    public void addSelectionListener(SelectionListener selectionListener) {
        if(!this.listeners.contains(selectionListener)) {
            this.listeners.add(selectionListener);
        }
    }

    private void fireSelectionEvent(SelectionEvent selectionEvent) {
        this.listeners.forEach(listener -> listener.onSelectionEvent(selectionEvent));
    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }

    public List<Entity> getShapes() {
        return Collections.unmodifiableList(shapes);
    }

    public boolean contains(Entity shape) {
        return shapes.contains(shape);
    }
}
