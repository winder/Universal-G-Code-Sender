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
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.TinyGController;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.List;

public class InvalidG2CommandErrorParser implements ErrorParser {
    private final FileObject fileObject;
    private final BackendAPI backend;
    private final List<GcodeError> errorList = new ArrayList<>();

    public InvalidG2CommandErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (!(backend.isConnected() && backend.getController() instanceof TinyGController)) {
            return;
        }

        if (token.id().equals(GcodeTokenId.MACHINE) || token.id().equals(GcodeTokenId.MOVEMENT) || token.id().equals(GcodeTokenId.TOOL)) {
            // This is a temporary error because of the wierd state we end up in when using this
            if (Code.M0.name().equalsIgnoreCase(token.text().toString())) {
                int offset = token.offset(null);
                GcodeError error = new GcodeError("command-not-supported", "Command not supported", String.format("The command '%s' is not currently supported", token.text()), fileObject, offset, offset + token.length(), true, Severity.ERROR);
                errorList.add(error);
            }
        }
    }

    @Override
    public List<GcodeError> getErrors() {
        return errorList;
    }
}
