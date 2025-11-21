/*
    Copyright 2025 Damian Nikodem

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

package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.uielements.FileOpenDialog;
import com.willwinder.universalgcodesender.uielements.FileSaveDialog;
import org.apache.commons.lang3.StringUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * FileFilter which is limited to firmware settings files.
 */
public class YamlSettingsFileTypeFilter extends FileFilter {
    private static FileOpenDialog fileOpenDialog;
    private static FileSaveDialog fileSaveDialog;
    
    public static FileOpenDialog getSettingsFileChooser() {
        if (fileOpenDialog == null) {
            fileOpenDialog = new FileOpenDialog("");
            fileOpenDialog.setFileFilter(new YamlSettingsFileTypeFilter());
        }
        return fileOpenDialog;
    }


    public static FileSaveDialog getSettingsFileSaveChooser() {
        if (fileSaveDialog == null) {
            fileSaveDialog = new FileSaveDialog("");
            fileSaveDialog.setFileFilter(new YamlSettingsFileTypeFilter());
        }
        return fileSaveDialog;
    }
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        return StringUtils.endsWith(f.getName(), ".yaml");
    }

    @Override
    public String getDescription() {
        return "FluidNC Yaml settings File";
    }

}
