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
package com.willwinder.ugs.nbp.designer.platform;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.actions.OpenAction;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.ImageUtilities;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

import static com.willwinder.ugs.nbp.designer.platform.UgsDataObject.ATTRIBUTE_TEMPORARY;

/**
 * @author Joacim Breiler
 */
public class UgsSaveCookie implements Icon, SaveCookie {
    private final UgsDataObject dataObject;
    private final Icon icon = ImageUtilities.loadImageIcon("img/new.svg", false);

    public UgsSaveCookie(UgsDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public void save() {
        boolean isTemporary = dataObject.getPrimaryFile().getAttribute(ATTRIBUTE_TEMPORARY) != null;
        if (isTemporary) {
            displaySaveAsDialog();
        } else {
            saveDesign(new File(dataObject.getPrimaryFile().getPath()));
        }
    }

    private void displaySaveAsDialog() {
        FileChooserBuilder fcb = new FileChooserBuilder(OpenAction.class);
        fcb.setFileFilter(OpenAction.DESIGN_FILE_FILTER);
        JFileChooser fileChooser = fcb.createFileChooser();
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), dataObject.getPrimaryFile().getName()));

        if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Removing the old file
        File file = new File(StringUtils.appendIfMissing(fileChooser.getSelectedFile().getAbsolutePath(), ".ugsd"));
        if (file.exists()) {
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to overwrite the file " + file.getName(), "Overwrite existing file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                file.delete();
            } else {
                return;
            }
        }

        renameAndSaveDataObject(file);
    }

    private void renameAndSaveDataObject(File file) {
        try {
            FileObject directory = FileUtil.toFileObject(file.getParentFile());
            dataObject.move(DataFolder.findFolder(directory));
            dataObject.rename(Files.getNameWithoutExtension(file.getName()));
            saveDesign(new File(dataObject.getPrimaryFile().getPath()));
        } catch (IOException e) {
            GUIHelpers.displayErrorDialog("Could not save file", true);
        }
    }

    private void saveDesign(File file) {
        try {
            UgsDesignWriter writer = new UgsDesignWriter();
            writer.write(file, ControllerFactory.getController());
            dataObject.getPrimaryFile().setAttribute(ATTRIBUTE_TEMPORARY, null);
            dataObject.setModified(false);
            PlatformUtils.exportAndLoadGcode(dataObject.getName());
        } catch (IOException e) {
            GUIHelpers.displayErrorDialog("Could not save file", true);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
