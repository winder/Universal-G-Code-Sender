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
package com.willwinder.universalgcodesender.firmware.smoothie;

import com.willwinder.universalgcodesender.firmware.smoothie.commands.SmoothieCommand;
import com.willwinder.universalgcodesender.gcode.ICommandCreator;
import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * A command creator for smoothie firmware controllers
 */
public class SmoothieCommandCreator implements ICommandCreator {
    @Override
    public GcodeCommand createCommand(String command) {
        return new SmoothieCommand(command);
    }

    @Override
    public GcodeCommand createCommand(String command, String originalCommand, String comment, int lineNumber) {
        return  new SmoothieCommand(command, originalCommand, comment, lineNumber);
    }
}
