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
import com.willwinder.universalgcodesender.gcode.util.Code;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.Collections;
import java.util.List;

public class UnitsMissingErrorParser implements ErrorParser {
    private final FileObject fileObject;
    private int firstUnitLine = 0;
    private int firstMovementLine = 0;
    private Token<?> firstMovementToken;
    private Token<?> firstUnitToken;

    public UnitsMissingErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (GcodeTokenId.MOVEMENT.equals(token.id())) {
            if (isMovementCommand(token) && firstMovementToken == null) {
                firstMovementToken = token;
                firstMovementLine = line;
            }

            if ((StringUtils.equalsIgnoreCase(token.text(), "G21") || StringUtils.equalsIgnoreCase(token.text(), "G20")) && firstUnitToken == null) {
                firstUnitToken = token;
                firstUnitLine = line;
            }
        }
    }

    private boolean isMovementCommand(Token<?> token) {
        return StringUtils.equalsIgnoreCase(token.text(), Code.G0.name()) ||
                StringUtils.equalsIgnoreCase(token.text(), "G00") ||
                StringUtils.equalsIgnoreCase(token.text(), Code.G1.name()) ||
                StringUtils.equalsIgnoreCase(token.text(), "G01") ||
                StringUtils.equalsIgnoreCase(token.text(), Code.G2.name()) ||
                StringUtils.equalsIgnoreCase(token.text(), "G02") ||
                StringUtils.equalsIgnoreCase(token.text(), Code.G3.name()) ||
                StringUtils.equalsIgnoreCase(token.text(), "G03");
    }

    @Override
    public List<GcodeError> getErrors() {
        if (firstUnitToken == null && firstMovementToken == null) {
            return Collections.emptyList();
        } else if (firstMovementToken != null && (firstUnitToken == null || firstMovementLine < firstUnitLine)) {
           int offset = firstMovementToken.offset(null);
            GcodeError error = new GcodeError("no-units", "No units defined", "No unit (G20/G21) has been assigned before movement command", fileObject, offset, offset + firstMovementToken.length(), true, Severity.ERROR);
            return Collections.singletonList(error);
        }

        return Collections.emptyList();
    }
}
