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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple Gcode stream for transmitting commands to a controller using string
 * as an input.
 *
 * @author Joacim Breiler
 */
public class SimpleGcodeStreamReader implements IGcodeStreamReader {

    private final List<GcodeCommand> commands = new ArrayList<>();
    private int currentLine;

    /**
     * A constructor for creating a stream using multiple strings for each line of gcode.
     *
     * @param gcodeLines multiple gcode commands as an array
     */
    public SimpleGcodeStreamReader(String... gcodeLines) {
        for (int i = 0; i < gcodeLines.length; i++) {
            commands.add(new GcodeCommand(gcodeLines[i], i));
        }
        currentLine = 0;
    }

    /**
     * A constructor for creating a stream using a list of gcode commands
     *
     * @param commands a list of gcode commands
     */
    public SimpleGcodeStreamReader(GcodeCommand... commands) {
        this.commands.addAll(Arrays.asList(commands));
        currentLine = 0;
    }

    public SimpleGcodeStreamReader(List<GcodeCommand> commands) {
        this.commands.addAll(commands);
        currentLine = 0;
    }

    @Override
    public boolean ready() {
        return currentLine <= commands.size();
    }

    @Override
    public int getNumRows() {
        return commands.size();
    }

    @Override
    public int getNumRowsRemaining() {
        return commands.size() - currentLine;
    }

    @Override
    public GcodeCommand getNextCommand() {
        if (currentLine >= commands.size()) {
            return null;
        }

        return commands.get(currentLine++);
    }

    @Override
    public void close() {
        currentLine = 0;
        commands.clear();
    }
}
