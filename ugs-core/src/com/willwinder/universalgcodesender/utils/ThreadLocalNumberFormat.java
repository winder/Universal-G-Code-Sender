/*
    Copyright 2026 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.Supplier;

/**
 * A number format that delegates to one instance per thread.
 * <p>
 * Number formats keep their parser state in the instance and are not thread safe. Sharing one
 * between threads may produce corrupted numbers when formatting and throw
 * {@link NumberFormatException} when parsing.
 *
 * @author Joacim Breiler
 */
public class ThreadLocalNumberFormat extends NumberFormat {
    private static final long serialVersionUID = 1L;

    private final transient ThreadLocal<NumberFormat> numberFormat;

    public ThreadLocalNumberFormat(Supplier<NumberFormat> numberFormatSupplier) {
        this.numberFormat = ThreadLocal.withInitial(numberFormatSupplier);
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return numberFormat.get().format(number, toAppendTo, pos);
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        return numberFormat.get().format(number, toAppendTo, pos);
    }

    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        return numberFormat.get().parse(source, parsePosition);
    }
}
