package com.willwinder.ugs.nbp.designer.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.Group;
import com.willwinder.ugs.nbp.designer.logic.events.EntityListener;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

/**
 * A control is either
 */
public abstract class AbstractControlGroup extends Group implements EntityListener, Control {

    private final SelectionManager selectionManager;
    private final Entity target;

    public AbstractControlGroup(Entity target, SelectionManager selectionManager) {
        super();
        this.target = target;
        super.setParent(selectionManager);
        this.selectionManager = selectionManager;
        this.addListener(this);
        addListener(this);
    }

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
