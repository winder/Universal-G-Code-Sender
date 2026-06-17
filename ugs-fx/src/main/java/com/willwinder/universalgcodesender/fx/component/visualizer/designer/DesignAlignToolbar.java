/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.universalgcodesender.fx.actions.DesignAlignBottomAction;
import com.willwinder.universalgcodesender.fx.actions.DesignAlignCenterAction;
import com.willwinder.universalgcodesender.fx.actions.DesignAlignLeftAction;
import com.willwinder.universalgcodesender.fx.actions.DesignAlignMiddleAction;
import com.willwinder.universalgcodesender.fx.actions.DesignAlignRightAction;
import com.willwinder.universalgcodesender.fx.actions.DesignAlignTopAction;

/**
 * A toolbar of alignment actions (align left, right, top, bottom and the horizontal/vertical
 * centering actions) laid out as flowing buttons. The actions stay disabled until more than one
 * entity is selected, since alignment is performed relative to the first selected entity.
 */
public class DesignAlignToolbar extends AbstractDesignToolbar {

    public DesignAlignToolbar() {
        super("design-align-toolbar");

        addButton(DesignAlignLeftAction.class);
        addButton(DesignAlignCenterAction.class);
        addButton(DesignAlignRightAction.class);
        addButton(DesignAlignTopAction.class);
        addButton(DesignAlignMiddleAction.class);
        addButton(DesignAlignBottomAction.class);
    }
}
