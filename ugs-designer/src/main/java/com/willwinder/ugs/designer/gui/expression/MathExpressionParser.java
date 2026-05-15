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

import java.util.OptionalDouble;

/**
 * Tiny recursive-descent evaluator for arithmetic expressions a designer might
 * type into a position field: {@code 3/2}, {@code 7.2+1}, {@code (5+1)*2},
 * {@code -3+4}. Supports {@code + - * /} with standard precedence, unary minus,
 * and parentheses. Decimal numbers only — no scientific notation, no identifiers.
 *
 * Any input outside the strict whitelist returns an empty {@link OptionalDouble}
 * so callers can fall through to their own parsing without surprises.
 */
public final class MathExpressionParser {

    private MathExpressionParser() {
    }

    public static OptionalDouble tryEvaluate(String text) {
        if (text == null) {
            return OptionalDouble.empty();
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return OptionalDouble.empty();
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            boolean ok = (c >= '0' && c <= '9')
                    || c == '.' || c == '+' || c == '-' || c == '*' || c == '/'
                    || c == '(' || c == ')' || c == ' ';
            if (!ok) {
                return OptionalDouble.empty();
            }
        }
        try {
            Parser p = new Parser(trimmed);
            double value = p.parseExpr();
            p.skipWhitespace();
            if (!p.atEnd()) {
                return OptionalDouble.empty();
            }
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(value);
        } catch (ParseFailure e) {
            return OptionalDouble.empty();
        }
    }

    private static final class ParseFailure extends RuntimeException {
        ParseFailure() {
            super(null, null, false, false);
        }
    }

    private static final class Parser {
        private final String src;
        private int pos;

        Parser(String src) {
            this.src = src;
        }

        double parseExpr() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (peek('+') || peek('-')) {
                    char op = src.charAt(pos);
                    pos++;
                    skipWhitespace();
                    // Disallow doubled additive operators (e.g. "5++3", "5+-3")
                    // — these usually indicate a typo rather than intent.
                    if (peek('+') || peek('-')) {
                        throw new ParseFailure();
                    }
                    double rhs = parseTerm();
                    value = (op == '+') ? value + rhs : value - rhs;
                } else {
                    return value;
                }
            }
        }

        double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipWhitespace();
                if (peek('*')) {
                    pos++;
                    value *= parseFactor();
                } else if (peek('/')) {
                    pos++;
                    double divisor = parseFactor();
                    if (divisor == 0.0) {
                        throw new ParseFailure();
                    }
                    value /= divisor;
                } else {
                    return value;
                }
            }
        }

        double parseFactor() {
            skipWhitespace();
            // Allow a single leading unary +/- before a primary. Stacking ("--5",
            // "+-5") is rejected because it almost always indicates a typo.
            if (peek('+')) {
                pos++;
                return parsePrimary();
            }
            if (peek('-')) {
                pos++;
                return -parsePrimary();
            }
            return parsePrimary();
        }

        double parsePrimary() {
            skipWhitespace();
            if (atEnd()) {
                throw new ParseFailure();
            }
            if (peek('(')) {
                pos++;
                double value = parseExpr();
                skipWhitespace();
                if (!peek(')')) {
                    throw new ParseFailure();
                }
                pos++;
                return value;
            }
            return parseNumber();
        }

        double parseNumber() {
            int start = pos;
            boolean sawDigit = false;
            while (!atEnd() && src.charAt(pos) >= '0' && src.charAt(pos) <= '9') {
                pos++;
                sawDigit = true;
            }
            if (!atEnd() && src.charAt(pos) == '.') {
                pos++;
                while (!atEnd() && src.charAt(pos) >= '0' && src.charAt(pos) <= '9') {
                    pos++;
                    sawDigit = true;
                }
            }
            if (!sawDigit) {
                throw new ParseFailure();
            }
            try {
                return Double.parseDouble(src.substring(start, pos));
            } catch (NumberFormatException e) {
                throw new ParseFailure();
            }
        }

        void skipWhitespace() {
            while (!atEnd() && src.charAt(pos) == ' ') {
                pos++;
            }
        }

        boolean peek(char c) {
            return !atEnd() && src.charAt(pos) == c;
        }

        boolean atEnd() {
            return pos >= src.length();
        }
    }
}
