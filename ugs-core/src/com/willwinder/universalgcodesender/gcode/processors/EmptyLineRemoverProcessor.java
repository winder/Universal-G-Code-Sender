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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Removes empty lines from the gcode file
 *
 * @author Joacim Breiler
 */
public class EmptyLineRemoverProcessor implements CommandProcessor {

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        if (StringUtils.trimToNull(command) == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(command);
    }

    @Override
    public String getHelp() {
        return Localization.getString("sender.help.empty-line-remover");
    }
}
