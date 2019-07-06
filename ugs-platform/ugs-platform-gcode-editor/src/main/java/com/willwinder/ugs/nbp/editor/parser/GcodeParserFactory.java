package com.willwinder.ugs.nbp.editor.parser;

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;

import java.util.Collection;

/**
 * @author peter
 */
public class GcodeParserFactory extends ParserFactory {

    @Override
    public Parser createParser(Collection<Snapshot> snapshots) {
        return new GcodeParser();
    }
}