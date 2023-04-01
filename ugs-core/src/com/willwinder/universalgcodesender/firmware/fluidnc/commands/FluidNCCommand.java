/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.universalgcodesender.GrblUtils.isGrblStatusString;

/**
 * A generic command that expects it to end with a command status "ok" or "error" to consider the command done
 */
public class FluidNCCommand extends GcodeCommand {
    public FluidNCCommand(String command) {
        super(command);
    }

    public FluidNCCommand(String command, String originalCommand, String comment, int lineNumber) {
        super(command, originalCommand, comment, lineNumber);
    }

    @Override
    public void setResponse(String response) {
        super.setResponse("");
        appendResponse(response);
    }

    @Override
    public void appendResponse(String response) {
        // In some cases the controller will echo the commands sent, do not add those to the response.
        if (response.equals(getOriginalCommandString())) {
            return;
        }

        // Do not append status strings to non status commands
        if (!StringUtils.equals(getCommandString(), "?") && isGrblStatusString(response)) {
            return;
        }

        super.appendResponse(response);

        if (response.startsWith("ok")) {
            setDone(true);
            setOk(true);
        }

        if (response.startsWith("error")) {
            setDone(true);
            setOk(false);
            setError(true);
        }
    }
}
