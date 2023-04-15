/*
    Copyright 2023 Will Winder

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
package com.willwinder.universalgcodesender.firmware.grbl.commands;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.universalgcodesender.GrblUtils.isGrblStatusString;

public class GrblCommand extends GcodeCommand {
    public GrblCommand(String command) {
        super(command);
    }

    public GrblCommand(String command, String originalCommand, String comment, int commandNumber) {
        super(command, originalCommand, comment, commandNumber);
    }

    @Override
    public void appendResponse(String response) {
        // Do not append status strings to non status commands
        if (!StringUtils.equals(getCommandString(), "?") && isGrblStatusString(response)) {
            return;
        }

        super.appendResponse(response);
    }
}
