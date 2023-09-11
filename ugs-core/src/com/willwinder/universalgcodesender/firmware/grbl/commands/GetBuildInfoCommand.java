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
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * Gets the build information of the controller
 *
 * @author Joacim Breiler
 */
public class GetBuildInfoCommand extends GrblSystemCommand {
    public GetBuildInfoCommand() {
        super(GrblUtils.GRBL_BUILD_INFO_COMMAND);
    }

    public Optional<GrblVersion> getVersion() {
        String[] lines = StringUtils.split(getResponse(), "\n");

        // With GRBL 0.9 or older, the version string is only one line and an "ok"
        // treat those as a version string
        if (lines.length == 2 && StringUtils.equals(lines[1], "ok")) {
            return Optional.of(new GrblVersion(lines[0]));
        }

        return Arrays.stream(lines)
                .filter(line -> line.startsWith("[VER"))
                .map(GrblVersion::new).findFirst();
    }
}
