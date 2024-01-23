/*
    Copyright 2024 Will Winder

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Writes the preprocessed gcode to the given file.
 *
 * @author Joacim Breiler
 */
public class SimpleGcodeStreamWriter implements IGcodeWriter {
    private final PrintWriter fileWriter;

    public SimpleGcodeStreamWriter(File f) throws FileNotFoundException {
        try {
            fileWriter = new PrintWriter(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileNotFoundException("Could not use UTF-8 to output gcode stream file");
        }
    }

    @Override
    public String getCanonicalPath() {
        return "";
    }

    @Override
    public void addLine(GcodeCommand command) {
        addLine(command.getOriginalCommandString(), command.getCommandString(), command.getComment(), command.getCommandNumber());
    }

    @Override
    public void addLine(String original, String processed, String comment, int commandNumber) {
        fileWriter.append(processed);
        fileWriter.append("\n");
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }
}
