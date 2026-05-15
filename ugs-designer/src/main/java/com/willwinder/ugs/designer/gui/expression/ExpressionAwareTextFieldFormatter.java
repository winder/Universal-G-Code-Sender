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
package com.willwinder.ugs.designer.gui.expression;

import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.uielements.TextFieldUnitFormatter;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.OptionalDouble;

/**
 * {@link TextFieldUnitFormatter} that also accepts simple arithmetic expressions
 * (e.g. {@code 3/2}, {@code 7.2+1}, {@code (5+1)*2}). Plain numeric input takes
 * the normal path; only inputs the base formatter rejects are run through
 * {@link MathExpressionParser}.
 */
public class ExpressionAwareTextFieldFormatter extends TextFieldUnitFormatter {

    private static final Unit[] STRIPPABLE_UNITS = new Unit[]{
            Unit.MM,
            Unit.INCH,
            Unit.INCHES_PER_MINUTE,
            Unit.MM_PER_MINUTE,
            Unit.REVOLUTIONS_PER_MINUTE,
            Unit.PERCENT
    };

    public ExpressionAwareTextFieldFormatter(Unit unit, int numberOfDecimals) {
        super(unit, numberOfDecimals);
    }

    public ExpressionAwareTextFieldFormatter(Unit unit, int numberOfDecimals, boolean showAbbreviation) {
        super(unit, numberOfDecimals, showAbbreviation);
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        // Try the expression parser on a cleaned-up copy of the text. We can't
        // rely on the base formatter throwing for inputs like "3/2", because
        // DecimalFormat.parse stops at the first non-numeric character — it
        // would happily return 3.
        String stripped = stripTrailingUnitAbbreviation(text);
        stripped = StringUtils.replace(stripped, ",", ".");
        OptionalDouble evaluated = MathExpressionParser.tryEvaluate(stripped);
        if (evaluated.isPresent()) {
            // Re-enter the base formatter so rounding and PERCENT scaling stay
            // in a single place.
            return super.stringToValue(Double.toString(evaluated.getAsDouble()));
        }
        // Not an expression — let the base formatter handle plain numbers,
        // locale quirks, and its existing error reporting.
        return super.stringToValue(text);
    }

    private static String stripTrailingUnitAbbreviation(String text) {
        String value = text == null ? "" : text;
        for (Unit u : STRIPPABLE_UNITS) {
            value = StringUtils.removeEnd(value, u.getAbbreviation());
        }
        return StringUtils.trim(value);
    }
}
