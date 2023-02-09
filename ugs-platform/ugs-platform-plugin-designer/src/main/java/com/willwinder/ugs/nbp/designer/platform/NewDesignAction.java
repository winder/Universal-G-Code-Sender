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
package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.lib.EditorUtils;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.willwinder.ugs.nbp.designer.platform.UgsDataObject.ATTRIBUTE_TEMPORARY;

@ActionID(
        category = LocalizingService.OpenCategory,
        id = "NewDesignAction")
@ActionRegistration(
        iconBase = "img/new.svg",
        displayName = "New",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.OpenWindowPath,
                position = 9),
        @ActionReference(
                path = "Toolbars/File",
                position = 9)
})
public final class NewDesignAction extends AbstractAction {

    public static final String SMALL_ICON_PATH = "img/new.svg";
    public static final String LARGE_ICON_PATH = "img/new24.svg";

    public NewDesignAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "New design");
        putValue(NAME, "New design");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            EditorUtils.closeOpenEditors();
            File file = new File(Files.createTempDirectory("ugsd").toFile().getAbsolutePath(), "unnamed.ugsd");
            if (!file.createNewFile()) {
                throw new IOException("Could not create temporary file " + file);
            }
            FileObject fileObject = FileUtil.toFileObject(file);
            fileObject.setAttribute(ATTRIBUTE_TEMPORARY, true);
            DataObject.find(fileObject).getLookup().lookup(OpenCookie.class).open();
        } catch (IOException ex) {
            GUIHelpers.displayErrorDialog("Could not create temporary file");
        }
    }
}
