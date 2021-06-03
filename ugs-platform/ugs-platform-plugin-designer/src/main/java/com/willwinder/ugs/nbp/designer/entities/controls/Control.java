package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

public interface Control extends EntityListener, Entity {
    SelectionManager getSelectionManager();
}
