package com.willwinder.ugs.nbp.editor.parser;

import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.openide.filesystems.FileObject;

public class GcodeError extends DefaultError {
    private final int line;

    public GcodeError(String key, String displayName, String description, FileObject file, int line, int start, int end, boolean lineError, Severity severity) {
        super(key, displayName, description, file, start, end, lineError, severity);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
