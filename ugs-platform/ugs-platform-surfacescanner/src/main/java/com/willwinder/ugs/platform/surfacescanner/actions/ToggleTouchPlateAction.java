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
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;

/**
 * Toggles the translucent touch-plate slab in the AutoLeveler visualizer preview.
 * The plate is only drawn when a non-zero touch plate thickness is configured.
 */
public class ToggleTouchPlateAction extends AbstractAction implements UGSEventListener {
    public static final String ICON_BASE_ENABLED = "com/willwinder/ugs/platform/surfacescanner/icons/eye.svg";
    public static final String ICON_BASE_DISABLED = "com/willwinder/ugs/platform/surfacescanner/icons/eyeoff.svg";

    private final transient AutoLevelPreview autoLevelPreview;
    private final transient BackendAPI backend;

    public ToggleTouchPlateAction(AutoLevelPreview autoLevelPreview) {
        this.autoLevelPreview = autoLevelPreview;
        this.backend = LookupService.lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        updateState();
    }

    @Override
    public boolean isEnabled() {
        return backend.getSettings().getAutoLevelSettings().getTouchPlateThickness() > 0;
    }

    private void updateState() {
        SwingUtilities.invokeLater(() -> {
            String title = Localization.getString("autoleveler.panel.touch-plate-visible");
            String tooltip = Localization.getString("autoleveler.panel.touch-plate-visible.tooltip");
            String icon = autoLevelPreview.isTouchPlateVisible() ? ICON_BASE_ENABLED : ICON_BASE_DISABLED;
            putValue(NAME, title);
            putValue("menuText", title);
            putValue(Action.SHORT_DESCRIPTION, tooltip);
            putValue("iconBase", icon);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(icon, false));
            putValue(Action.SELECTED_KEY, autoLevelPreview.isTouchPlateVisible());
            setEnabled(isEnabled());
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        autoLevelPreview.setTouchPlateVisible(!autoLevelPreview.isTouchPlateVisible());
        updateState();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            updateState();
        }
    }
}
