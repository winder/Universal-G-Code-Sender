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
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOptions;
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gets the build information of the controller
 *
 * @author Joacim Breiler
 */
public class GetBuildInfoCommand extends GrblSystemCommand {
    public static final String FALLBACK_VERSION_STRING = "[VER: 1.1f]";
    private static final Logger LOGGER = Logger.getLogger(GetBuildInfoCommand.class.getSimpleName());

    public GetBuildInfoCommand() {
        super(GrblUtils.GRBL_BUILD_INFO_COMMAND);
    }

    public Optional<GrblVersion> getVersion() {
        String[] lines = StringUtils.split(getResponse(), "\n");

        // With some odd controllers it just responds with an ok, we will assume it is 1.1
        if (lines.length == 1 && StringUtils.equals(lines[0], "ok")) {
            return Optional.of(new GrblVersion(FALLBACK_VERSION_STRING));
        }

        // With GRBL 0.9 or older, the version string is only one line and an "ok"
        // treat those as a version string
        if (lines.length == 2 && StringUtils.equals(lines[1], "ok")) {
            return Optional.of(new GrblVersion(lines[0]));
        }

        return Arrays.stream(lines)
                .filter(line -> line.startsWith("[VER"))
                .map(line -> {
                    if (line.equalsIgnoreCase("[VER:]")) {
                        LOGGER.log(Level.SEVERE, "GRBL returned empty version string, we will assume that it is a 1.1 version");
                        return FALLBACK_VERSION_STRING;
                    }
                    return line;
                })
                .map(GrblVersion::new)
                .findFirst();
    }

    public GrblBuildOptions getBuildOptions() {
        String[] lines = StringUtils.split(getResponse(), "\n");

        if (lines.length == 2 && StringUtils.equals(lines[1], "ok")) {
            return new GrblBuildOptions();
        }

        return Arrays.stream(lines)
                .filter(line -> line.startsWith("[OPT:"))
                .map(GrblBuildOptions::new)
                .findFirst()
                .orElse(new GrblBuildOptions());
    }
}
