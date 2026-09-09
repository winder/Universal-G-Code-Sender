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
package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a tool profile out of the tool descriptions the designer writes into a program, such as
 * {@code 6mm Upcut}, {@code 6.35mm Ball}, {@code 1/4" Downcut} or {@code 6mm V-bit 60°}. The shape
 * words are the display names of the tool library shapes; anything without a ball or V-bit word is
 * treated as a flat endmill.
 */
public final class ToolDescriptionParser {
    private static final double DEFAULT_V_BIT_ANGLE = 90;
    private static final Pattern MILLIMETERS = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*mm", Pattern.CASE_INSENSITIVE);
    private static final Pattern INCHES = Pattern.compile("(\\d+(?:[.,]\\d+)?(?:\\s*/\\s*\\d+)?)\\s*(?:\"|in\\b|inch)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ANGLE = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:°|deg)", Pattern.CASE_INSENSITIVE);

    private ToolDescriptionParser() {
    }

    public static Optional<ToolProfile> parse(String description) {
        if (description == null) {
            return Optional.empty();
        }
        Optional<Double> diameter = diameter(description);
        if (diameter.isEmpty() || diameter.get() <= 0) {
            return Optional.empty();
        }
        String lower = description.toLowerCase(Locale.ROOT);
        if (lower.contains("v-bit") || lower.contains("vbit") || lower.contains("v bit")) {
            Matcher angle = ANGLE.matcher(description);
            double degrees = angle.find() ? number(angle.group(1)) : DEFAULT_V_BIT_ANGLE;
            return Optional.of(ToolProfile.vBit(diameter.get(), degrees));
        }
        if (lower.contains("ball")) {
            return Optional.of(ToolProfile.ball(diameter.get()));
        }
        return Optional.of(ToolProfile.flat(diameter.get()));
    }

    private static Optional<Double> diameter(String description) {
        Matcher mm = MILLIMETERS.matcher(description);
        if (mm.find()) {
            return Optional.of(number(mm.group(1)));
        }
        Matcher inch = INCHES.matcher(description);
        if (inch.find()) {
            return Optional.of(fraction(inch.group(1)) * 25.4);
        }
        return Optional.empty();
    }

    private static double fraction(String text) {
        String[] parts = text.split("/");
        double value = number(parts[0]);
        if (parts.length == 2) {
            double denominator = number(parts[1]);
            return denominator == 0 ? 0 : value / denominator;
        }
        return value;
    }

    private static double number(String text) {
        return Double.parseDouble(text.trim().replace(',', '.'));
    }
}
