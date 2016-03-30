/*
 * GcodeStreamReader.java
 *
 * Reads a 'GcodeStream' file containing command processing information, actual
 * command to send and other metadata like total number of commands.
 *
 * Created on Jan 7, 2016
 */
/*
    Copywrite 2016 Will Winder

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
    File file;
    BufferedReader reader;
    int numRows;
    int numRowsRemaining;
    public GcodeStreamReader(File f) throws FileNotFoundException, IOException {
        file = f;
        reader = new BufferedReader(new FileReader(file));
        String metadata = reader.readLine().trim();
        numRows = Integer.parseInt(metadata);
        numRowsRemaining = numRows;
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

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
