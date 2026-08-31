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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The error and alarm codes that the controller has reported using the enumeration commands $EE and
 * $EA. These replace the code descriptions that are bundled with UGS, so a controller with plugins
 * or a newer firmware can explain its own codes.
 *
 * @author Joacim Breiler
 */
public class GrblHalCodes {

    /**
     * Matches a response containing an error or alarm code, ie {@code error:1} or {@code ALARM:11}
     */
    private static final Pattern CODE_PATTERN = Pattern.compile("^(error|alarm):\\s*(\\d+)$", Pattern.CASE_INSENSITIVE);

    private static final String ALARM = "alarm";
    private static final String UNKNOWN_DESCRIPTION = "An unknown error has occurred";

    private final Map<String, GrblHalCode> errorCodes = new ConcurrentHashMap<>();
    private final Map<String, GrblHalCode> alarmCodes = new ConcurrentHashMap<>();

    public void updateErrorCodes(List<GrblHalCode> codes) {
        replaceAll(errorCodes, codes);
    }

    public void updateAlarmCodes(List<GrblHalCode> codes) {
        replaceAll(alarmCodes, codes);
    }

    public Optional<GrblHalCode> getErrorCode(String code) {
        return Optional.ofNullable(errorCodes.get(code));
    }

    public Optional<GrblHalCode> getAlarmCode(String code) {
        return Optional.ofNullable(alarmCodes.get(code));
    }

    /**
     * Describes an error or alarm response using the codes reported by the controller, ie the
     * response {@code error:1} becomes
     * {@code (error:1) G-code words consist of a letter and a value. Letter was not found.}
     * <p>
     * Responses that are not an error or alarm code, and responses received before the controller
     * has enumerated its codes, are returned unchanged.
     *
     * @param response a response from the controller
     * @return the described response
     */
    public String lookupCode(String response) {
        Matcher matcher = CODE_PATTERN.matcher(StringUtils.trimToEmpty(response));
        if (!matcher.matches()) {
            return response;
        }

        Map<String, GrblHalCode> codes = ALARM.equalsIgnoreCase(matcher.group(1)) ? alarmCodes : errorCodes;
        if (codes.isEmpty()) {
            return response;
        }

        String description = Optional.ofNullable(codes.get(matcher.group(2)))
                .map(GrblHalCodes::describe)
                .orElse(UNKNOWN_DESCRIPTION);

        return "(" + response + ") " + description;
    }

    private static String describe(GrblHalCode code) {
        return StringUtils.isEmpty(code.description()) ? code.message() : code.description();
    }

    private static void replaceAll(Map<String, GrblHalCode> target, List<GrblHalCode> codes) {
        target.clear();
        codes.forEach(code -> target.put(code.code(), code));
    }
}
