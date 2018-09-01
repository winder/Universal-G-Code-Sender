/*
    Copyright 2017-2018 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Adds a dwell command after an M3 or M4.
 *
 * @author wwinder
 */
public class SpindleOnDweller implements CommandProcessor {
    private final String dwellCommand;

    // Contains an M3 not followed by another digit (i.e. M30)
    private Pattern spindleOnPattern = Pattern.compile(".*[mM][34](?!\\d)(\\D.*)?");

    public SpindleOnDweller(double dwellDuration) {
        this.dwellCommand = String.format(Locale.ROOT, "G4P%.2f", dwellDuration);
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        String noComments = GcodePreprocessorUtils.removeComment(command);
        if (spindleOnPattern.matcher(noComments).matches()) {
            return Arrays.asList(command, dwellCommand);
        }
        return Collections.singletonList(command);
    }

    @Override
    public String getHelp() {
        return Localization.getString("sender.help.spindle-dwell");
    }
}
