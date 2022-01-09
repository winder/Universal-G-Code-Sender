/*
    Copyright 2018 Will Winder

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


import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.modules.OnStart;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

import javax.swing.*;

@ActionID(
        category = CameraResetPreset.CATEGORY,
        id = CameraResetPreset.ID
)
@ActionRegistration(
        iconBase = CameraResetPreset.SMALL_ICON_PATH,
        displayName = "--",
        lazy = false
)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_VISUALIZER,
                position = 1060)
})
public final class CameraResetPreset extends MoveCameraAction {
    public static final String SMALL_ICON_PATH = "icons/XYZ.svg";
    public static final String LARGE_ICON_PATH = "icons/XYZ24.svg";
    public static final String CATEGORY = LocalizingService.CATEGORY_VISUALIZER;
    public static final String ID = "com.willwinder.ugs.nbm.visualizer.actions.CameraResetPreset";
    public static final String NAME = Localization.getString("platform.visualizer.popup.presets.reset");

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
        public Localizer() {
            super(LocalizingService.CATEGORY_VISUALIZER, ID, NAME);
        }
    }

    public CameraResetPreset() {
        super(
                Lookup.getDefault().lookup(GcodeRenderer.class),
                ROTATION_ISOMETRIC);

        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", NAME);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, "Resets camera preset");
    }
}