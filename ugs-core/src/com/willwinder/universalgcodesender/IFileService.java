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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.File;

import java.io.IOException;
import java.util.List;

/**
 * A file service for handling the file system on the controller
 */
public interface IFileService {
    /**
     * Downloads a file from the controller given a filename
     *
     * @param file the file to download
     * @return the file as an byte array.
     * @throws IOException on any communication error
     */
    byte[] downloadFile(File file) throws IOException;

    /**
     * Upload a file to the controller with the given  filename
     *
     * @param filename the file name including its path to upload
     * @param data     the file as an byte array.
     * @throws IOException on any communication error
     */
    void uploadFile(String filename, byte[] data) throws IOException;

    /**
     * Deletes a file or directory
     *
     * @param file the file to delete
     * @throws IOException on any communication error
     */
    void deleteFile(File file) throws IOException;

    /**
     * Returns a list of all files on the controller
     *
     * @return a list of files
     */
    List<File> getFiles() throws IOException;
}
