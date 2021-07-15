/*
    Copyright 2016-2020 Will Winder

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
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads a 'GcodeStream' file containing command processing information, actual
 * command to send and other metadata like total number of commands.
 *
 * @author wwinder
 */
public class GcodeStreamReader extends GcodeStream implements IGcodeStreamReader {
    private BufferedReader reader;
    private int numRows = 0;
    private int numRowsRemaining = 0;

    public static class NotGcodeStreamFile extends Exception {}

    public GcodeStreamReader(BufferedReader reader) throws NotGcodeStreamFile {
        this.reader = reader;
        
        try {
            String line = reader.readLine();
            if (StringUtils.isEmpty(line)) {
                return;
            }

            String metadata = line.trim();
            if (!metadata.startsWith(super.metaPrefix)) {
                throw new NotGcodeStreamFile();
            }

            metadata = metadata.substring(super.metaPrefix.length());
            numRows = Integer.parseInt(metadata);
            numRowsRemaining = numRows;
        } catch (IOException | NumberFormatException e) {
            throw new NotGcodeStreamFile();
        }
    }

    public GcodeStreamReader(File f) throws NotGcodeStreamFile, FileNotFoundException {
        this(new BufferedReader(new FileReader(f)));
    }
    
    @Override
    public boolean ready() {
        return getNumRowsRemaining() > 0;
    }

    @Override
    public int getNumRows() {
        return numRows;
    }

    @Override
    public int getNumRowsRemaining() {
        return numRowsRemaining;
    }

    private String[] parseLine(String line) {
        return splitPattern.split(line, -1);
    }

    @Override
    public GcodeCommand getNextCommand() throws IOException {
        if (numRowsRemaining == 0) return null;

        String line = reader.readLine();
        String[] nextLine = parseLine(line);
        if (nextLine.length != NUM_COLUMNS) {
            throw new IOException("Corrupt data found while processing gcode stream: " + line);
        }
        numRowsRemaining--;
        return new GcodeCommand(
                nextLine[COL_PROCESSED_COMMAND],
                nextLine[COL_ORIGINAL_COMMAND],
                nextLine[COL_COMMENT],
                Integer.parseInt(nextLine[COL_COMMAND_NUMBER]),
                false);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
