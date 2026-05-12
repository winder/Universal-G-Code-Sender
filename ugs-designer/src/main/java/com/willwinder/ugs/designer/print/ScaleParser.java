/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.print;

/**
 * Parses user-entered scale strings into a positive double scale factor.
 *
 * <p>Accepted forms (leading/trailing whitespace is tolerated, and {@code /} is treated as a
 * synonym for {@code :}):
 * <ul>
 *   <li>{@code a:b} — interpreted as engineering ratio, scale = {@code a/b}.</li>
 *   <li>{@code N%} — percentage, scale = {@code N/100}.</li>
 *   <li>bare decimal — used directly as the scale factor.</li>
 * </ul>
 *
 * <p>Returns {@code null} for anything that cannot be parsed, for zero/negative results, or for
 * a ratio with a zero denominator. Never throws.
 *
 * @author Damian Nikodem
 */
public final class ScaleParser {

    private ScaleParser() {
    }

    public static Double parse(String input) {
        if (input == null) {
            return null;
        }
        String text = input.trim().replace('/', ':');
        if (text.isEmpty()) {
            return null;
        }

        try {
            if (text.endsWith("%")) {
                double percent = Double.parseDouble(text.substring(0, text.length() - 1).trim());
                return validate(percent / 100.0);
            }
            int colonIdx = text.indexOf(':');
            if (colonIdx >= 0) {
                // Only the first colon is meaningful — reject multi-colon inputs like "1:2:3".
                if (text.indexOf(':', colonIdx + 1) >= 0) {
                    return null;
                }
                double a = Double.parseDouble(text.substring(0, colonIdx).trim());
                double b = Double.parseDouble(text.substring(colonIdx + 1).trim());
                if (b == 0.0) {
                    return null;
                }
                return validate(a / b);
            }
            return validate(Double.parseDouble(text));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double validate(double value) {
        if (!Double.isFinite(value) || value <= 0) {
            return null;
        }
        return value;
    }
}
