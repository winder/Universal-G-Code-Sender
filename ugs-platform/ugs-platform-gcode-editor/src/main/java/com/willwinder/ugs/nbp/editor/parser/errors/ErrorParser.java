package com.willwinder.ugs.nbp.editor.parser.errors;

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import org.netbeans.api.lexer.Token;

import java.util.List;

public interface ErrorParser {
    void handleToken(Token<GcodeTokenId> token, int line);

    List<GcodeError> getErrors();
}
