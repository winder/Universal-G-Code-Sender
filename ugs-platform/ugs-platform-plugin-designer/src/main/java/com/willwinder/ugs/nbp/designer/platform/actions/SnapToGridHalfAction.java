/*
 * Copyright (C) 2025 Damian Nikodem
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.platform.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "SnapToGridHalfAction")
@ActionRegistration(
        iconBase = SnapToGridHalfAction.SMALL_ICON_PATH,
        displayName = "Snap to 0.5mm grid",
        lazy = false)
public class SnapToGridHalfAction extends com.willwinder.ugs.nbp.designer.actions.SnapToGridHalfAction {

}

