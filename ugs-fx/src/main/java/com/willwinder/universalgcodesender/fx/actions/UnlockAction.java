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

import com.willwinder.universalgcodesender.fx.helper.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.event.ActionEvent;

public class UnlockAction extends BaseAction {

    private static final String ICON_BASE = "icons/lock.svg";
    private final BackendAPI backend;

    public UnlockAction() {
        super(Localization.getString("mainWindow.swing.alarmLock"), Localization.getString("platform.actions.unlock.tooltip"), Localization.getString("actions.category.machine"), ICON_BASE);
        backend = CentralLookup.lookup(BackendAPI.class).orElseThrow();
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(canUnlock());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            enabledProperty().set(canUnlock());
        }
    }

    private boolean canUnlock() {
        return backend != null && backend.getControllerState() == ControllerState.ALARM;
    }

    @Override
    public void handleAction(ActionEvent event) {
        try {
            backend.killAlarmLock();
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
