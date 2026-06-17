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

import com.willwinder.universalgcodesender.fx.actions.DesignCircleAction;
import com.willwinder.universalgcodesender.fx.actions.DesignClipartAction;
import com.willwinder.universalgcodesender.fx.actions.DesignImportAction;
import com.willwinder.universalgcodesender.fx.actions.DesignLineAction;
import com.willwinder.universalgcodesender.fx.actions.DesignPointAction;
import com.willwinder.universalgcodesender.fx.actions.DesignRectangleAction;
import com.willwinder.universalgcodesender.fx.actions.DesignSelectAction;
import com.willwinder.universalgcodesender.fx.actions.DesignTextAction;
import com.willwinder.universalgcodesender.fx.actions.DesignTraceImageAction;

/**
 * A toolbox of drawing actions (rectangle, ellipse, point, text, line) and import actions
 * (import file, clipart, trace image) laid out as flowing buttons. Tool actions are rendered
 * as toggle buttons that stay highlighted while the matching tool is active in the designer.
 */
public class DesignToolbar extends AbstractDesignToolbar {

    public DesignToolbar() {
        super("design-tool-toolbar");

        addToggleButton(DesignSelectAction.class);
        addToggleButton(DesignRectangleAction.class);
        addToggleButton(DesignCircleAction.class);
        addToggleButton(DesignPointAction.class);
        addToggleButton(DesignTextAction.class);
        addToggleButton(DesignLineAction.class);

        addButton(DesignImportAction.class);
        addButton(DesignClipartAction.class);
        addButton(DesignTraceImageAction.class);
    }
}
