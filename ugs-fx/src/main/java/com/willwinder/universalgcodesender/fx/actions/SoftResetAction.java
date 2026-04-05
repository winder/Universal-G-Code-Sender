/*
    Copyright 2025 Joacim Breiler

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

import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.event.ActionEvent;

public class SoftResetAction extends BaseAction {

    private static final String ICON_BASE = "icons/reset.svg";
    private final BackendAPI backend;

    public SoftResetAction() {
        super(Localization.getString("mainWindow.swing.softResetMachineControl"), Localization.getString("platform.actions.softreset.tooltip"), Localization.getString("actions.category.machine"), ICON_BASE);
        backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(canReset());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            enabledProperty().set(canReset());
        }
    }

    private boolean canReset() {
        return backend != null && (backend.getControllerState() == ControllerState.IDLE ||
                backend.getControllerState() == ControllerState.HOLD ||
                backend.getControllerState() == ControllerState.CHECK ||
                backend.getControllerState() == ControllerState.ALARM ||
                backend.getControllerState() == ControllerState.SLEEP);
    }

    @Override
    public void handleAction(ActionEvent event) {
        try {
            backend.issueSoftReset();
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
