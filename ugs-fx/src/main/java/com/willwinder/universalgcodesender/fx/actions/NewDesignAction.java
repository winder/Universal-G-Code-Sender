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

import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.event.ActionEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a new, empty UGSD design and makes it the active workspace. The design is not written to
 * disk until it is saved for the first time, at which point the user is asked where to store it.
 */
public class NewDesignAction extends BaseAction {

    private static final Logger LOGGER = Logger.getLogger(NewDesignAction.class.getName());
    private static final String ICON_BASE = "icons/new-design.svg";

    private final BackendAPI backend;

    public NewDesignAction() {
        super("New design", "New design", ICON_BASE);
        backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(!backend.isConnected() || backend.isIdle());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent || event instanceof FileStateEvent) {
            enabledProperty().set(!backend.isConnected() || backend.isIdle());
        }
    }

    @Override
    public void handleAction(ActionEvent event) {
        try {
            UgsdWorkspaceContext workspace = new UgsdWorkspaceContext(null);
            WorkspaceManager.getInstance().setWorkspace(workspace);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not create a new design", e);
        }
    }
}
