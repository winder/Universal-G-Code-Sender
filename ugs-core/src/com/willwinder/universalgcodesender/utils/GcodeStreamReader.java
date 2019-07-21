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
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author wwinder
 */
public class GcodeStreamReader extends GcodeStream implements Closeable {
    BufferedReader reader;
    int numRows;
    int numRowsRemaining;

    public static class NotGcodeStreamFile extends Exception {}

    public GcodeStreamReader(BufferedReader reader) throws NotGcodeStreamFile {
        this.reader = reader;
        
        try {
            String metadata = reader.readLine().trim();

            if (!metadata.startsWith(super.metaPrefix)) {
                throw new NotGcodeStreamFile();
            }

            metadata = metadata.substring(super.metaPrefix.length(), metadata.length());
            numRows = Integer.parseInt(metadata);
            numRowsRemaining = numRows;
        } catch (IOException | NumberFormatException e) {
            throw new NotGcodeStreamFile();
        }
    }

    public GcodeStreamReader(File f) throws NotGcodeStreamFile, FileNotFoundException {
        this(new BufferedReader(new FileReader(f)));
    }
    
    public boolean ready() {
        return getNumRowsRemaining() > 0;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumRowsRemaining() {
        return numRowsRemaining;
    }

    private String[] parseLine(String line) {
        return splitPattern.split(line, -1);
    }
    public GcodeCommand getNextCommand() throws IOException {
        if (numRowsRemaining == 0) return null;

        String line = reader.readLine();
        String nextLine[] = parseLine(line);
        if (nextLine.length != NUM_COLUMNS) {
            throw new IOException("Corrupt data found while processing gcode stream: " + line);
        }
        numRowsRemaining--;
        return new GcodeCommand(
                nextLine[COL_PROCESSED_COMMAND],
                nextLine[COL_ORIGINAL_COMMAND],
                nextLine[COL_COMMENT],
                Integer.parseInt(nextLine[COL_COMMAND_NUMBER]));
    }

    /**
     * Peek at the next command without moving the pointers
     * NOTE: this calls mark() and reset() so reset() calls outside of
     * peekNextCommand() may be interfered with by this.
     * @return GcodeCommand containing next line
     * @throws IOException
     */
    public GcodeCommand peekNextCommand() throws IOException {
        if (numRowsRemaining == 0) return null;

        reader.mark(NUM_COLUMNS);              // bookmark for rollback
        String line = reader.readLine();
        reader.reset();                        // rollback to mark

        String nextLine[] = parseLine(line);
        if (nextLine.length != NUM_COLUMNS) {
            throw new IOException("Corrupt data found while processing gcode stream: " + line);
        }
        return new GcodeCommand(
                nextLine[COL_PROCESSED_COMMAND],
                nextLine[COL_ORIGINAL_COMMAND],
                nextLine[COL_COMMENT],
                Integer.parseInt(nextLine[COL_COMMAND_NUMBER]));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
