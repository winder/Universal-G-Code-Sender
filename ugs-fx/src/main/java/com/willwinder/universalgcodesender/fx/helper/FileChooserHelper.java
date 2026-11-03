/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.helper;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Helpers for configuring file and directory choosers.
 */
public final class FileChooserHelper {

    private FileChooserHelper() {
    }

    /**
     * Resolves a directory that is safe to use as the initial directory of a file chooser. JavaFX
     * throws an exception and refuses to show the dialog if the initial directory does not exist,
     * which happens when the remembered directory has been moved or deleted.
     *
     * @param directory the preferred directory, may be null
     * @return the directory if it exists, otherwise the user home directory if it exists, otherwise null
     */
    public static File resolveInitialDirectory(String directory) {
        if (StringUtils.isNotBlank(directory)) {
            File file = new File(directory);
            if (file.isDirectory()) {
                return file;
            }
        }

        File userHome = new File(System.getProperty("user.home"));
        return userHome.isDirectory() ? userHome : null;
    }
}
