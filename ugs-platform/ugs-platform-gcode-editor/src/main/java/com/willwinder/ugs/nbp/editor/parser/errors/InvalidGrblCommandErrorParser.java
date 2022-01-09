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
import com.willwinder.universalgcodesender.GrblController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple parser that checks that the parsed commands are supported by the controller.
 *
 * @author Joacim Breiler
 */
public class InvalidGrblCommandErrorParser implements ErrorParser {

    private static final List<String> VALID_COMMANDS_LIST = Arrays.asList(
            "G38.2", "G38.3", "G38.4", "G38.5", "G80", "G54", "G55", "G56",
            "G57", "G58", "G59", "G17", "G18", "G19", "G90", "G91", "G91.1",
            "G93", "G94", "G20", "G21", "G40", "G43.1", "G49", "M30", "G10",
            "G10", "L20", "G28", "G30", "G28.1", "G30.1", "G53", "G92", "G92.1",
            "G61"
    );
    private static final Pattern VALID_PATTERN = Pattern.compile("G0?[0-4]|L0?2|M0?[0-5]|M0?[7-9]", Pattern.CASE_INSENSITIVE);
    private final FileObject fileObject;
    private final BackendAPI backend;
    private final List<GcodeError> errorList = new ArrayList<>();

    public InvalidGrblCommandErrorParser(FileObject fileObject) {
        this(fileObject, CentralLookup.getDefault().lookup(BackendAPI.class));
    }

    public InvalidGrblCommandErrorParser(FileObject fileObject, BackendAPI backend) {
        this.fileObject = fileObject;
        this.backend = backend;
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (!(backend.isConnected() && backend.getController() instanceof GrblController)) {
            return;
        }

        if (!token.id().equals(GcodeTokenId.MACHINE) && !token.id().equals(GcodeTokenId.MOVEMENT)) {
            return;
        }

        boolean isValidCommand = VALID_COMMANDS_LIST.stream().anyMatch(c -> c.equalsIgnoreCase(token.text().toString()));
        boolean isValidPattern = VALID_PATTERN.matcher(token.text()).matches();
        if (isValidCommand || isValidPattern) {
            return;
        }

        int offset = token.offset(null);
        GcodeError error = new GcodeError("command-not-supported", "Command not supported", String.format("The command '%s' might not be supported by GRBL", token.text()), fileObject, offset, offset + token.length(), true, Severity.WARNING);
        errorList.add(error);
    }

    @Override
    public List<GcodeError> getErrors() {
        return errorList;
    }
}
