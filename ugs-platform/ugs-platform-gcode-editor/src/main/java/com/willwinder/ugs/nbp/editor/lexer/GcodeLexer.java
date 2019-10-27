/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.ugs.nbp.editor.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

import static org.apache.commons.lang3.CharUtils.isAsciiNumeric;

/**
 * A basic lexer for parsing a gcode file to tokens for describing the gcode elements.
 *
 * @author Joacim Breiler
 */
public class GcodeLexer implements Lexer<GcodeTokenId> {
    private LexerRestartInfo<GcodeTokenId> info;
    private LexerInput input;

    public GcodeLexer(LexerRestartInfo<GcodeTokenId> info) {
        this.info = info;
        this.input = info.input();
    }

    @Override
    public Token<GcodeTokenId> nextToken() {
        int character = input.read();
        switch (Character.toUpperCase(character)) {
            case '%':
                return parseStartOrEnd();

            case ';':
                return parseComment();

            case '(':
                return parseCommentSection();

            case 'A':
            case 'B':
            case 'C':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                return parseNumericField(GcodeTokenId.AXIS);

            case 'D':
            case 'E':
            case 'F':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
                return parseNumericField(GcodeTokenId.PARAMETER);

            case 'T':
                return parseCommand(GcodeTokenId.TOOL);

            case 'M':
                return parseCommand(GcodeTokenId.MACHINE);

            case 'N':
            case 'O':
                return parseCommand(GcodeTokenId.PROGRAM);

            case 'G':
                return parseCommand(GcodeTokenId.MOVEMENT);

            case LexerInput.EOF:
                return null;

            default:
                return parseWhitespace(character);
        }

    }

    private Token<GcodeTokenId> parseStartOrEnd() {
        while (true) {
            int character = input.read();

            if (character == LexerInput.EOF || character == '\r' || character == '\n') {
                input.backup(1);
                break;
            }
        }
        return createToken(GcodeTokenId.START_OR_END);
    }

    private Token<GcodeTokenId> parseWhitespace(int character) {
        if (character == '\n') {
            return createToken(GcodeTokenId.END_OF_LINE);
        } else if (Character.isWhitespace((char) character)) {
            character = input.read();
            while (character != LexerInput.EOF && Character.isWhitespace((char) character)) {
                character = input.read();
            }
            input.backup(1);
            return createToken(GcodeTokenId.WHITESPACE);
        } else {
            input.read();
            return createToken(GcodeTokenId.ERROR);
        }
    }

    private Token<GcodeTokenId> parseCommentSection() {
        int ch;
        while (true) {
            ch = input.read();

            if (ch == LexerInput.EOF || ch == '\r' || ch == '\n') {
                input.backup(1);
                return createToken(GcodeTokenId.ERROR);
            } else if (ch == ')') {
                break;
            }
        }
        return createToken(GcodeTokenId.COMMENT);
    }

    private Token<GcodeTokenId> parseComment() {
        int ch;
        while (true) {
            ch = input.read();

            if (ch == LexerInput.EOF || ch == '\r' || ch == '\n') {
                input.backup(1);
                break;
            }
        }
        return createToken(GcodeTokenId.COMMENT);
    }

    private Token<GcodeTokenId> parseNumericField(GcodeTokenId tokenId) {
        int length = 0;
        int minusCount = 0;
        int commaCount = 0;
        int numberCount = 0;

        while (true) {
            char character = (char) input.read();
            if (character == ' ' && length == 0) {
                // It's allowed to have a leading space after parameter name
            } else if (!isNumeric(character)) {
                input.backup(1);
                break;
            } else if (character == ',' || character == '.') {
                commaCount++;
            } else if (character == '-') {
                minusCount++;
            }

            if (isNumeric(character)) {
                numberCount++;
            }

            length++;
        }

        if (length == 0 || minusCount > 1 || commaCount > 1 || numberCount == 0) {
            return createToken(GcodeTokenId.ERROR);
        }

        return createToken(tokenId);
    }

    /**
     * Returns if the character is a part of a numeric string. That includes minus and commas.
     *
     * @param character the character to check
     * @return true if the character is a part of a numeric field.
     */
    private boolean isNumeric(char character) {
        return isAsciiNumeric(character) || character == '-' || character == '.' || character == ',';
    }

    private Token<GcodeTokenId> parseCommand(GcodeTokenId tokenId) {
        int length = 0;
        int commaCount = 0;
        while (true) {
            char character = (char) input.read();
            if (!(isAsciiNumeric(character) || character == '.')) {
                input.backup(1);
                break;
            }

            if (character == '.') {
                commaCount++;
            }
            length++;
        }

        if (length == 0 || commaCount > 1) {
            return createToken(GcodeTokenId.ERROR);
        }

        return createToken(tokenId);
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }

    private Token<GcodeTokenId> createToken(GcodeTokenId tokenId) {
        return info.tokenFactory().createToken(tokenId);
    }
}