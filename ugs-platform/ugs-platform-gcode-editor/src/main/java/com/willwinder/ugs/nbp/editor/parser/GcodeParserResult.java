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
package com.willwinder.ugs.nbp.editor.parser;

import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Gcode parser results holding all errors found
 *
 * @author Joacim Breiler
 */
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

    public void addAll(List<GcodeError> errors) {
        errorList.addAll(errors);
    }
}
