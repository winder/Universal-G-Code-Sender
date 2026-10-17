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

import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.ToastService;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.util.Optional;

public class StartAction extends BaseAction {

    private static final String ICON_BASE = "icons/start.svg";
    private final BackendAPI backend;

    public StartAction() {
        super(Localization.getString("mainWindow.swing.sendButton"), Localization.getString("mainWindow.swing.sendButton"), Localization.getString("actions.category.machine"), ICON_BASE);
        setMenuVisible(true);
        setMenuOrder(200);
        backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        updateEnabled();
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent || event instanceof FileStateEvent) {
            updateEnabled();
        }
    }

    private void updateEnabled() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::updateEnabled);
            return;
        }
        enabledProperty().set(backend.canSend() || backend.isPaused());
    }

    @Override
    public void handleAction(ActionEvent event) {
        if (!backend.isPaused()) {
            Optional<String> reason = findReasonNotToSend();
            if (reason.isPresent()) {
                ToastService.getInstance().showError(reason.get());
                return;
            }
        }

        try {
            if (backend.isPaused()) {
                backend.pauseResume();
            } else {
                backend.send();
            }

            // Disable the button until we get a new UGS event to prevent the button to be double clicked
            enabledProperty().set(false);
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }

    /**
     * What runs on the machine should always be what is on disk, so a workspace with unsaved
     * changes or a design whose G-code is still being regenerated is not sent. The action stays
     * enabled so the user learns why instead of facing a greyed out button.
     */
    private Optional<String> findReasonNotToSend() {
        Optional<WorkspaceContext> workspace = WorkspaceManager.getInstance().getActiveWorkspace();
        if (workspace.map(WorkspaceContext::isDirty).orElse(false)) {
            return Optional.of(Localization.getString("send.unsavedChanges"));
        }

        boolean regenerating = workspace
                .filter(UgsdWorkspaceContext.class::isInstance)
                .map(UgsdWorkspaceContext.class::cast)
                .map(design -> design.gcodeBusyProperty().get())
                .orElse(false);
        if (regenerating) {
            return Optional.of(Localization.getString("send.generatingGcode"));
        }
        return Optional.empty();
    }
}
