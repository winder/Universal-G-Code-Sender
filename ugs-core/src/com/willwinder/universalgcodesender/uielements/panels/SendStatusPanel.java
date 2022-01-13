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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.model.events.FileState.FILE_LOADED;
import static com.willwinder.universalgcodesender.model.events.FileState.FILE_STREAM_COMPLETE;

/**
 * A send status panel for displaying the progress of a file stream
 *
 * @author wwinder
 */
public class SendStatusPanel extends JPanel implements UGSEventListener {
    private static final String AL_RIGHT = "al right";
    private static final Logger LOGGER = Logger.getLogger(SendStatusPanel.class.getSimpleName());
    private final BackendAPI backend;

    private final JLabel rowsLabel = new JLabel(Localization.getString("mainWindow.swing.rowsLabel"));
    private final JLabel sentRowsLabel = new JLabel(Localization.getString("mainWindow.swing.sentRowsLabel"));
    private final JLabel remainingRowsLabel = new JLabel(Localization.getString("mainWindow.swing.remainingRowsLabel"));
    private final JLabel remainingTimeLabel = new JLabel(Localization.getString("mainWindow.swing.remainingTimeLabel"));
    private final JLabel durationLabel = new JLabel(Localization.getString("mainWindow.swing.durationLabel"));
    private final JLabel latestCommentLabel = new JLabel(Localization.getString("mainWindow.swing.latestCommentLabel"));

    private final JLabel rowsValue = new JLabel();
    private final JLabel sentRowsValue = new JLabel();
    private final JLabel remainingRowsValue = new JLabel();
    private final JLabel remainingTimeValue = new JLabel();
    private final JLabel durationValue = new JLabel();
    private final JTextArea latestCommentValueLabel = new JTextArea();

    private Timer timer;

    public SendStatusPanel() {
        this(null);
    }

    public SendStatusPanel(BackendAPI b) {
        backend = b;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        initComponents();
        resetSentRowLabels();

        if (backend.isSendingFile()) {
            beginSend();
        }
    }

    private void update() {
        durationValue.setText(Utils.formattedMillis(backend.getSendDuration()));
        setRemainingTime(backend.getSendRemainingDuration());
        sentRowsValue.setText("" + backend.getNumCompletedRows());
        remainingRowsValue.setText("" + backend.getNumRemainingRows());
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
                            update();

                            // Stop the timer if we no longer is sending a file
                            if (!backend.isSendingFile() && timer != null && timer.isRunning()) {
                                endSend();
                                timer.stop();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        this.resetTimerLabels();

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(400, actionListener);

        // Note: there is a divide by zero error in the timer because it uses
        //       the rowsValueLabel that was just reset.

        try {
            timer.start();
        } catch (Exception e) {
            timer.stop();
            GUIHelpers.displayErrorDialog(e.getMessage());
        }
    }

    private void endSend() {
        setRemainingTime(Utils.formattedMillis(0));
        remainingRowsValue.setText("0");
        sentRowsValue.setText("" + backend.getNumCompletedRows());
    }

    private void resetTimerLabels() {
        // Reset labels
        this.durationValue.setText("00:00:00");
        if (this.backend != null && this.backend.isConnected()) {
            setRemainingTime(backend.getSendDuration());
        } else {
            setRemainingTime("--:--:--");
        }
    }

    private void setRemainingTime(long millis) {
        if (millis < 0) {
            setRemainingTime("estimating...");
        } else if (millis == 0) {
            setRemainingTime("--:--:--");
        } else {
            setRemainingTime(Utils.formattedMillis(millis));
        }
    }

    private void setRemainingTime(String text) {
        this.remainingTimeValue.setText(text);
    }

    public String getDuration() {
        return this.durationValue.getText();
    }

    private void resetSentRowLabels() {
        long numRows = 0;
        if (backend.getProcessedGcodeFile() != null) {
            try {
                try (IGcodeStreamReader gsr = new GcodeStreamReader(backend.getProcessedGcodeFile())) {
                    numRows = gsr.getNumRows();
                    LOGGER.fine("NUM ROWS: " + numRows);
                }
            } catch (GcodeStreamReader.NotGcodeStreamFile | IOException ex) {
            }
        }
        // Reset labels
        String totalRows = String.valueOf(numRows);
        resetTimerLabels();
        this.sentRowsValue.setText("0");
        this.remainingRowsValue.setText(totalRows);
        this.rowsValue.setText(totalRows);
    }

    private void initComponents() {
        // MigLayout... 3rd party layout library.
        setLayout(new MigLayout("fillx, wrap 2, inset 5", "grow"));
        add(rowsLabel, AL_RIGHT);
        add(rowsValue);
        add(sentRowsLabel, AL_RIGHT);
        add(sentRowsValue);
        add(remainingRowsLabel, AL_RIGHT);
        add(remainingRowsValue);
        add(remainingTimeLabel, AL_RIGHT);
        add(remainingTimeValue);
        add(durationLabel, AL_RIGHT);
        add(durationValue);
        add(latestCommentLabel, "span 2, wrap");
        add(latestCommentValueLabel, "growx, span 2, wrap, wmin 10");

        latestCommentValueLabel.setOpaque(false);
        latestCommentValueLabel.setWrapStyleWord(true);
        latestCommentValueLabel.setLineWrap(true);
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        // Look for a send beginning.
        if (evt instanceof ControllerStateEvent && ((ControllerStateEvent) evt).getState() == ControllerState.RUN) {
            if (backend.isSendingFile()) {
                beginSend();
            }
        }

        // On file loaded event, reset the rows.
        else if (evt instanceof FileStateEvent) {
            FileStateEvent fileStateEvent = (FileStateEvent) evt;
            if (fileStateEvent.getFileState() == FILE_LOADED) {
                resetSentRowLabels();
            } else if (fileStateEvent.getFileState() == FILE_STREAM_COMPLETE) {
                update();
                endSend();
            }
        } else if (evt instanceof CommandEvent) {
            CommandEvent commandEvent = ((CommandEvent) evt);
            GcodeCommand command = commandEvent.getCommand();
            if ((commandEvent.getCommandEventType() == CommandEventType.COMMAND_SENT ||
                    commandEvent.getCommandEventType() == CommandEventType.COMMAND_SKIPPED) &&
                    command.hasComment()) {
                latestCommentValueLabel.setText(command.getComment());
            }
        }
    }
}
