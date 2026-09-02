/*
    Copyright 2026 Will Winder

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

import com.willwinder.ugs.platform.surfacescanner.renderable.AutoLevelPreview;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class ToggleTouchPlateAction extends AbstractAction {
    public static final String ICON_BASE_ENABLED = "com/willwinder/ugs/platform/surfacescanner/icons/eye.svg";
    public static final String ICON_BASE_DISABLED = "com/willwinder/ugs/platform/surfacescanner/icons/eyeoff.svg";

    private final transient AutoLevelPreview autoLevelPreview;

    public ToggleTouchPlateAction(AutoLevelPreview autoLevelPreview) {
        this.autoLevelPreview = autoLevelPreview;
        updateState();
    }

    private void updateState() {
        String title = Localization.getString("autoleveler.panel.touch-plate-visible");
        String icon = autoLevelPreview.isTouchPlateVisible() ? ICON_BASE_ENABLED : ICON_BASE_DISABLED;

        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", icon);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(icon, false));
        putValue(Action.SELECTED_KEY, autoLevelPreview.isTouchPlateVisible());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        autoLevelPreview.setTouchPlateVisible((boolean) getValue(Action.SELECTED_KEY));
        updateState();
    }
}
