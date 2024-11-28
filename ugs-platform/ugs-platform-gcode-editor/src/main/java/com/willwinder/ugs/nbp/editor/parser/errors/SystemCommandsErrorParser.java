package com.willwinder.ugs.nbp.editor.parser.errors;

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.List;

public class SystemCommandsErrorParser implements ErrorParser {
    private final FileObject fileObject;
    private final List<GcodeError> errorList = new ArrayList<>();

    public SystemCommandsErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (GcodeTokenId.SYSTEM.equals(token.id())) {
            int offset = token.offset(null);
            GcodeError error = new GcodeError("system-command-in-gcode", "System commands should not be included in gcode", String.format("The command '%s' is a system command and should not be included in the gcode. These types of commands are generally not included in the controllers planner buffer and the order of the commands can not be guaranteed or will exhaust the life span of the controllers flash memory.", token.text()), fileObject, offset, offset + token.length(), true, Severity.ERROR);
            errorList.add(error);
        }
    }

    @Override
    public List<GcodeError> getErrors() {
        return errorList;
    }
}
