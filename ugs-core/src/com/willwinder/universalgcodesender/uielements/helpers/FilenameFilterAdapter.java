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
package com.willwinder.universalgcodesender.uielements.helpers;

import org.apache.commons.io.filefilter.IOFileFilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A file filter adapter to be used together with apache commons
 *
 * @author Joacim Breiler
 */
public class FilenameFilterAdapter implements IOFileFilter {

    private final FileFilter fileFilter;

    public FilenameFilterAdapter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    @Override
    public boolean accept(File file) {
        return fileFilter.accept(file);
    }

    @Override
    public boolean accept(File dir, String name) {
        return fileFilter.accept(new File(dir.getAbsolutePath() + File.separatorChar + name));
    }
}
