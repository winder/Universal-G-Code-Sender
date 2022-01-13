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
package com.willwinder.ugs.cli;

import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * Displays the file send progress as a progress bar
 *
 * @author Joacim Breiler
 */
public class ProgressBarPrinter implements UGSEventListener {
    private ProgressBar pb;
    private final BackendAPI backend;

    public ProgressBarPrinter(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof FileStateEvent) {
            FileStateEvent fileStateEvent = (FileStateEvent) event;
            if(fileStateEvent.getFileState() == FileState.FILE_LOADED) {
                pb = new ProgressBarBuilder()
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .setInitialMax(100)
                        .setTaskName(backend.getGcodeFile().getName())
                        .setPrintStream(System.out)
                        .build();
            } else if(fileStateEvent.getFileState() == FileState.FILE_STREAM_COMPLETE) {
                if (pb != null) {
                    pb.maxHint(backend.getNumRows());
                    pb.stepTo(backend.getNumCompletedRows());
                    pb.close();
                    pb = null;
                }
            }
        } else if (event instanceof ControllerStateEvent && pb != null) {
            ControllerStateEvent controllerStateEvent = (ControllerStateEvent) event;
            if (controllerStateEvent.getState() == ControllerState.HOLD) {
                pb.setExtraMessage("[PAUSED] 'ENTER' to resume");
            } else {
                pb.setExtraMessage("[" + controllerStateEvent.getState() + "]");
            }
        } else if (event instanceof CommandEvent) {
            if (pb != null) {
                pb.maxHint(backend.getNumRows());
                pb.stepTo(backend.getNumCompletedRows());
            }
        }
    }
}
