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
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import java.awt.event.ActionEvent;
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "SnapToGridOneAction")
@ActionRegistration(
        iconBase = SnapToGridOneAction.SMALL_ICON_PATH,
        displayName = "Snap to 1mm grid",
        lazy = false)
public class SnapToGridOneAction extends AbstractDesignAction {
    public static final String SMALL_ICON_PATH = "img/snap_to_grid_1mm_16.png";
    public static final String LARGE_ICON_PATH = "img/snap_to_grid_1mm_24.png";

    public SnapToGridOneAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Snap to 1mm grid");
        putValue(NAME, "Snap to 1mm grid");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = ControllerFactory.getController();
        controller.getDrawing().snapToGridUpdated(1.0);
    }
}

