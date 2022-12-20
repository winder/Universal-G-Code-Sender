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
package com.willwinder.ugs.nbp.editor.parser.errors;

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.List;

public class InvalidGcodeErrorParser implements ErrorParser {
    private final FileObject fileObject;
    private final List<GcodeError> errorList = new ArrayList<>();

    public InvalidGcodeErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (GcodeTokenId.ERROR.equals(token.id())) {
            int offset = token.offset(null);
            GcodeError error = new GcodeError("command-not-supported", "Command not supported", String.format("The command '%s' is not a valid GCode commands", token.text()), fileObject, offset, offset + token.length(), true, Severity.ERROR);
            errorList.add(error);
        }
    }

    @Override
    public List<GcodeError> getErrors() {
        return errorList;
    }
}
