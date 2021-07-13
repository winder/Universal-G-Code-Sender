/*
    Copyright 2021 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

/**
 * @author Joacim Breiler
 */
public class ModifyControls extends AbstractControlEntityGroup {

    public ModifyControls(SelectionManager selectionManager) {
        super(selectionManager);
        addChild(new MoveControl(selectionManager));
        addChild(new RotationControl(selectionManager));
        addChild(new ResizeControl(selectionManager, Location.BOTTOM_LEFT));
        addChild(new ResizeControl(selectionManager, Location.BOTTOM_RIGHT));
        addChild(new ResizeControl(selectionManager, Location.TOP_LEFT));
        addChild(new ResizeControl(selectionManager, Location.TOP_RIGHT));
    }


    @Override
    public void onEvent(EntityEvent entityEvent) {

    }
}
