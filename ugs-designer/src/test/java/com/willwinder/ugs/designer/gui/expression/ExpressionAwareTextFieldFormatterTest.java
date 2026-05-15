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
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ExpressionAwareTextFieldFormatterTest {

    private static double parseAsDouble(ExpressionAwareTextFieldFormatter f, String text) throws ParseException {
        Object value = f.stringToValue(text);
        return ((Number) value).doubleValue();
    }

    @Test
    public void plainIntegerGoesThroughSuperPath() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        assertEquals(5.0, parseAsDouble(f, "5"), 1e-9);
    }

    @Test
    public void plainNumberWithUnitSuffix() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        assertEquals(5.0, parseAsDouble(f, "5 mm"), 1e-9);
    }

    @Test
    public void expressionEvaluates() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        assertEquals(1.5, parseAsDouble(f, "3/2"), 1e-9);
    }

    @Test
    public void expressionWithTrailingUnit() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        assertEquals(1.5, parseAsDouble(f, "3/2 mm"), 1e-9);
    }

    @Test
    public void expressionWithCommaDecimal() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        // Comma is the European decimal separator; the formatter normalises it.
        assertEquals(8.2, parseAsDouble(f, "7,2+1"), 1e-9);
    }

    @Test
    public void parenthesisedExpression() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        assertEquals(12.0, parseAsDouble(f, "(5+1)*2"), 1e-9);
    }

    @Test
    public void roundingHonoursDecimals() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 2);
        // 10/3 = 3.3333… should be rounded to 2 decimals by the super formatter
        assertEquals(3.33, parseAsDouble(f, "10/3"), 1e-9);
    }

    @Test
    public void garbageRejected() {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        try {
            f.stringToValue("foo");
            fail("expected ParseException");
        } catch (ParseException expected) {
            // ok
        }
    }

    @Test
    public void identifierExpressionRejected() {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        try {
            f.stringToValue("Math.sqrt(4)");
            fail("expected ParseException");
        } catch (ParseException expected) {
            // ok
        }
    }

    @Test
    public void valueToStringFormatsAsBeforeForMm() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.MM, 4);
        String formatted = f.valueToString(1.5);
        // The base formatter inserts " mm" by default
        assertEquals("1.5 mm", formatted);
    }

    @Test
    public void degreesPlainNumber() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.DEGREE, 4);
        assertEquals(45.0, parseAsDouble(f, "45"), 1e-9);
    }

    @Test
    public void degreesExpression() throws Exception {
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.DEGREE, 4);
        assertEquals(45.0, parseAsDouble(f, "360/8"), 1e-9);
    }

    @Test
    public void percentPlainNumberIsScaled() throws Exception {
        // For PERCENT the base formatter divides by 100 — "50" should store 0.5.
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.PERCENT, 4);
        assertEquals(0.5, parseAsDouble(f, "50"), 1e-9);
    }

    @Test
    public void percentExpressionIsScaled() throws Exception {
        // "50/2" = 25 % → stored as 0.25
        ExpressionAwareTextFieldFormatter f = new ExpressionAwareTextFieldFormatter(Unit.PERCENT, 4);
        assertEquals(0.25, parseAsDouble(f, "50/2"), 1e-9);
    }
}
