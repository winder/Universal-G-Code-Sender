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

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.platform.surfacescanner.MeshLevelManager;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleApplyToGcodeAction extends AbstractAction implements UGSEventListener {
    public static final String ICON_BASE_ENABLED = "com/willwinder/ugs/platform/surfacescanner/icons/transform.svg";
    public static final String ICON_BASE_DISABLED = "com/willwinder/ugs/platform/surfacescanner/icons/transformoff.svg";
    private final MeshLevelManager meshLevelManager;
    private final BackendAPI backend;

    public ToggleApplyToGcodeAction(MeshLevelManager meshLevelManager) {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        this.meshLevelManager = meshLevelManager;
        putValue(Action.SELECTED_KEY, backend.getSettings().getAutoLevelSettings().getApplyToGcode());
        updateState();
        setEnabled(isEnabled());
    }

    @Override
    public boolean isEnabled() {
        return (backend.isConnected() && backend.isIdle()) || !backend.isConnected();
    }

    private void updateState() {
        String title = Localization.getString("autoleveler.panel.apply");
        String icon = backend.getSettings().getAutoLevelSettings().getApplyToGcode() ? ICON_BASE_ENABLED : ICON_BASE_DISABLED;

        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", icon);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(icon, false));
        putValue(Action.SELECTED_KEY, backend.getSettings().getAutoLevelSettings().getApplyToGcode());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isActive = (boolean) getValue(Action.SELECTED_KEY);
        backend.getSettings().getAutoLevelSettings().setApplyToGcode(isActive);
        meshLevelManager.update();
        updateState();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            updateState();
        }

        if (evt instanceof ControllerStatusEvent) {
            setEnabled(isEnabled());
        }
    }
}
