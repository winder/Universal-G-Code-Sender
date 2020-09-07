/**
 * Used by the gcode parser to preprocess commands.
 */
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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import java.util.List;

/**
 *
 * @author wwinder
 */
public interface CommandProcessor {
    /**
     * Given a command and the current state of a program returns a replacement
     * list of commands.
     * @param command Input gcode.
     * @param state State of the gcode parser when the command will run.
     * @return One or more gcode commands to replace the original command with.
     */
    List<String> processCommand(String command, GcodeState state) throws GcodeParserException;

    /**
     * Returns information about the current command and its configuration.
     * @return 
     */
    String getHelp();
}
