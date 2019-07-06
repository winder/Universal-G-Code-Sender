package com.willwinder.ugs.nbp.editor.parser;

import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

public class SyntaxErrorTask extends ParserResultTask<Parser.Result> {
    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        System.out.println("Hello");
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
