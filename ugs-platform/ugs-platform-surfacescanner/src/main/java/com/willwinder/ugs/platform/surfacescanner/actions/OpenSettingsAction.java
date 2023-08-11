/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.surfacescanner.actions;

import com.willwinder.universalgcodesender.i18n.Localization;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenSettingsAction extends AbstractAction {
    public static final String ICON_BASE = "com/willwinder/ugs/platform/surfacescanner/icons/settings.svg";

    public static final String SETTINGS_KEY = "UGS/autoleveler";

    public OpenSettingsAction() {
        String title = Localization.getString("mainWindow.swing.settingsMenu");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(Action.SELECTED_KEY, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open(SETTINGS_KEY);
    }
}
