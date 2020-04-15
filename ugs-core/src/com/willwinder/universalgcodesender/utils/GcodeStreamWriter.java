/*
 * GcodeStreamReader.java
 *
 * Reads a 'GcodeStream' file containing command processing information, actual
 * command to send and other metadata like total number of commands.
 *
 * Created on Jan 7, 2016
 */
/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

/**
 * Writes a "GcodeStream" file in a machine readable format.
 * @author wwinder
 */
public class GcodeStreamWriter extends GcodeStream implements IGcodeWriter {
    private final File file;
    private final PrintWriter fileWriter;
    private final String metadataReservedSize = "                                                  ";

    private Integer lineCount = 0;

    public GcodeStreamWriter(File f) throws FileNotFoundException {
        file = f;
        fileWriter = new PrintWriter(f);
        // 50 bytes at the beginning of the file to store metadata
        fileWriter.append(metadataReservedSize);
        fileWriter.append("\n");
    }

    private String getString(String str) {
        return str == null ? "" : str.trim();
    }

    @Override
    public String getCanonicalPath() throws IOException {
        return file.getCanonicalPath();
    }

    public void addLine(GcodeCommand command) {
        lineCount++;
        String sep = "";
        for (int i = 0; i < NUM_COLUMNS; i++) {
            fileWriter.append(sep);
            switch (i) {
                case COL_ORIGINAL_COMMAND:
                    fileWriter.append(command.getOriginalCommandString());
                    break;
                case COL_PROCESSED_COMMAND:
                    fileWriter.append(command.getCommandString());
                    break;
                case COL_COMMENT:
                    fileWriter.append(command.getComment());
                    break;
                case COL_COMMAND_NUMBER:
                    fileWriter.append(Integer.toString(command.getCommandNumber()));
                    break;
                default:
                    break;
            }
            sep = separator;
        }
        fileWriter.append("\n");
    }

    public void addLine(String original, String processed, String comment, int commandNumber) {
        if (    (original != null && original.trim().contains("\n")) || 
                (processed != null && processed.trim().contains("\n")) ||
                (comment != null && comment.trim().contains("\n"))) {
            throw new IllegalArgumentException("Cannot include newlines in gcode stream.");
        }

        lineCount++;
        String sep = "";
        for (int i = 0; i < NUM_COLUMNS; i++) {
            fileWriter.append(sep);
            switch (i) {
                case COL_ORIGINAL_COMMAND:
                    fileWriter.append(getString(original));
                    break;
                case COL_PROCESSED_COMMAND:
                    fileWriter.append(getString(processed));
                    break;
                case COL_COMMENT:
                    fileWriter.append(getString(comment));
                    break;
                case COL_COMMAND_NUMBER:
                    fileWriter.append(Integer.toString(commandNumber));
                    break;
                default:
                    break;
            }
            sep = separator;
        }
        fileWriter.append("\n");
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
        try (RandomAccessFile raw = new RandomAccessFile(file, "rw")) {
            raw.seek(0);
            String metadata = "gsw_meta:" + lineCount.toString();
            if (metadata.length() > metadataReservedSize.length()) {
                throw new IOException("Too many lines to write metadata for GcodeStreamWriter!");
            }
            raw.write(metadata.getBytes(), 0, metadata.length());
        }
    }
}
