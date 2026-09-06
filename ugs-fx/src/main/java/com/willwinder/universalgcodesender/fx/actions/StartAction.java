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
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.event.ActionEvent;

public class StartAction extends BaseAction {

    private static final String ICON_BASE = "icons/start.svg";
    private final BackendAPI backend;
    private final InvalidationListener regenerationListener = observable -> updateEnabled();
    private ReadOnlyBooleanProperty regenerating;

    public StartAction() {
        super(Localization.getString("mainWindow.swing.sendButton"), Localization.getString("mainWindow.swing.sendButton"), Localization.getString("actions.category.machine"), ICON_BASE);
        setMenuVisible(true);
        setMenuOrder(200);
        backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        WorkspaceManager.getInstance().addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                followRegeneration(workspace);
                updateEnabled();
            }

            @Override
            public void onWorkspaceClosed() {
                followRegeneration(null);
                updateEnabled();
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
                updateEnabled();
            }
        });
        updateEnabled();
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent || event instanceof FileStateEvent) {
            updateEnabled();
        }
    }

    /**
     * Tracks the G-code regeneration of the active design, whose busy flag decides when the
     * loaded program matches the design again.
     */
    private void followRegeneration(WorkspaceContext workspace) {
        if (regenerating != null) {
            regenerating.removeListener(regenerationListener);
        }
        regenerating = workspace instanceof UgsdWorkspaceContext design ? design.gcodeBusyProperty() : null;
        if (regenerating != null) {
            regenerating.addListener(regenerationListener);
        }
    }

    /**
     * A program can be sent when the controller is ready, the workspace has no unsaved changes
     * and no G-code is being regenerated, so what runs on the machine is always what is on disk.
     * Resuming a paused job is always possible.
     */
    private void updateEnabled() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::updateEnabled);
            return;
        }
        boolean dirty = WorkspaceManager.getInstance().getActiveWorkspace().map(WorkspaceContext::isDirty).orElse(false);
        boolean busy = regenerating != null && regenerating.get();
        enabledProperty().set((backend.canSend() && !dirty && !busy) || backend.isPaused());
    }

    @Override
    public void handleAction(ActionEvent event) {
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
}
