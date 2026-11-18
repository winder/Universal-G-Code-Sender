/*
    Copyright 2023-2024 Will Winder

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

import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class TogglePreviewAction extends AbstractAction {
    public static final String ICON_BASE_ENABLED = "com/willwinder/ugs/platform/surfacescanner/icons/eye.svg";
    public static final String ICON_BASE_DISABLED = "com/willwinder/ugs/platform/surfacescanner/icons/eyeoff.svg";

    private final Renderable renderable;

    public TogglePreviewAction(Renderable renderable) {
        this.renderable = renderable;
        renderable.addListener(this::updateState);
        updateState();
    }

    private void updateState() {
        SwingUtilities.invokeLater(() -> {
            String title = Localization.getString("autoleveler.panel.visible");
            String icon = renderable.isEnabled() ? ICON_BASE_ENABLED : ICON_BASE_DISABLED;
            putValue(NAME, title);
            putValue("menuText", title);
            putValue(Action.SHORT_DESCRIPTION, title);
            putValue("iconBase", icon);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(icon, false));
            putValue(Action.SELECTED_KEY, renderable.isEnabled());
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(() -> renderable.setEnabled(!renderable.isEnabled()));
    }
}
