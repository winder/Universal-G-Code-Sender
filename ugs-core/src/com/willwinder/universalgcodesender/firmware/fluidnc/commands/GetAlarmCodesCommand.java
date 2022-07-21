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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetAlarmCodesCommand extends SystemCommand {
    public GetAlarmCodesCommand() {
        super("$Alarms/List");
    }

    public Map<Integer, String> getErrorCodes() {
        Map<Integer, String> alarmCodes = new HashMap<>();
        Pattern alarmCodePattern = Pattern.compile("([0-9]+):(.*)");
        Arrays.stream(StringUtils.split(getResponse(), "\n")).forEach(line -> {
            Matcher matcher = alarmCodePattern.matcher(line);
            if (matcher.find()) {
                alarmCodes.put(Integer.parseInt(matcher.group(1)), matcher.group(2));
            }
        });
        return alarmCodes;
    }
}
