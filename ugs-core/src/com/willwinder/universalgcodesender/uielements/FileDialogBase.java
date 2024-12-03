/*
    Copyright 2024 Will Winder

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.uielements.helpers.FilenameFilterAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.filechooser.FileFilter;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.Optional;

/**
 * A generic file dialog base
 */
public class FileDialogBase extends FileDialog {
    public FileDialogBase(String directory) {
        super((Frame) null);

        File directoryFile = new File(directory);
        if (directoryFile.isFile()) {
            directory = directoryFile.getParent();
        }
        setDirectory(directory);
    }

    public void centerOn(Frame frame) {
        if (frame == null) {
            return;
        }

        pack();
        setSize(800, 600);
        validate();

        double width = getBounds().getWidth();
        double height = getBounds().getHeight();

        Rectangle rect = frame.getBounds();
        int x = (int) (rect.getCenterX() - (width / 2));
        int y = (int) (rect.getCenterY() - (height / 2));
        setLocation(new Point(x, y));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setModal(true);
        requestFocus();
    }

    /**
     * Returns the selected file or an empty optional
     *
     * @return the selected file
     */
    public Optional<File> getSelectedFile() {
        String file = getFile();
        if (StringUtils.isEmpty(file)) {
            return Optional.empty();
        }

        return Optional.of(new File(getDirectory() + File.separatorChar + getFile()));
    }

    /**
     * Sets a file filter
     *
     * @param fileFilter the file filter
     */
    public void setFileFilter(FileFilter fileFilter) {
        setFilenameFilter(new FilenameFilterAdapter(fileFilter));
    }

    /**
     * Sets the selected file
     *
     * @param file the selected file
     */
    public void setSelectedFile(File file) {
        setDirectory(file.getParent());
        setFile(file.getName());
    }
}
