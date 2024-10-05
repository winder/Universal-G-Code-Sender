/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.actions;

import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.util.ImageUtilities;

@ActionID(
        category = LocalizingService.CATEGORY_VISUALIZER,
        id = ToggleGridFeatureAction.ID
)
@ActionRegistration(
        iconBase = ToggleGridFeatureAction.SMALL_ICON_PATH,
        displayName = "Toggle grid",
        checkedOn = @ActionState(type = BackendAPI.class, useActionInstance = true)
)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_VISUALIZER,
                position = 1075)
})
public class ToggleGridFeatureAction extends ToggleFeatureAction {
    public static final String SMALL_ICON_PATH = "icons/grid.svg";
    public static final String LARGE_ICON_PATH = "icons/grid24.svg";
    public static final String ID = "com.willwinder.ugs.nbm.visualizer.actions.ToggleGridFeatureAction";


    public ToggleGridFeatureAction() {
        super(VisualizerOptions.VISUALIZER_OPTION_GRID, VisualizerOptions.VISUALIZER_OPTION_GRID_DESC);
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
    }
}
