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
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;


public class OpenFileAction extends BaseAction {

    private static final String ICON_BASE = "icons/open.svg";
    private final BackendAPI backend;

    public OpenFileAction() {
        super(LocalizingService.OpenTitle, LocalizingService.OpenTitle, ICON_BASE);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
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
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File(backend.getSettings().getLastWorkingDirectory()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gcode files", "*.nc", "*.txt", "*.gcode"));

        Window window = ((Node) event.getSource()).getScene().getWindow();

        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            try {
                backend.setGcodeFile(selectedFile);
                backend.getSettings().setLastWorkingDirectory(selectedFile.getParent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
