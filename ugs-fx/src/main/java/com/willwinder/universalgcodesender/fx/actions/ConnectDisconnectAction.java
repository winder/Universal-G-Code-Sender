package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.fx.stage.ConnectStage;
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

    ConnectDisconnectAction() {
        super(LocalizingService.ConnectDisconnectTitleConnect, ICON_BASE_DISCONNECT);
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
    public void handle(ActionEvent event) {
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
