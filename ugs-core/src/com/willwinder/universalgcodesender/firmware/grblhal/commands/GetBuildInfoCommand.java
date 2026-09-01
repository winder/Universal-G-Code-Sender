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

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOptions;
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalOptions;
import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Fetches the build information from a grblHAL controller using the command $I. In addition to the
 * information reported by GRBL, grblHAL will report which firmware it is running, its extended build
 * options and any loaded plugins:
 *
 * <pre>
 * [VER:1.1f.20230312:]
 * [OPT:VSHM,35,1024,3,0]
 * [NEWOPT:ENUMS,RT+,HOME,TC,SED]
 * [FIRMWARE:grblHAL]
 * [PLUGIN:SDCARD v1.07]
 * ok
 * </pre>
 *
 * @author Joacim Breiler
 */
public class GetBuildInfoCommand extends GrblHalSystemCommand {
    private static final String VERSION_PREFIX = "[VER:";
    private static final String OPTIONS_PREFIX = "[OPT:";
    private static final String EXTENDED_OPTIONS_PREFIX = "[NEWOPT:";
    private static final String FIRMWARE_PREFIX = "[FIRMWARE:";
    private static final String PLUGIN_PREFIX = "[PLUGIN:";

    public GetBuildInfoCommand() {
        super(GrblUtils.GRBL_BUILD_INFO_COMMAND);
    }

    public Optional<GrblVersion> getVersion() {
        return findLine(VERSION_PREFIX).map(GrblVersion::new);
    }

    public GrblBuildOptions getBuildOptions() {
        return findLine(OPTIONS_PREFIX)
                .map(GrblBuildOptions::new)
                .orElseGet(GrblBuildOptions::new);
    }

    public GrblHalOptions getExtendedOptions() {
        return findLine(EXTENDED_OPTIONS_PREFIX)
                .map(GrblHalOptions::new)
                .orElseGet(GrblHalOptions::new);
    }

    /**
     * Returns the name of the firmware running on the controller, ie {@code grblHAL}.
     *
     * @return the firmware name or an empty optional if the controller didn't report one
     */
    public Optional<String> getFirmwareName() {
        return findLine(FIRMWARE_PREFIX)
                .map(line -> StringUtils.substringBetween(line, FIRMWARE_PREFIX, "]"))
                .map(StringUtils::trimToEmpty);
    }

    /**
     * Returns the plugins that has been loaded by the controller, ie {@code SDCARD v1.07}.
     *
     * @return a list of plugin descriptions
     */
    public List<String> getPlugins() {
        return responseLines()
                .filter(line -> line.startsWith(PLUGIN_PREFIX))
                .map(line -> StringUtils.substringBetween(line, PLUGIN_PREFIX, "]"))
                .map(StringUtils::trimToEmpty)
                .toList();
    }

    /**
     * Returns if the controller identified itself as grblHAL.
     *
     * @return true if this is a grblHAL controller
     */
    public boolean isGrblHal() {
        return getFirmwareName()
                .filter(name -> StringUtils.equalsIgnoreCase(name, GrblHalUtils.FIRMWARE_NAME))
                .isPresent();
    }

    private Optional<String> findLine(String prefix) {
        return responseLines()
                .filter(line -> line.startsWith(prefix))
                .findFirst();
    }

    private Stream<String> responseLines() {
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(getResponse()), "\n"))
                .map(StringUtils::trimToEmpty);
    }
}
