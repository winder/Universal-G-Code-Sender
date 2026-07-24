/*
    Copyright 2024 Will Winder

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

import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A lot of people experience problems when the gcode has G28 defined but have never set
 * the home position. This error parser will warn the user about this.
 *
 * @author Joacim Breiler
 */
public class ToolChangeErrorParser implements ErrorParser {
    private final FileObject fileObject;
    private final List<GcodeError> errorList = new ArrayList<>();
    private final BackendAPI backend;

    public ToolChangeErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;
        this.backend = LookupService.lookup(BackendAPI.class);
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (StringUtils.equalsIgnoreCase(token.text(), Code.M6.toString()) && !backend.getSettings().isHandleToolChanges()) {
            int offset = token.offset(null);
            GcodeError error = new GcodeError("m6-used", "Using M6", "The command 'M6' may not be supported by the controller.", fileObject, offset, offset + token.length(), true, Severity.INFO);
            errorList.add(error);
        }
    }

    @Override
    public List<GcodeError> getErrors() {
        return errorList;
    }
}
