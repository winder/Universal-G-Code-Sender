/*
    Copyright 2013-2023 Will Winder

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
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * A command creator that may be used for creating commands for a specific type of controller
 *
 * @author Joacim Breiler
 */
public interface ICommandCreator {

    /**
     * Creates a simple command for a specific type of controller
     *
     * @param command the command to be sent as a string
     * @return the created command
     */
    GcodeCommand createCommand(String command);

    /**
     * Creates a complete command for a specific type of controller
     *
     * @param command the command to be sent to the controller
     * @param originalCommand an optional original command that was used by the preprocessing engine
     * @param comment an optional comment that is associated with the command
     * @param lineNumber an optional line number that this command originates from
     * @return the created command
     */
    GcodeCommand createCommand(String command, String originalCommand, String comment, int lineNumber);
}
