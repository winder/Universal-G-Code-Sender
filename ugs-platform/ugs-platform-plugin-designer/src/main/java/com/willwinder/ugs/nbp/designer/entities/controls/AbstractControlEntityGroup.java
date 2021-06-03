package com.willwinder.ugs.nbp.designer.entities.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

/**
 * A control is either
 */
public abstract class AbstractControlEntityGroup extends EntityGroup implements EntityListener, Control {

    private final SelectionManager selectionManager;

    protected AbstractControlEntityGroup(SelectionManager selectionManager) {
        super.setParent(selectionManager);
        this.selectionManager = selectionManager;
        this.addListener(this);
        addListener(this);
    }

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
