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
