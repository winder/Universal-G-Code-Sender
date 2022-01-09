/*
    Copyright 2015-2021 Will Winder

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

import com.willwinder.ugs.nbp.core.services.FileFilterService;
import com.willwinder.ugs.nbp.lib.EditorUtils;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.OpenCategory,
        id = LocalizingService.OpenActionId)
@ActionRegistration(
        iconBase = OpenAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.OpenTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.OpenWindowPath,
                position = 10),
        @ActionReference(
                path = "Toolbars/File",
                position = 10),
        @ActionReference(
                path = "Shortcuts",
                name = "M-O")
})
public final class OpenAction extends AbstractAction {

    public static final String ICON_BASE = "resources/icons/open.svg";
    private transient final FileFilterService fileFilterService;
    private transient final BackendAPI backend;
    private final JFileChooser fileChooser;

    public OpenAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.fileFilterService = Lookup.getDefault().lookup(FileFilterService.class);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.OpenTitle);
        putValue(NAME, LocalizingService.OpenTitle);

        fileChooser = createFileChooser();
    }

    @Override
    public boolean isEnabled() {
        return backend != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Fetches all available file formats that UGS can open
        fileFilterService.getFileFilters().forEach(fileChooser::addChoosableFileFilter);

        int returnVal = fileChooser.showOpenDialog(new JFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                if (EditorUtils.closeOpenEditors()) {
                    backend.getSettings().setLastOpenedFilename(fileChooser.getSelectedFile().getAbsolutePath());
                    DataObject.find(FileUtil.toFileObject(fileChooser.getSelectedFile()))
                            .getLookup()
                            .lookup(OpenCookie.class)
                            .open();
                }
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private JFileChooser createFileChooser() {
        String sourceDir = backend.getSettings().getLastOpenedFilename();

        JFileChooser fileChooser = new JFileChooser(sourceDir);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setAcceptAllFileFilterUsed(true);
        return fileChooser;
    }
}
