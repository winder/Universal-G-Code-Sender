package com.willwinder.ugs.nbp.designer.controls;

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.awt.Shape;

public abstract class AbstractControl extends AbstractEntity implements Control {

    private final SelectionManager selectionManager;
    private final Entity target;

    public AbstractControl(Entity target, SelectionManager selectionManager) {
        this.target = target;
        super.setParent(selectionManager);
        this.selectionManager = selectionManager;
        addListener(this);
    }

    @Override
    public Shape getShape() {
        return getTarget().getShape();
    }

    @Override
    public Shape getRelativeShape() {
        return getTarget().getRelativeShape();
    }

    @Override
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public void setParent(Entity shape) {
        // Short circuit the set parent, we never want to change it
    }

    @Override
    public Entity getTarget() {
        return target;
    }

    @Override
    public void destroy() {
        removeListener(this);
        super.destroy();
    }
}
