/*
    Copyright 2026 Will Winder

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
package com.willwinder.universalgcodesender.firmware.grblhal.commands;

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalSettingGroup;
import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enumerates the setting groups supported by the controller using the command $EG:
 *
 * <pre>
 * [SETTINGGROUP:0|0|Root]
 * [SETTINGGROUP:2|0|Control signals]
 * [SETTINGGROUP:43|42|X-axis]
 * ok
 * </pre>
 *
 * @author Joacim Breiler
 */
public class GetSettingGroupsCommand extends GrblHalSystemCommand {
    private static final String GROUP_PREFIX = "[SETTINGGROUP:";
    private static final int FIELD_LIMIT = 3;
    private static final int FIELD_ID = 0;
    private static final int FIELD_PARENT_ID = 1;
    private static final int FIELD_NAME = 2;

    public GetSettingGroupsCommand() {
        super(GrblHalUtils.GRBLHAL_SETTING_GROUPS_COMMAND);
    }

    public List<GrblHalSettingGroup> getSettingGroups() {
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(getResponse()), "\n"))
                .map(StringUtils::trimToEmpty)
                .filter(line -> line.startsWith(GROUP_PREFIX))
                .map(GetSettingGroupsCommand::parseGroup)
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<GrblHalSettingGroup> parseGroup(String line) {
        String[] fields = StringUtils.removeEnd(StringUtils.removeStart(line, GROUP_PREFIX), "]")
                .split("\\|", FIELD_LIMIT);
        if (fields.length <= FIELD_NAME) {
            return Optional.empty();
        }

        return Optional.of(new GrblHalSettingGroup(
                fields[FIELD_ID].trim(),
                fields[FIELD_PARENT_ID].trim(),
                fields[FIELD_NAME].trim()));
    }
}
