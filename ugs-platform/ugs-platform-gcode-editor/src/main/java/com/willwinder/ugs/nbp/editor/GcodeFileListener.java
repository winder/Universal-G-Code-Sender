/*
    Copyright 2016-2021 Will Winder

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
package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import java.io.Serializable;

/**
 * Listens to external file change events and updates it on the controller
 */
public class GcodeFileListener implements FileChangeListener, Serializable {
    private static final long serialVersionUID = 7255903502190131123L;

    @Override
    public void fileFolderCreated(FileEvent fe) {

    }

    @Override
    public void fileDataCreated(FileEvent fe) {

    }

    @Override
    public void fileChanged(FileEvent fe) {
        try {
            BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            // Do not reload the file if the machine is running.
            if (backend.isIdle() || !backend.isConnected()) {
                backend.reloadGcodeFile();
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {

    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {

    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {

    }
}
