package com.willwinder.ugs.nbp.editor.parser;

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * @author shannah
 */
@MimeRegistration(mimeType = GcodeLanguageConfig.MIME_TYPE, service = TaskFactory.class)
public class SyntaxErrorHighlightingTaskFactory extends TaskFactory {

    public SyntaxErrorHighlightingTaskFactory() {

    }

    @Override
    public Collection<? extends SchedulerTask> create(Snapshot snpsht) {
        return Collections.singleton(new SyntaxErrorTask());
    }

}