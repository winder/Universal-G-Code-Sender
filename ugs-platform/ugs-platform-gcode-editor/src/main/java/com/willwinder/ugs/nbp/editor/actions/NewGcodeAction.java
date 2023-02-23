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
package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.lib.EditorUtils;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@ActionID(
        category = LocalizingService.OpenCategory,
        id = "com.willwinder.ugs.nbp.editor.actions.NewGcodeAction")
@ActionRegistration(
        iconBase = "icons/new.svg",
        displayName = "New gcode",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.OpenWindowPath,
                position = 8),
        @ActionReference(
                path = "Toolbars/File",
                position = 8)
})
public final class NewGcodeAction extends AbstractAction {

    public static final String SMALL_ICON_PATH = "icons/new.svg";
    public static final String LARGE_ICON_PATH = "icons/new24.svg";

    public NewGcodeAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "New gcode file");
        putValue(NAME, "New gcode file");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!EditorUtils.closeOpenEditors()) {
            return;
        }

        try {
            File file = chooseFile();
            if (!file.createNewFile()) {
                throw new IOException("Could not create temporary file " + file);
            }
            writeExampleToFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);
            DataObject.find(fileObject).getLookup().lookup(OpenCookie.class).open();
        } catch (IOException ex) {
            GUIHelpers.displayErrorDialog("Could not create temporary file");
        }
    }

    private File chooseFile() throws IOException {
        FileChooserBuilder fcb = new FileChooserBuilder(NewGcodeAction.class);
        fcb.setTitle("Create Gcode file");
        fcb.setFileFilter(new GcodeFileTypeFilter());
        JFileChooser fileChooser = fcb.createFileChooser();
        fileChooser.setFileHidingEnabled(true);

        if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            throw new IOException();
        }

        // Removing the old file
        File file = new File(StringUtils.appendIfMissing(fileChooser.getSelectedFile().getAbsolutePath(), ".gcode"));
        if (file.exists()) {
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to overwrite the file " + file.getName(), "Overwrite existing file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                file.delete();
            } else {
                throw new IOException();
            }
        }
        return file;
    }

    private void writeExampleToFile(File file) throws IOException {
        try (InputStream exampleStream = getClass().getResourceAsStream("/com/willwinder/ugs/nbp/editor/example.gcode")) {
            if (exampleStream == null) {
                return;
            }
            FileUtils.write(file, IOUtils.toString(exampleStream, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }
    }
}
