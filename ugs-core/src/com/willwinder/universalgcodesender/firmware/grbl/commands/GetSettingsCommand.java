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

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetSettingsCommand extends GrblCommand {
    public GetSettingsCommand() {
        super(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND);
    }

    public List<FirmwareSetting> getSettings() {
        return Arrays.stream(StringUtils.split(getResponse(), "\n")).filter(line -> line.startsWith("$")).map(line -> {
            String[] split = line.split("=");
            return new FirmwareSetting(split[0], split[1]);
        }).collect(Collectors.toList());
    }
}
