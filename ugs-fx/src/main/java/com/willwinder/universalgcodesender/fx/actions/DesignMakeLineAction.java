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
 * Turns the nodes selected in the vertex tool into simple corner nodes, with straight lines on
 * both sides. Enabled only when a selected node has a curve on one of its sides.
 */
public class DesignMakeLineAction extends AbstractDesignEditAction {
    public static final String ICON_BASE = "icons/node-line.svg";

    public DesignMakeLineAction() {
        super(Localization.getString("platform.designer.convertToLine"), ICON_BASE);
        enabledProperty().bind(VertexEditing.canMakeLineProperty());
    }

    @Override
    protected void performAction() {
        VertexEditing.makeLine();
    }
}
