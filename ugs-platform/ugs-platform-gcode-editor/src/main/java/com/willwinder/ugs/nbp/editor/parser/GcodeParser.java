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

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.ugs.nbp.editor.parser.errors.ErrorParser;
import com.willwinder.ugs.nbp.editor.parser.errors.FeedRateMissingErrorParser;
import com.willwinder.ugs.nbp.editor.parser.errors.InvalidG2CommandErrorParser;
import com.willwinder.ugs.nbp.editor.parser.errors.InvalidGrblCommandErrorParser;
import com.willwinder.ugs.nbp.editor.parser.errors.MovementInMachineCoordinatesErrorParser;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A gcode parser that parses errors from gcode tokens
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = GcodeParser.class)
public class GcodeParser extends Parser {

    private int line;
    private TokenSequence<GcodeTokenId> tokenSequence;
    private GcodeParserResult parserResult;
    private List<ErrorParser> errorParserList;

    public GcodeParser() {
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sourceModificationEvent) {
        initialize(snapshot);

        while (tokenSequence.moveNext()) {
            Token<GcodeTokenId> token = tokenSequence.token();
            if (GcodeTokenId.END_OF_LINE.equals(token.id())) {
                line++;
            }

            errorParserList.forEach(errorParser -> errorParser.handleToken(token, line));
        }

        errorParserList.forEach(errorParser -> errorParser.getErrors().forEach(error -> parserResult.add(error)));
    }

    @SuppressWarnings("unchecked")
    private void initialize(Snapshot snapshot) {
        parserResult = new GcodeParserResult(snapshot);
        line = 0;
        FileObject fileObject = snapshot.getSource().getFileObject();
        errorParserList = new ArrayList<>();
        errorParserList.add(new FeedRateMissingErrorParser(fileObject));
        errorParserList.add(new InvalidGrblCommandErrorParser(fileObject));
        errorParserList.add(new MovementInMachineCoordinatesErrorParser(fileObject));
        errorParserList.add(new InvalidG2CommandErrorParser(fileObject));

        tokenSequence = (TokenSequence<GcodeTokenId>) snapshot.getTokenHierarchy().tokenSequence();
        tokenSequence.moveStart();
    }

    @Override
    public Result getResult(Task task) {
        return parserResult;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }
}
