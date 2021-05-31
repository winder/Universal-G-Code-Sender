package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

public class ModifyControls extends AbstractControlEntityGroup {

    public ModifyControls(SelectionManager selectionManager) {
        super(selectionManager);
        addChild(new MoveControl(selectionManager));
        addChild(new RotationControl(selectionManager));
        addChild(new ResizeControl(selectionManager, Location.TOP_LEFT));
        addChild(new ResizeControl(selectionManager, Location.TOP_RIGHT));
        addChild(new ResizeControl(selectionManager, Location.BOTTOM_LEFT));
        addChild(new ResizeControl(selectionManager, Location.BOTTOM_RIGHT));
    }


    @Override
    public void onEvent(EntityEvent entityEvent) {

    }
}
