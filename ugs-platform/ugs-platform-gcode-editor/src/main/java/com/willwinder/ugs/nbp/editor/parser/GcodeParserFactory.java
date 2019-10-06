package com.willwinder.ugs.nbp.editor.parser;

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;

import java.util.Collection;

@MimeRegistration(mimeType = GcodeLanguageConfig.MIME_TYPE, service = ParserFactory.class)
public class GcodeParserFactory extends ParserFactory {

    @Override
    public Parser createParser(Collection<Snapshot> snapshots) {
        return new GcodeParser();
    }
}