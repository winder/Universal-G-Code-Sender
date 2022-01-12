/*
    Copyright 2020 Will Winder

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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.gcode.processors.RunFromProcessor;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * A service that will handle skipping to given line numbers in a loaded gcode program. When this service is created it
 * will load a gcode processor to the backend which can be used to skip lines in the currently loaded program.
 *
 * @author Joacim Breiler
 */
public class RunFromService implements UGSEventListener {
    private final BackendAPI backend;
    private final Set<RunFromServiceListener> listeners = new HashSet<>();
    private RunFromProcessor runFromProcessor = new RunFromProcessor(0);

    public RunFromService(BackendAPI backend) {
        this.backend = backend;
        try {
            this.backend.applyCommandProcessor(runFromProcessor);
        } catch (Exception e) {
            // Never mind this
        }
        this.backend.addUGSEventListener(this);
    }

    public void runFromLine(int lineNumber) {
        try {
            this.runFromProcessor.setLineNumber(lineNumber);
            this.backend.applyCommandProcessor(runFromProcessor);
            listeners.forEach(listener -> listener.runFromLineChanged(lineNumber));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(RunFromServiceListener runFromServiceListener) {
        listeners.add(runFromServiceListener);
    }

    public void removeListener(RunFromServiceListener runFromServiceListener) {
        listeners.removeIf(l -> l == runFromServiceListener);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {

        if (evt instanceof FileStateEvent && ((FileStateEvent)evt).getFileState() == FileState.OPENING_FILE) {
            runFromProcessor.setLineNumber(0);
        }
    }

    public interface RunFromServiceListener {
        void runFromLineChanged(int line);
    }
}
