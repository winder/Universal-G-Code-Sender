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

import org.apache.commons.lang3.StringUtils;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;

/**
 * Syntax error scheduler task
 *
 * @author Joacim Breiler
 */
public class SyntaxErrorTask extends IndexingAwareParserResultTask<GcodeParserResult> {

    protected SyntaxErrorTask(TaskIndexingMode scanMode) {
        super(scanMode);
    }

    @Override
    public void run(GcodeParserResult result, SchedulerEvent event) {
        Document document = result.getSnapshot().getSource().getDocument(false);

        List<ErrorDescription> errors = new ArrayList<>();
        for (GcodeError error : result.getDiagnostics()) {

            try {
                Severity severity = convertSeverity(error);
                errors.add(ErrorDescriptionFactory.createErrorDescription(
                        severity,
                        StringUtils.defaultString(error.getDescription()),
                        document,
                        document.createPosition(error.getStartPosition()),
                        document.createPosition(error.getEndPosition())
                ));
            } catch (BadLocationException ex) {
            }

        }

        HintsController.setErrors(document, "Gcode", errors);
    }

    private Severity convertSeverity(GcodeError error) {
        switch (error.getSeverity()) {
            case INFO:
                return Severity.HINT;
            case ERROR:
            case FATAL:
                return Severity.ERROR;
            case WARNING:
                return Severity.WARNING;
            default:
                return Severity.HINT;
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }
}
