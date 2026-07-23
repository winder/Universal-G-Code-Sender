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

import com.google.common.io.Files;
import com.willwinder.ugs.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveAction extends BaseAction {

    private static final Logger LOGGER = Logger.getLogger(SaveAction.class.getName());
    private static final String ICON_BASE = "icons/save.svg";

    public SaveAction() {
        super(Localization.getString("platform.menu.save"), Localization.getString("platform.menu.save"), Localization.getString("actions.category.file"), ICON_BASE);
        setMenuVisible(true);
        setMenuOrder(120);
        setDefaultShortcut("SHORTCUT+S");
        enabledProperty().set(false);

        WorkspaceManager.getInstance().addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                updateEnabled(workspace, workspace.isDirty());
            }

            @Override
            public void onWorkspaceClosed() {
                enabledProperty().set(false);
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
                updateEnabled(workspace, dirty);
            }
        });
    }

    private void updateEnabled(WorkspaceContext workspace, boolean dirty) {
        boolean enabled = workspace instanceof UgsdWorkspaceContext && dirty;
        enabledProperty().set(enabled);
    }

    @Override
    public void handleAction(ActionEvent event) {
        WorkspaceContext workspace = WorkspaceManager.getInstance().getActiveWorkspace().orElse(null);
        if (!(workspace instanceof UgsdWorkspaceContext)) {
            return;
        }

        // A design that has never been saved has no file yet - ask the user where to store it.
        if (workspace.getFile() == null) {
            File target = promptForSaveLocation(event, workspace);
            if (target == null) {
                return;
            }
            workspace.setFile(target);
        }

        try {
            Controller controller = ControllerFactory.getController();

            UgsDesignWriter designWriter = new UgsDesignWriter();
            designWriter.write(workspace.getFile(), controller);

            GcodeDesignWriter gcodeWriter = new GcodeDesignWriter();
            File tempFile = new File(Files.createTempDir(), workspace.getFile().getName() + ".gcode");
            gcodeWriter.write(tempFile, controller);

            BackendAPI backend = LookupService.lookup(BackendAPI.class);
            backend.setGcodeFile(tempFile);
            backend.getSettings().setLastWorkingDirectory(workspace.getFile().getParent());

            WorkspaceManager.getInstance().markActiveWorkspaceDirty(false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not save design", e);
        }
    }

    private File promptForSaveLocation(Event event, WorkspaceContext workspace) {
        String extension = workspace.getFileExtension();
        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Localization.getString("platform.menu.save"));
        fileChooser.setInitialDirectory(new File(backend.getSettings().getLastWorkingDirectory()));
        fileChooser.setInitialFileName("design." + extension);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("UGS design files", "*." + extension));

        File selectedFile = fileChooser.showSaveDialog(resolveWindow(event));
        if (selectedFile == null) {
            return null;
        }

        // The extension filter does not force a suffix on every platform, so make sure it is present.
        String suffix = "." + extension;
        if (!selectedFile.getName().toLowerCase().endsWith(suffix)) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + suffix);
        }
        return selectedFile;
    }

    private static Window resolveWindow(Event event) {
        if (event != null && event.getSource() instanceof Node node && node.getScene() != null) {
            return node.getScene().getWindow();
        }
        return null;
    }
}
