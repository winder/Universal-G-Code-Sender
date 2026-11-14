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
package com.willwinder.ugs.nbp.designer.platform.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

/**
 * Selects the tool for adding distance constraints between the points of shapes.
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "ToolConstraintAction")
@ActionRegistration(
        iconBase = ToolConstraintAction.SMALL_ICON_PATH,
        displayName = "Distance",
        lazy = false)
public class ToolConstraintAction extends com.willwinder.ugs.designer.actions.ToolConstraintAction {

}
