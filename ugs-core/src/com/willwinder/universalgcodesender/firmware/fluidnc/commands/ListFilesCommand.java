/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.model.File;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ListFilesCommand extends SystemCommand {

    public ListFilesCommand() {
        super("$LocalFS/List");
    }

    public List<String> getFileList() {
        List<String> files = new ArrayList<>();
        for (String line : StringUtils.split(getResponse(), "\n")) {
            if (line.startsWith("[FILE:")) {
                String filename = StringUtils.substringBetween(line, "[FILE:", "|").trim();
                if (!filename.equals(".")) {
                    files.add("/localfs/" + filename);
                }
            }
        }
        return files;
    }

    public List<File> getFiles() {
        List<File> files = new ArrayList<>();
        for (String line : StringUtils.split(getResponse(), "\n")) {
            if (line.startsWith("[FILE:")) {
                String filename = StringUtils.substringBetween(line, "[FILE:", "|").trim();
                String size = StringUtils.substringBetween(line, "SIZE:", "]");

                if (!filename.equals(".")) {
                    long fileSize = StringUtils.isNumeric(size) ? Long.parseLong(size) : 0;
                    files.add(new File(filename, "/localfs/" + filename, fileSize));
                }
            }
        }
        return files;
    }
}
