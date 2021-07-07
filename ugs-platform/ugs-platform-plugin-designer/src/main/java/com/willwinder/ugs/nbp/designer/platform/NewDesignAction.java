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

import com.willwinder.ugs.nbp.designer.actions.OpenAction;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

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
                position = 9),
        @ActionReference(
                path = "Shortcuts",
                name = "M-O")
})
public final class NewDesignAction extends AbstractAction {

    public static final String SMALL_ICON_PATH = "img/new.svg";
    public static final String LARGE_ICON_PATH = "img/new32.svg";
    private BackendAPI backend;

    public NewDesignAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "New design");
        putValue(NAME, "New design");
    }

    @Override
    public boolean isEnabled() {
        return backend != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            FileChooserBuilder fcb = new FileChooserBuilder(OpenAction.class);
            fcb.setFileFilter(OpenAction.DESIGN_FILE_FILTER);

            JFileChooser fileChooser = fcb.createFileChooser();
            fileChooser.setFileHidingEnabled(true);
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = new File(StringUtils.appendIfMissing(fileChooser.getSelectedFile().getAbsolutePath(), ".ugsd"));
                FileObject dir = FileUtil.toFileObject(fileChooser.getSelectedFile().getParentFile());

                // Removing the old file
                if (file.exists()) {
                    if(JOptionPane.showConfirmDialog(SwingUtilities.getRoot((Component)e.getSource()), "Are you sure you want to overwrite the file " + file.getName(), "Overwrite existing file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        file.delete();
                    } else {
                        return;
                    }
                }

                FileObject fileObject = dir.createData(file.getName());
                DataObject.find(fileObject).getLookup().lookup(OpenCookie.class).open();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
