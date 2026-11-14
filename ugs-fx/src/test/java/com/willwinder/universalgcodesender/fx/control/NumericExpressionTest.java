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

import org.junit.Test;

import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class NumericExpressionTest {

    @Test
    public void evaluate_shouldEvaluateAnExpression() {
        OptionalDouble result = NumericExpression.evaluate("(10 + 4) / 2 * 25.4");

        assertThat(result.getAsDouble()).isCloseTo(177.8, within(1e-9));
    }

    @Test
    public void evaluate_shouldBeEmptyForAnIncompleteExpression() {
        OptionalDouble result = NumericExpression.evaluate("2 *");

        assertThat(result).isEmpty();
    }

    @Test
    public void evaluate_shouldBeEmptyForADivisionByZero() {
        OptionalDouble result = NumericExpression.evaluate("1 / 0");

        assertThat(result).isEmpty();
    }

}
