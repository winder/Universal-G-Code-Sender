/**
 * Throws an exception if the command is too long.
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
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class CommandLengthProcessor implements CommandProcessor {
    final private int length;
    public CommandLengthProcessor(int length) {
        this.length = length;
    }

    @Override
    public String getHelp() {
        return Localization.getString("sender.help.command.length") + "\n" +
                Localization.getString("sender.command.length")
                + ": " + length;
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        if (command.length() > length)
            throw new GcodeParserException("Command '" + command + "' is longer than " + length + " characters.");

        List<String> ret = new ArrayList<>();
        ret.add(command);
        return ret;
    }
    
}
