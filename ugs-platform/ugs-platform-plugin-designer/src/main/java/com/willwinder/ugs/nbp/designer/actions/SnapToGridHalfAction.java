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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import java.awt.event.ActionEvent;

public class SnapToGridHalfAction extends AbstractDesignAction {
    public static final String SMALL_ICON_PATH = "img/snap_to_grid_5mm.svg";
    public static final String LARGE_ICON_PATH = "img/snap_to_grid_5mm_24.svg";

    public SnapToGridHalfAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));
        putValue("menuText", "Snap to 0.5mm grid");
        putValue(NAME, "Snap to 0.5mm grid");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = ControllerFactory.getController();
        controller.getDrawing().snapToGridUpdated(0.5);
    }
}

