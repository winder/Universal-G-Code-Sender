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
package com.willwinder.universalgcodesender.firmware.grblhal;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Parses the extended build options that grblHAL reports in the {@code [NEWOPT:ENUMS,RT+,TC]} line
 * of the build info command ($I).
 *
 * @author Joacim Breiler
 */
public class GrblHalOptions {
    private final Set<String> options;

    public GrblHalOptions() {
        this("[NEWOPT:]");
    }

    public GrblHalOptions(String newOptionsLine) {
        String codes = StringUtils.substringBetween(newOptionsLine, "[NEWOPT:", "]");
        options = Arrays.stream(StringUtils.split(StringUtils.trimToEmpty(codes), ","))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotEmpty)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    public boolean isEnabled(GrblHalOption option) {
        return isEnabled(option.getCode());
    }

    public boolean isEnabled(String code) {
        return options.contains(code);
    }

    public Set<String> getOptions() {
        return Collections.unmodifiableSet(options);
    }
}
