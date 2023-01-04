/*
    Copyright 2016-2023 Will Winder

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

import com.willwinder.universalgcodesender.gcode.ICommandCreator;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.willwinder.universalgcodesender.utils.GcodeStream.COL_COMMAND_NUMBER;
import static com.willwinder.universalgcodesender.utils.GcodeStream.COL_COMMENT;
import static com.willwinder.universalgcodesender.utils.GcodeStream.COL_ORIGINAL_COMMAND;
import static com.willwinder.universalgcodesender.utils.GcodeStream.COL_PROCESSED_COMMAND;
import static com.willwinder.universalgcodesender.utils.GcodeStream.META_PREFIX;
import static com.willwinder.universalgcodesender.utils.GcodeStream.NUM_COLUMNS;
import static com.willwinder.universalgcodesender.utils.GcodeStream.SPLIT_PATTERN;

/**
 * Reads a 'GcodeStream' file containing command processing information, actual
 * command to send and other metadata like total number of commands.
 *
 * @author wwinder
 */
public class GcodeStreamReader implements IGcodeStreamReader {
    private final ICommandCreator commandCreator;
    private final BufferedReader reader;
    private final int numRows;
    private int numRowsRemaining;

    public static class NotGcodeStreamFile extends Exception {}

    public GcodeStreamReader(InputStream inputStream, ICommandCreator commandCreator) throws NotGcodeStreamFile {
        this.commandCreator = commandCreator;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String metadata = StringUtils.trimToEmpty(reader.readLine());
            if (!metadata.startsWith(META_PREFIX)) {
                throw new NotGcodeStreamFile();
            }

            metadata = metadata.substring(META_PREFIX.length());
            numRows = Integer.parseInt(metadata);
            numRowsRemaining = numRows;
        } catch (IOException | NumberFormatException e) {
            throw new NotGcodeStreamFile();
        }
    }

    public GcodeStreamReader(File f, ICommandCreator commandCreator) throws NotGcodeStreamFile, FileNotFoundException {
        this(new FileInputStream(f), commandCreator);
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
        return SPLIT_PATTERN.split(line, -1);
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
        return commandCreator.createCommand(
                nextLine[COL_PROCESSED_COMMAND],
                nextLine[COL_ORIGINAL_COMMAND],
                nextLine[COL_COMMENT],
                Integer.parseInt(nextLine[COL_COMMAND_NUMBER]));
    }

    @Override
    public void close() throws IOException {
        numRowsRemaining = 0;
        reader.close();
    }
}
