/*
    Copyright 2019 Will Winder

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
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 * A simple Gcode stream for transmitting commands to a controller using string
 * as an input.
 *
 * @author Joacim Breiler
 */
public class StringGcodeStreamReader implements IGcodeStreamReader {

    private final List<String> lines;
    private int currentLine;

    /**
     * A constructor for creating a stream using a multiline string with a gcode program.
     * Each line should be seperated by an escaped new line character \n
     *
     * @param gcode the gcode program as a multi line string
     * @throws IOException
     */
    public StringGcodeStreamReader(String gcode) throws IOException {
        this(IOUtils.readLines(new StringReader(gcode)));
    }

    /**
     * A constructor for creating a stream using multiple strings for each line of gcode.
     *
     * @param gcodeLines multiple gcode commands as an array
     */
    public StringGcodeStreamReader(String... gcodeLines) {
        this(Arrays.asList(gcodeLines));
    }

    /**
     * A constructor for creating a stream using a list of gcode commands
     *
     * @param gcodeLines a list of gcode commands
     */
    public StringGcodeStreamReader(List<String> gcodeLines) {
        lines = gcodeLines;
        currentLine = 0;
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public int getNumRows() {
        return lines.size();
    }

    @Override
    public int getNumRowsRemaining() {
        return lines.size() - currentLine;
    }

    @Override
    public GcodeCommand getNextCommand() {
        if (currentLine > lines.size()) {
            return null;
        }

        GcodeCommand gcodeCommand = new GcodeCommand(lines.get(currentLine), currentLine);
        currentLine++;
        return gcodeCommand;
    }

    @Override
    public void close() {
        currentLine = 0;
        lines.clear();
    }
}
