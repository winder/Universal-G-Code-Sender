package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.event.ActionEvent;

public class StartAction extends BaseAction {

    private static final String ICON_BASE = "icons/start.svg";
    private final BackendAPI backend;

    public StartAction() {
        super(Localization.getString("mainWindow.swing.sendButton"), ICON_BASE);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(backend.canSend() || backend.isPaused());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent || event instanceof FileStateEvent) {
            enabledProperty().set(backend.canSend() || backend.isPaused());
        }
    }

    @Override
    public void handle(ActionEvent event) {
        try {
            if (backend.isPaused()) {
                backend.pauseResume();
            } else {
                backend.send();
            }
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
