/*
    Copyright 2026 Joacim Breiler

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

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalDataType;
import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalSettingDetail;
import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enumerates every setting supported by the controller using the command $ES. The controller
 * responds with one line per setting in the format
 * {@code [SETTING:<id>|<group id>|<name>|<unit>|<data type>|<format>|<min>|<max>|<reboot required>|<null allowed>]}:
 *
 * <pre>
 * [SETTING:0|35|Step pulse time|microseconds|6|#0.0|1.0||0|0]
 * [SETTING:14|2|Invert control inputs||1|N/A,Feed hold,Cycle start,N/A,N/A,N/A,EStop|||0|0]
 * ok
 * </pre>
 * <p>
 * Trailing fields may be omitted by the controller. Note that the enumeration does not contain the
 * long descriptions of the settings, only their names.
 * <a href="https://github.com/grblHAL/core/wiki/Report-extensions">Documentation</a>
 *
 * @author Joacim Breiler
 */
public class GetSettingDetailsCommand extends GrblHalSystemCommand {
    private static final String SETTING_PREFIX = "[SETTING:";
    private static final String FIELD_SEPARATOR = "\\|";

    private static final int FIELD_KEY = 0;
    private static final int FIELD_GROUP_ID = 1;
    private static final int FIELD_NAME = 2;
    private static final int FIELD_UNITS = 3;
    private static final int FIELD_DATA_TYPE = 4;
    private static final int FIELD_FORMAT = 5;
    private static final int FIELD_MIN = 6;
    private static final int FIELD_MAX = 7;
    private static final int FIELD_REBOOT_REQUIRED = 8;
    private static final int FIELD_ALLOW_NULL = 9;

    public GetSettingDetailsCommand() {
        super(GrblHalUtils.GRBLHAL_SETTING_DETAILS_COMMAND);
    }

    public List<GrblHalSettingDetail> getSettingDetails() {
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(getResponse()), "\n"))
                .map(StringUtils::trimToEmpty)
                .filter(line -> line.startsWith(SETTING_PREFIX))
                .map(GetSettingDetailsCommand::parseSetting)
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<GrblHalSettingDetail> parseSetting(String line) {
        String[] fields = StringUtils.removeEnd(StringUtils.removeStart(line, SETTING_PREFIX), "]")
                .split(FIELD_SEPARATOR, -1);
        if (fields.length <= FIELD_DATA_TYPE) {
            return Optional.empty();
        }

        return Optional.of(new GrblHalSettingDetail(
                "$" + fields[FIELD_KEY].trim(),
                fields[FIELD_GROUP_ID].trim(),
                fields[FIELD_NAME].trim(),
                fields[FIELD_UNITS].trim(),
                GrblHalDataType.fromCode(fields[FIELD_DATA_TYPE].trim()),
                field(fields, FIELD_FORMAT),
                field(fields, FIELD_MIN),
                field(fields, FIELD_MAX),
                isEnabled(field(fields, FIELD_REBOOT_REQUIRED)),
                isEnabled(field(fields, FIELD_ALLOW_NULL))));
    }

    private static String field(String[] fields, int index) {
        return index < fields.length ? fields[index].trim() : "";
    }

    private static boolean isEnabled(String field) {
        return "1".equals(field);
    }
}
