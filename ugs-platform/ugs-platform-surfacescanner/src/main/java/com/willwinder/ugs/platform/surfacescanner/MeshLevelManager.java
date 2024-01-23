/*
    Copyright 2023-2024 Will Winder

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
package com.willwinder.ugs.platform.surfacescanner;

import static com.willwinder.ugs.platform.surfacescanner.Utils.createCommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessorList;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.util.Exceptions;

/**
 * A mesh leveler manager that handles all required command processors
 * to be able to do a mesh leveling
 */
public class MeshLevelManager {
    private final SurfaceScanner surfaceScanner;
    private final BackendAPI backend;
    private CommandProcessorList commandProcessorList;

    public MeshLevelManager(SurfaceScanner surfaceScanner, BackendAPI backend) {
        this.surfaceScanner = surfaceScanner;
        this.backend = backend;
    }

    public void update() {
        AutoLevelSettings autoLevelSettings = backend.getSettings().getAutoLevelSettings();
        try {
            clear();

            if (!surfaceScanner.isValid() || !autoLevelSettings.getApplyToGcode()) {
                return;
            }

            commandProcessorList = createCommandProcessor(autoLevelSettings, surfaceScanner);
            backend.applyCommandProcessor(commandProcessorList);
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    public void clear() {
        // Remove previously active command processors
        if (commandProcessorList != null) {
            try {
                backend.removeCommandProcessor(commandProcessorList);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            commandProcessorList = null;
        }
    }
}
