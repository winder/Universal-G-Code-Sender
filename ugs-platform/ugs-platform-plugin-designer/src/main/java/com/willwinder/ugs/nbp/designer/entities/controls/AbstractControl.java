package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

import java.awt.Shape;

public abstract class AbstractControl extends AbstractEntity implements Control {

    private final SelectionManager selectionManager;

    protected AbstractControl(SelectionManager selectionManager) {
        super.setParent(selectionManager);
        this.selectionManager = selectionManager;
        addListener(this);
    }

    @Override
    public Shape getShape() {
        return selectionManager.getShape();
    }

    @Override
    public Shape getRelativeShape() {
        return selectionManager.getRelativeShape();
    }

    @Override
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public void setParent(Entity entity) {
        // Short circuit the set parent, we never want to change it
    }

    @Override
    public void destroy() {
        removeListener(this);
        super.destroy();
    }
}
