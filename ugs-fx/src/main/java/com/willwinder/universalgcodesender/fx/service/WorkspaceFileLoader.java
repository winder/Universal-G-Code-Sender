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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.services.FileLoader;
import javafx.application.Platform;

import java.io.File;

/**
 * A {@link FileLoader} for the JavaFX edition that opens files as a workspace instead of loading the
 * gcode directly into the backend. The workspace is opened on the JavaFX application thread since it
 * updates the UI.
 *
 * @author Joacim Breiler
 */
public class WorkspaceFileLoader implements FileLoader {

    @Override
    public void openFile(File file) {
        Platform.runLater(() -> WorkspaceManager.getInstance().openWorkspace(file));
    }
}
