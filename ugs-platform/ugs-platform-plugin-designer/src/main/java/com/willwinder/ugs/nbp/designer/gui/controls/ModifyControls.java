package com.willwinder.ugs.nbp.designer.gui.controls;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;

public class ModifyControls extends AbstractControlGroup {

    public ModifyControls(Entity target, SelectionManager selectionManager) {
        super(target, selectionManager);
        addChild(new RotationControl(target, selectionManager));
        addChild(new ResizeControl(target, selectionManager, Location.TOP_LEFT));
        addChild(new ResizeControl(target, selectionManager, Location.TOP_RIGHT));
        addChild(new ResizeControl(target, selectionManager, Location.BOTTOM_LEFT));
        addChild(new ResizeControl(target, selectionManager, Location.BOTTOM_RIGHT));
        addChild(new MoveControl(target, selectionManager));
    }


    @Override
    public void onEvent(EntityEvent entityEvent) {

    }
}
