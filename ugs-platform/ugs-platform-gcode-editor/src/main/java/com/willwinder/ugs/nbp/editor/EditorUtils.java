/*
    Copyright 2021 Will Winder

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
import org.openide.filesystems.FileObject;

import java.io.File;

/**
 * @author Joacim Breiler
 */
public class EditorUtils {
    public static void openFile(FileObject pf) {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        try {
            if (backend.getGcodeFile() == null || !backend.getGcodeFile().getAbsolutePath().equalsIgnoreCase(pf.getPath())) {
                backend.setGcodeFile(new File((pf.getPath())));
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }
}
