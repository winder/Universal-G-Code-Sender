package com.willwinder.ugs.nbp.editor.parser;

import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

import java.util.ArrayList;
import java.util.List;

public class GcodeParserResult extends ParserResult {

    private final List<GcodeError> errorList;

    public GcodeParserResult(Snapshot snapshot) {
        super(snapshot);
        errorList = new ArrayList<>();
    }

    public void add(GcodeError error) {
        errorList.add(error);
    }

    @Override
    public List<GcodeError> getDiagnostics() {
        return errorList;
    }

    @Override
    protected void invalidate() {
        errorList.clear();
    }
}
