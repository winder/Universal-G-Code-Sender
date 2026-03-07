/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.fx.helper.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.event.ActionEvent;

public class HomingAction extends BaseAction {

    private static final String ICON_BASE = "icons/home.svg";
    private final BackendAPI backend;

    public HomingAction() {
        super(Localization.getString("mainWindow.swing.homeMachine"), Localization.getString("mainWindow.swing.homeMachine"), Localization.getString("actions.category.machine"), ICON_BASE);
        backend = CentralLookup.lookup(BackendAPI.class).orElseThrow();
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(canHome());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent || event instanceof FirmwareSettingEvent) {
            enabledProperty().set(canHome());
        }
    }

    private boolean canHome() {
        return (backend.getControllerState() == ControllerState.IDLE || backend.getControllerState() == ControllerState.ALARM) &&
                isHomingEnabled();
    }

    @Override
    public void handleAction(ActionEvent event) {
        try {
            backend.performHomingCycle();
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }

    private boolean isHomingEnabled() {
        boolean isHomingEnabled = false;
        try {
            isHomingEnabled = backend.getController() != null &&
                    backend.getController().getFirmwareSettings() != null &&
                    backend.getController().getFirmwareSettings().isHomingEnabled();
        } catch (FirmwareSettingsException ignored) {
            // Never mind
        }
        return isHomingEnabled;
    }
}
