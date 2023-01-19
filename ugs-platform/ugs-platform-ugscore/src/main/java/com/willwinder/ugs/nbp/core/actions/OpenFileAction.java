/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.EditorUtils;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * A generic action for opening a file
 *
 * @author Joacim Breiler
 */
public class OpenFileAction extends AbstractAction {
    private final File selectedFile;

    public OpenFileAction(File selectedFile) {
        this.selectedFile = selectedFile;
        putValue(NAME, selectedFile.getName());
        putValue(Action.SHORT_DESCRIPTION, selectedFile.getAbsolutePath());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!EditorUtils.closeOpenEditors()) {
            return;
        }

        try {
            BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            backend.getSettings().setLastOpenedFilename(selectedFile.getAbsolutePath());
            OpenCookie c = DataObject.find(FileUtil.toFileObject(selectedFile))
                    .getLookup()
                    .lookup(OpenCookie.class);
            if (c != null) c.open();
        } catch (DataObjectNotFoundException e) {
            GUIHelpers.displayErrorDialog("Could not open file " + selectedFile.getAbsolutePath());
        }
    }
}
