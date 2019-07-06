package com.willwinder.ugs.nbp.editor.parser;

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@ServiceProvider(service = GcodeParser.class)
public class GcodeParser extends Parser {
    private static final Logger LOGGER = Logger.getLogger(GcodeParser.class.getName());

    private GcodeParserResult parserResult;
    private Set<ChangeListener> changeListeners = new HashSet<>();

    public GcodeParser() {
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sourceModificationEvent) throws ParseException {
        parserResult = new GcodeParserResult(snapshot);
        FileObject fileObject = snapshot.getSource().getFileObject();
        try {
            fileObject.setAttribute("parser-parserResult", parserResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TokenSequence<?> tokenSequence = snapshot.getTokenHierarchy().tokenSequence();
        tokenSequence.moveStart();

        boolean hasFeedRate = false;
        boolean hasFeedRateError = false;
        Token<?> token;
        int line = 0;
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            if (GcodeTokenId.END_OF_LINE.equals(token.id())) {
                line++;
            } else if (GcodeTokenId.MOVEMENT.equals(token.id()) && !hasFeedRate && !hasFeedRateError) {
                hasFeedRateError = true;
                GcodeError error = new GcodeError("no-feed-rate", "No feed rate", "No feed rate has been assigned before movement command", fileObject, line, tokenSequence.offset(), tokenSequence.offset() + token.length(), true, Severity.ERROR);

                parserResult.add(error);
            }
        }

        notifyListeners(parserResult);
    }

    private void notifyListeners(GcodeParserResult parserResult) {
        changeListeners.forEach(changeListener -> {
            changeListener.stateChanged(new ChangeEvent(parserResult));
        });
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return parserResult;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        changeListeners.add(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        changeListeners.remove(cl);
    }
}
