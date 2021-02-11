package com.willwinder.ugs.nbp.designer.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.events.EntityListener;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

public interface Control extends EntityListener, Entity {
    SelectionManager getSelectionManager();

    Entity getTarget();
}
