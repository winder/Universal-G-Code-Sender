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
package com.willwinder.ugs.nbp.filebrowser;

import com.willwinder.universalgcodesender.File;
import com.willwinder.universalgcodesender.IFileService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBrowserDialogTest {
    public static void main(String[] args) {
        List<File> fileList = new ArrayList<>();
        fileList.add(new File("config.yaml", "/localfs/config.yaml", 100302L));
        fileList.add(new File("index.html.gz", "/localfs/index.html.gz", 8124778L));

        IFileService fileService = new IFileService() {
            @Override
            public byte[] downloadFile(File file) throws IOException {
                return fileList.stream()
                        .filter(f -> f.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath()))
                        .findFirst()
                        .orElseThrow(() -> new IOException("File not found"))
                        .getName()
                        .getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public void uploadFile(String filename, byte[] data) throws IOException {
                fileList.add(new File(filename, filename, data.length));
            }

            @Override
            public void deleteFile(File file) throws IOException {
                fileList.removeIf(f -> f.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath()));
            }

            @Override
            public List<File> getFiles() throws IOException {
                return fileList;
            }
        };

        FileBrowserDialog fileBrowserDialog = new FileBrowserDialog(fileService);
        fileBrowserDialog.showDialog();
    }
}
