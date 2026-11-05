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
 * Turns the nodes selected in the vertex tool into smooth curve nodes, with curve handles that
 * can be dragged to shape the curves on either side. Enabled only when a selected node has a
 * straight line on one of its sides.
 */
public class DesignMakeCurveAction extends AbstractDesignEditAction {
    public static final String ICON_BASE = "icons/node-curve.svg";

    public DesignMakeCurveAction() {
        super(Localization.getString("platform.designer.convertToCurve"), ICON_BASE);
        enabledProperty().bind(VertexEditing.canMakeCurveProperty());
    }

    @Override
    protected void performAction() {
        VertexEditing.makeCurve();
    }
}
