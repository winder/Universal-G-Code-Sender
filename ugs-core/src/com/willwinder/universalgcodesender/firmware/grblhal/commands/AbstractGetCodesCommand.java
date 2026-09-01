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

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enumerates codes reported by the controller in the format
 * {@code [<prefix>:<code>|<message>|<description>]}, one line per code.
 *
 * @author Joacim Breiler
 */
public abstract class AbstractGetCodesCommand extends GrblHalSystemCommand {
    private static final int FIELD_LIMIT = 3;
    private static final int FIELD_CODE = 0;
    private static final int FIELD_MESSAGE = 1;
    private static final int FIELD_DESCRIPTION = 2;

    private final String prefix;

    protected AbstractGetCodesCommand(String command, String prefix) {
        super(command);
        this.prefix = prefix;
    }

    public List<GrblHalCode> getCodes() {
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(getResponse()), "\n"))
                .map(StringUtils::trimToEmpty)
                .filter(line -> line.startsWith(prefix))
                .map(this::parseCode)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<GrblHalCode> parseCode(String line) {
        String[] fields = StringUtils.removeEnd(StringUtils.removeStart(line, prefix), "]")
                .split("\\|", FIELD_LIMIT);
        if (fields.length <= FIELD_MESSAGE) {
            return Optional.empty();
        }

        return Optional.of(new GrblHalCode(
                fields[FIELD_CODE].trim(),
                fields[FIELD_MESSAGE].trim(),
                fields.length > FIELD_DESCRIPTION ? fields[FIELD_DESCRIPTION].trim() : ""));
    }
}
