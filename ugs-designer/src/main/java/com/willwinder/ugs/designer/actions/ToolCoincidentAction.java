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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.i18n.Localization;

/**
 * Selects the tool for gluing points of shapes together with coincidence constraints
 */
public class ToolCoincidentAction extends AbstractToolAction {
    public static final String SMALL_ICON_PATH = "img/coincidentconstraint.svg";

    public ToolCoincidentAction() {
        super(Tool.COINCIDENT, SMALL_ICON_PATH, Localization.getString("platform.designer.coincident"),
                Localization.getString("designer.constraint.coincident.tooltip"));
    }
}
