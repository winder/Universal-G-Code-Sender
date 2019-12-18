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

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 * A token for describing a gcode element type.
 *
 * @author Joacim Breiler
 */
public enum GcodeTokenId implements TokenId {
    COMMENT("comment"),
    MOVEMENT("movement"),
    MACHINE("machine"),
    TOOL("tool"),
    AXIS("axis"),
    PARAMETER("parameter"),
    WHITESPACE("whitespace"),
    ERROR("error"),
    PROGRAM("program"),
    START_OR_END("start or end"),
    END_OF_LINE("end of line");

    private final String primaryCategory;

    GcodeTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public static Language<GcodeTokenId> getLanguage() {
        return new GcodeLanguageHierarcy().language();
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
}
