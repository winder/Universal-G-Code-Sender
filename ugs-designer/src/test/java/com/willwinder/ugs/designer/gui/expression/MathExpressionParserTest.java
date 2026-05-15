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

import org.junit.Test;

import java.util.OptionalDouble;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MathExpressionParserTest {

    private static void assertEvaluates(String input, double expected) {
        OptionalDouble result = MathExpressionParser.tryEvaluate(input);
        assertTrue("expected " + input + " to evaluate", result.isPresent());
        assertEquals(expected, result.getAsDouble(), 1e-9);
    }

    private static void assertRejects(String input) {
        OptionalDouble result = MathExpressionParser.tryEvaluate(input);
        assertFalse("expected " + input + " to be rejected", result.isPresent());
    }

    @Test
    public void simpleDivision() {
        assertEvaluates("3/2", 1.5);
    }

    @Test
    public void addingDecimalAndInteger() {
        assertEvaluates("7.2+1", 8.2);
    }

    @Test
    public void parenthesisedExpression() {
        assertEvaluates("(5+1)*2", 12.0);
    }

    @Test
    public void unaryMinus() {
        assertEvaluates("-3+4", 1.0);
    }

    @Test
    public void whitespaceAndNegativeFactor() {
        assertEvaluates("  10  *  -2 ", -20.0);
    }

    @Test
    public void leadingDecimalPoint() {
        assertEvaluates(".5+.5", 1.0);
    }

    @Test
    public void plainNumberIsValid() {
        assertEvaluates("5", 5.0);
    }

    @Test
    public void plainNegativeDecimalIsValid() {
        assertEvaluates("-3.7", -3.7);
    }

    @Test
    public void emptyStringRejected() {
        assertRejects("");
    }

    @Test
    public void nullRejected() {
        assertRejects(null);
    }

    @Test
    public void identifierRejected() {
        assertRejects("abc");
    }

    @Test
    public void doubleOperatorRejected() {
        assertRejects("5++3");
    }

    @Test
    public void unmatchedOpeningParenRejected() {
        assertRejects("(5+1");
    }

    @Test
    public void unmatchedClosingParenRejected() {
        assertRejects("5+1)");
    }

    @Test
    public void divisionByZeroRejected() {
        assertRejects("5/0");
    }

    @Test
    public void semicolonRejected() {
        assertRejects("5; System.exit(0)");
    }

    @Test
    public void identifierWithDotRejected() {
        assertRejects("Math.sqrt(4)");
    }

    @Test
    public void scientificNotationRejected() {
        assertRejects("1e3");
    }

    @Test
    public void trailingOperatorRejected() {
        assertRejects("5+");
    }

    @Test
    public void leadingMultiplicationRejected() {
        assertRejects("*5");
    }

    @Test
    public void precedenceMixedOperators() {
        assertEvaluates("2+3*4", 14.0);
        assertEvaluates("2*3+4", 10.0);
        assertEvaluates("8/4/2", 1.0);
        assertEvaluates("8-4-2", 2.0);
    }

    @Test
    public void parenthesisOverridesPrecedence() {
        assertEvaluates("(2+3)*4", 20.0);
    }

    @Test
    public void nestedParentheses() {
        assertEvaluates("((1+2)*(3+4))", 21.0);
    }

    @Test
    public void doubleNegationRejected() {
        // Stacked unary operators almost always indicate a typo — keep the
        // grammar strict and let the user re-type.
        assertRejects("--5");
        assertRejects("+-5");
    }
}
