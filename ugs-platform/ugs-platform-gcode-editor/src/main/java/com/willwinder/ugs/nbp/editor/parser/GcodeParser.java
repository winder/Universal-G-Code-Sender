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
import com.willwinder.ugs.nbp.editor.parser.errors.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A gcode parser that parses errors from gcode tokens
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = GcodeParser.class)
public class GcodeParser extends Parser {



    /**
     * A support object for notifying listeners about that we need to reparse the document
     */
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private List<GcodeError> errors;
    private Snapshot snapshot;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sourceModificationEvent) {
        this.snapshot = snapshot;

        FileObject fileObject = snapshot.getSource().getFileObject();
        List<ErrorParser> errorParserList = new ArrayList<>();
        errorParserList.add(new FeedRateMissingErrorParser(fileObject));
        errorParserList.add(new InvalidGrblCommandErrorParser(fileObject));
        errorParserList.add(new MovementInMachineCoordinatesErrorParser(fileObject));
        errorParserList.add(new InvalidG2CommandErrorParser(fileObject));

        TokenSequence<?> tokenSequence = snapshot.getTokenHierarchy().tokenSequence();
        tokenSequence.moveStart();

        int line = 1; // The snapshot starts on line 1
        while (tokenSequence.moveNext()) {
            Token<?> token = tokenSequence.token();
            if (GcodeTokenId.END_OF_LINE.equals(token.id())) {
                line++;
            }

            final int currentLine = line;
            errorParserList.forEach(errorParser -> errorParser.handleToken(token, currentLine));
        }

        this.errors = errorParserList.stream()
                .flatMap(ep -> ep.getErrors().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Result getResult(Task task) {
        if (task instanceof SyntaxErrorTask) {
            GcodeParserResult parserResult = new GcodeParserResult(snapshot);
            parserResult.addAll(errors);
            return parserResult;
        }
        return null;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        changeSupport.addChangeListener(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        changeSupport.removeChangeListener(cl);
    }
}
