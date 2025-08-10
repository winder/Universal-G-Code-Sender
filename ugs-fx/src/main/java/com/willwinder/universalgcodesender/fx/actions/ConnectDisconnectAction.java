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

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.fx.stage.ConnectStage;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

public class ConnectDisconnectAction extends BaseAction {
    public static final String ICON_BASE = "resources/icons/connect.svg";
    public static final String ICON_BASE_DISCONNECT = "resources/icons/disconnect.svg";

    private final BackendAPI backend;

    public ConnectDisconnectAction() {
        super(LocalizingService.ConnectDisconnectTitleConnect, LocalizingService.ConnectDisconnectTitleConnect, Localization.getString("actions.category.machine"), ICON_BASE_DISCONNECT);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(true);
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            Platform.runLater(this::updateIconAndText);
        }
    }

    private void updateIconAndText() {
        titleProperty().set(LocalizingService.ConnectDisconnectActionTitle);
        if (backend.getControllerState() == ControllerState.DISCONNECTED) {
            titleProperty().set(LocalizingService.ConnectDisconnectTitleConnect);
            iconProperty().set(ICON_BASE_DISCONNECT);
        } else {
            titleProperty().set(LocalizingService.ConnectDisconnectTitleDisconnect);
            iconProperty().set(ICON_BASE);
        }
    }

    @Override
    public void handleAction(ActionEvent event) {
        if (backend.getControllerState() == ControllerState.DISCONNECTED) {
            // Connect modal
            Window window = ((Node) event.getSource()).getScene().getWindow();
            ConnectStage connectModal = new ConnectStage(window);
            connectModal.showAndWait();
        } else {
            try {
                backend.disconnect();
            } catch (Exception e) {
                String message = e.getMessage();
                if (StringUtils.isEmpty(message)) {
                    message = "Got an unknown error while disconnecting, see log for more details";
                }
                GUIHelpers.displayErrorDialog(message);
            }
        }
    }
}
