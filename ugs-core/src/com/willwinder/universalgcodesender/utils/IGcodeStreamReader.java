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

import java.io.Closeable;
import java.io.IOException;

/**
 * A Gcode stream for transmitting gcode commands to a controller
 *
 * @author Joacim Breiler
 */
public interface IGcodeStreamReader extends Closeable {

    /**
     * Returns true if the gcode stream is ready to stream commands
     *
     * @return true if ready
     */
    boolean ready();

    /**
     * Get total number of rows in the stream
     *
     * @return an integer with total number of rows
     */
    int getNumRows();

    /**
     * Get number of rows remaining in the stream
     *
     * @return an integer with the remaining rows in the stream
     */
    int getNumRowsRemaining();

    /**
     * Returns the next command in the stream or null if stream is finished
     *
     * @return the next command or null
     * @throws IOException if the stream can not be read
     */
    GcodeCommand getNextCommand() throws IOException;
}
