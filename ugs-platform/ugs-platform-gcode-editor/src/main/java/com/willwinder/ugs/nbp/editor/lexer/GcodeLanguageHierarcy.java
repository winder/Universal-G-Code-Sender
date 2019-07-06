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

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

import java.util.Collection;
import java.util.EnumSet;

/**
 * A language hierarcy for supplying gcode lexer and tokens
 *
 * @author Joacim Breiler
 */
public class GcodeLanguageHierarcy extends LanguageHierarchy<GcodeTokenId> {

    @Override
    protected Collection<GcodeTokenId> createTokenIds() {
        return EnumSet.allOf(GcodeTokenId.class);
    }

    @Override
    protected Lexer<GcodeTokenId> createLexer(LexerRestartInfo<GcodeTokenId> lexerRestartInfo) {
        return new GcodeLexer(lexerRestartInfo);
    }

    @Override
    protected String mimeType() {
        return GcodeLanguageConfig.MIME_TYPE;
    }
}
