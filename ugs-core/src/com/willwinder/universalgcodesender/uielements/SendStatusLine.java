/**
 * A component which should be embedded in a status bar.
 */
/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javax.swing.JPanel;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_SENDING;
import static com.willwinder.universalgcodesender.model.UGSEvent.FileState.FILE_LOADED;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 *
 * @author wwinder
 */
public class SendStatusLine extends JLabel implements UGSEventListener, ControllerListener {
    private static final String SEND_PREFIX = "Send Status: ";
    private static final String LOAD_PREFIX = "Loaded File: ";
    private static final String SEND_FORMAT = SEND_PREFIX + "(%d/%d) %s / %s ";
    private static final String COMPLETED_FORMAT = SEND_PREFIX + "completed after %s ";
    private static final String ROWS_FORMAT = LOAD_PREFIX + "%d rows ";

    private final BackendAPI backend;
    private Timer timer;

    public SendStatusLine(BackendAPI b) {
        backend = b;
        if (backend != null) {
            backend.addUGSEventListener(this);
            backend.addControllerListener(this);
        }

        if (backend.isSending()) {
            beginSend();
        } else {
            setRows();
        }
    }

    private void beginSend() {
        // Timer for updating duration labels.
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
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
                });
            }
        };

        if (timer != null){ timer.stop(); }
        timer = new Timer(1000, actionListener);

        try {
            timer.start();
        } catch (Exception e) {
            timer.stop();
            GUIHelpers.displayErrorDialog(e.getMessage());
        }
    }

    private void endSend() {
        timer.stop();
        setText(String.format(COMPLETED_FORMAT, Utils.formattedMillis(backend.getSendDuration())));
    }

    private void setRows() {
        if (backend.getProcessedGcodeFile() != null) {
            try {
                try (GcodeStreamReader gsr = new GcodeStreamReader(backend.getProcessedGcodeFile())) {
                    setText(String.format(ROWS_FORMAT, gsr.getNumRows()));
                }
            } catch (IOException ex){}
        }
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        // Start/Restart timer when sending starts.
        if (evt.isStateChangeEvent() && evt.getControlState() == COMM_SENDING) {
            beginSend();
        }

        // Display the number of rows when a file is loaded.
        if (evt.isFileChangeEvent() && evt.getFileState() == FILE_LOADED) {
            setRows();
        }
    }

    ///////////////////////////////
    // ControllerListener events //
    ///////////////////////////////

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        endSend();
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
    }

    @Override
    public void commandSent(GcodeCommand command) {
    }

    @Override
    public void commandComplete(GcodeCommand command) {
    }

    @Override
    public void commandComment(String comment) {
    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
    }

    @Override
    public void postProcessData(int numRows) {
    }
}
