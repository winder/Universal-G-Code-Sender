/**
 * Splits up any command containing more than one of G, M, S or T commands.
 * Be sure to apply the command splitter AFTER the comment processor, or else
 * commands may be split inside a comment.
 */
/*
    Copywrite 2016 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wwinder
 */
public class CommandSplitter implements ICommandProcessor {
    Pattern GROUP_PATTERN = Pattern.compile("([GgMmSsTt].*?)(?=[GgMmSsTt]|$)");

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        List<String> ret = new ArrayList<>();
        
        Matcher m = GROUP_PATTERN.matcher(command);

        while (m.find()) {
            ret.add(m.group(0).trim());
        }

        if (ret.isEmpty()) {
            ret.add(command);
        }

        return ret;
    }
    
}
