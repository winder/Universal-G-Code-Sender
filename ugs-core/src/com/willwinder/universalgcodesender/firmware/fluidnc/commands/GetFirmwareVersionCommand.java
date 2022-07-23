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

import com.willwinder.universalgcodesender.utils.SemanticVersion;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetFirmwareVersionCommand extends SystemCommand {
    private static final Pattern VERSION_FLUIDNC_PATTERN = Pattern.compile("\\[VER:[0-9.]+ (?<variant>[a-zA-Z0-9]+) v(?<version>(?<major>[0-9]*)(.(?<minor>[0-9]+)(.(?<patch>[0-9]+))?)?([a-zA-Z]+)?)(.*:.*)*]", Pattern.CASE_INSENSITIVE);
    private static final Pattern VERSION_GRBL_PATTERN = Pattern.compile("\\[VER:(?<version>(?<major>[0-9]*)(.(?<minor>[0-9]+)(.(?<patch>[0-9]+))?)).*]", Pattern.CASE_INSENSITIVE);

    public GetFirmwareVersionCommand() {
        super("$I");
    }

    public String getFirmware() {
        Optional<String> firmwareOptional = parseFluidNCVariant();
        if (!firmwareOptional.isPresent()) {
            firmwareOptional = parseGrblVariant();
        }

        return firmwareOptional.orElse("Unknown");
    }

    private Optional<String> parseGrblVariant() {
        String response = StringUtils.defaultString(getResponse());
        for (String line : StringUtils.split(response, "\n")) {
            Matcher versionMatcher = VERSION_GRBL_PATTERN.matcher(line);
            if (versionMatcher.find()) {
                return Optional.of("GRBL");
            }
        }
        return Optional.empty();
    }

    private Optional<String> parseFluidNCVariant() {
        String response = StringUtils.defaultString(getResponse());
        for (String line : StringUtils.split(response, "\n")) {
            Matcher versionMatcher = VERSION_FLUIDNC_PATTERN.matcher(line);
            if (versionMatcher.find()) {
                return Optional.of(versionMatcher.group("variant"));
            }
        }
        return Optional.empty();
    }

    public SemanticVersion getVersion() {
        Optional<SemanticVersion> versionOptional = parseFluidNCVs();
        if (!versionOptional.isPresent()) {
            versionOptional = parseGrblVersion();
        }
        return versionOptional.orElse(new SemanticVersion());
    }

    private Optional<SemanticVersion> parseGrblVersion() {
        String response = StringUtils.defaultString(getResponse());
        for (String line : StringUtils.split(response, "\n")) {
            Matcher versionMatcher = VERSION_GRBL_PATTERN.matcher(line);
            if (versionMatcher.find()) {
                try {
                    return Optional.of(new SemanticVersion(versionMatcher.group("version")));
                } catch (ParseException e) {
                    // Never mind
                }
            }
        }
        return Optional.empty();
    }

    private Optional<SemanticVersion> parseFluidNCVs() {
        String response = StringUtils.defaultString(getResponse());
        for (String line : StringUtils.split(response, "\n")) {
            Matcher matcher = VERSION_FLUIDNC_PATTERN.matcher(line);
            if (matcher.find()) {
                try {
                    return Optional.of(new SemanticVersion(matcher.group("version")));
                } catch (ParseException e) {
                    // Never mind
                }
            }
        }
        return Optional.empty();
    }
}
