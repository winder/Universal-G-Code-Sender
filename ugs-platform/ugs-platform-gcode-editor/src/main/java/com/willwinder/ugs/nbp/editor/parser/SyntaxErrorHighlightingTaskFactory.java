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

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;

import java.util.Collection;
import java.util.Collections;

/**
 * A factory for creating a syntax error highlighter
 *
 * @author Joacim Breiler
 */
@MimeRegistration(mimeType = GcodeLanguageConfig.MIME_TYPE, service = TaskFactory.class)
public class SyntaxErrorHighlightingTaskFactory extends TaskFactory {

    public SyntaxErrorHighlightingTaskFactory() { }

    @Override
    public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
        return Collections.singleton(new SyntaxErrorTask(TaskIndexingMode.ALLOWED_DURING_SCAN));
    }
}