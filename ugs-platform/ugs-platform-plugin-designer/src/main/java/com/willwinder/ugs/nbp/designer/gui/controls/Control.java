package com.willwinder.ugs.nbp.designer.gui.controls;


import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.events.EntityListener;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;

public interface Control extends EntityListener, Entity {
    SelectionManager getSelectionManager();

    Entity getTarget();
}
