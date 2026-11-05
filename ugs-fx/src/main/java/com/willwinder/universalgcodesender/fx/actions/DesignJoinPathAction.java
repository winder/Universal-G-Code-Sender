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
 * Joins the two loose ends selected in the vertex tool into one node halfway between them:
 * the ends of two parts of a path make one part, the two ends of one part close it. Enabled
 * only when exactly two loose ends are selected.
 */
public class DesignJoinPathAction extends AbstractDesignEditAction {
    public static final String ICON_BASE = "icons/join-path.svg";

    public DesignJoinPathAction() {
        super(Localization.getString("platform.designer.joinPath"), ICON_BASE);
        enabledProperty().bind(VertexEditing.canJoinProperty());
    }

    @Override
    protected void performAction() {
        VertexEditing.joinPath();
    }
}
