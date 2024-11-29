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

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import com.willwinder.universalgcodesender.gcode.util.Code;
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
public class ReturnToHomeGcodeErrorParser implements ErrorParser {
    private final FileObject fileObject;
    private final List<GcodeError> errorList = new ArrayList<>();

    public ReturnToHomeGcodeErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (GcodeTokenId.MOVEMENT.equals(token.id()) && StringUtils.equalsIgnoreCase(token.text(), Code.G28.toString())) {
            int offset = token.offset(null);
            GcodeError error = new GcodeError("g28-used", "Using G28", "The command 'G28' will return to machine to a the predefined machine zero. Make sure that you have set a safe machine zero with 'G28.1' before running the program.", fileObject, offset, offset + token.length(), true, Severity.INFO);
            errorList.add(error);
        }
    }

    @Override
    public List<GcodeError> getErrors() {
        return errorList;
    }
}
