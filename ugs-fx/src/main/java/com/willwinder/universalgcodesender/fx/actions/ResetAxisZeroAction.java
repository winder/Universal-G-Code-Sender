package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.event.ActionEvent;

public class ResetAxisZeroAction extends BaseAction {

    private static final String ICON_BASE = "icons/resetzero.svg";
    private final BackendAPI backend;
    private final Axis axis;

    public ResetAxisZeroAction(Axis axis) {
        super(Localization.getString("mainWindow.swing.resetCoordinatesButton"), Localization.getString("mainWindow.swing.resetCoordinatesButton"), Localization.getString("actions.category.machine"), ICON_BASE);
        backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        this.axis = axis;
        enabledProperty().set(canResetZero());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            enabledProperty().set(canResetZero());
        }
    }

    private boolean canResetZero() {
        return backend != null && backend.isIdle();
    }

    @Override
    public void handleAction(ActionEvent event) {
        try {
            backend.resetCoordinateToZero(axis);
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
