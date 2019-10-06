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
package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbp.editor.lexer.GcodeLanguageHierarcy;
import com.willwinder.ugs.nbp.editor.parser.GcodeParser;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Lookup;

@LanguageRegistration(mimeType = GcodeLanguageConfig.MIME_TYPE, useMultiview = true)
public class GcodeLanguageConfig extends DefaultLanguageConfig {
    public static final String MIME_TYPE = "text/xgcode";

    @Override
    public String getLineCommentPrefix() {
        return "; ";
    }

    @Override
    public Language getLexerLanguage() {
        return new GcodeLanguageHierarcy().language();
    }

    @Override
    public String getDisplayName() {
        return "Gcode";
    }

    @Override
    public Parser getParser() {
        return Lookup.getDefault().lookup(GcodeParser.class);
    }
}
