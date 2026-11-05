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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.fx.component.designer.editor.VertexEditing;
import com.willwinder.universalgcodesender.i18n.Localization;

/**
 * Breaks a path at the node selected in the vertex tool: an open path is split in two at the
 * node, a closed one is opened there. The parts stay in the same path and can be separated
 * with {@link DesignBreakApartAction}. Enabled only when the selected node can be broken.
 */
public class DesignBreakPathAction extends AbstractDesignEditAction {
    public static final String ICON_BASE = "icons/break-path.svg";

    public DesignBreakPathAction() {
        super(Localization.getString("platform.designer.breakPath"), ICON_BASE);
        enabledProperty().bind(VertexEditing.canBreakProperty());
    }

    @Override
    protected void performAction() {
        VertexEditing.breakAtSelectedNode();
    }
}
