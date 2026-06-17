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

import com.willwinder.universalgcodesender.fx.actions.DesignBreakApartAction;
import com.willwinder.universalgcodesender.fx.actions.DesignGroupAction;
import com.willwinder.universalgcodesender.fx.actions.DesignIntersectionAction;
import com.willwinder.universalgcodesender.fx.actions.DesignStitchAction;
import com.willwinder.universalgcodesender.fx.actions.DesignSubtractAction;
import com.willwinder.universalgcodesender.fx.actions.DesignUnionAction;

/**
 * A toolbar of geometric operations (union, subtract, intersection, break apart, stitch) and the
 * grouping action, laid out as flowing buttons. Each action enables itself based on the current
 * selection (for example, a union needs at least two selected entities while a break apart needs
 * a single compound path).
 */
public class DesignOperationToolbar extends AbstractDesignToolbar {

    public DesignOperationToolbar() {
        super("design-operation-toolbar");

        addButton(DesignUnionAction.class);
        addButton(DesignSubtractAction.class);
        addButton(DesignIntersectionAction.class);
        addButton(DesignBreakApartAction.class);
        addButton(DesignStitchAction.class);
        addButton(DesignGroupAction.class);
    }
}
