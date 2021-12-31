/*
    Copyright 2016-2018 Will Winder

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
package com.willwinder.universalgcodesender.uielements.toolbars;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import static com.willwinder.universalgcodesender.model.events.FileState.FILE_LOADED;
import static com.willwinder.universalgcodesender.model.events.FileState.FILE_STREAM_COMPLETE;

/**
 * A component which should be embedded in a status bar.
 *
 * @author wwinder
 */
public class SendStatusLine extends JLabel implements UGSEventListener {
    private static final String SEND_PREFIX = "Send Status: ";
    private static final String LOAD_PREFIX = "Loaded File: ";
    private static final String NO_FILE_LOADED = "No file loaded";
    private static final String SEND_FORMAT = SEND_PREFIX + "(%d/%d) %s / %s";
    private static final String COMPLETED_FORMAT = SEND_PREFIX + "completed after %s";
    private static final String ROWS_FORMAT = LOAD_PREFIX + "%d rows";

    private final BackendAPI backend;
    private Timer timer;

    public SendStatusLine(BackendAPI b) {
        backend = b;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        if (backend.isSendingFile()) {
            beginSend();
        } else {
            setRows();
        }
    }

    private void beginSend() {
        updateStatusText();
        if (isUpdateTimerRunning()) {
            timer.stop();
        }

        // Timer for updating duration labels.
        ActionListener actionListener = actionEvent -> updateStatusText();
        timer = new Timer(1000, actionListener);

        try {
            timer.start();
        } catch (Exception e) {
            timer.stop();
            GUIHelpers.displayErrorDialog(e.getMessage());
        }
    }

    private boolean isUpdateTimerRunning() {
        return timer != null && timer.isRunning();
    }

    private void updateStatusText() {
        try {
            setText(String.format(SEND_FORMAT,
                    backend.getNumSentRows(),
                    backend.getNumRows(),
                    Utils.formattedMillis(backend.getSendDuration()),
                    Utils.formattedMillis(backend.getSendRemainingDuration())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endSend() {
        if (isUpdateTimerRunning()) {
            timer.stop();
            setText(String.format(COMPLETED_FORMAT, Utils.formattedMillis(backend.getSendDuration())));
        }
    }

    private void setRows() {
        if (backend.getProcessedGcodeFile() != null) {
            try {
                try (IGcodeStreamReader gsr = new GcodeStreamReader(backend.getProcessedGcodeFile())) {
                    setText(String.format(ROWS_FORMAT, gsr.getNumRows()));
                }
            } catch (GcodeStreamReader.NotGcodeStreamFile | IOException ex) {
            }
        } else {
            setText(NO_FILE_LOADED);
        }

    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent) {
            if (!isUpdateTimerRunning() && backend.isSendingFile()) {
                beginSend();
            } else if (isUpdateTimerRunning() && !backend.isSendingFile()) {
                endSend();
            }
        }

        // Display the number of rows when a file is loaded.
        if (evt instanceof FileStateEvent) {
            FileStateEvent fileStateEvent = (FileStateEvent) evt;
            if (fileStateEvent.getFileState() == FILE_LOADED) {
                setRows();
            } else if (fileStateEvent.getFileState() == FILE_STREAM_COMPLETE) {
                endSend();
            }
        }
    }
}
