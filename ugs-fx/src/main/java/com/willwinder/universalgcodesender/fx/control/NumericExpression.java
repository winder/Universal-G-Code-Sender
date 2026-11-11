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
package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.ugs.designer.gui.expression.MathExpressionParser;

import java.util.OptionalDouble;

/**
 * The value of what is typed in a numeric field: a number or an arithmetic expression such as
 * {@code 25.4 * 2}, see {@link MathExpressionParser}. Kept apart from the field so it can be
 * tested without the JavaFX toolkit.
 */
final class NumericExpression {
    private NumericExpression() {
    }

    /**
     * The value of the text when it is a complete expression with a finite value.
     */
    static OptionalDouble evaluate(String text) {
        OptionalDouble value = MathExpressionParser.tryEvaluate(text);
        return value.isPresent() && Double.isFinite(value.getAsDouble()) ? value : OptionalDouble.empty();
    }
}
