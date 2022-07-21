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

import com.willwinder.universalgcodesender.GrblUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class GetParserStateCommand extends SystemCommand {

    public GetParserStateCommand() {
        super("$GCode/Modes");
    }

    public Optional<String> getState() {
        String state = null;
        for (String line : StringUtils.split(getResponse(), "\n")) {
            if (GrblUtils.isGrblFeedbackMessageV1(line)) {
                state = GrblUtils.parseFeedbackMessageV1(line);
            }
        }
        return Optional.ofNullable(state);
    }
}
