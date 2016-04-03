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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class SendStatusPanel extends JPanel implements UGSEventListener, ControllerListener {
    private final BackendAPI backend;

    private final JLabel rowsLabel = new JLabel(Localization.getString("mainWindow.swing.rowsLabel"));
    private final JLabel sentRowsLabel = new JLabel(Localization.getString("mainWindow.swing.sentRowsLabel"));
    private final JLabel remainingRowsLabel = new JLabel(Localization.getString("mainWindow.swing.remainingRowsLabel"));
    private final JLabel remainingTimeLabel = new JLabel(Localization.getString("mainWindow.swing.remainingTimeLabel"));
    private final JLabel durationLabel = new JLabel(Localization.getString("mainWindow.swing.durationLabel"));

    private final JLabel rowsValue = new JLabel();
    private final JLabel sentRowsValue = new JLabel();
    private final JLabel remainingRowsValue = new JLabel();
    private final JLabel remainingTimeValue = new JLabel();
    private final JLabel durationValue = new JLabel();

    Timer timer;

    public SendStatusPanel() {
        this(null);
    }
    
    public SendStatusPanel(BackendAPI b) {
        backend = b;
        if (backend != null) {
            backend.addUGSEventListener(this);
            backend.addControllerListener(this);
        }
        initComponents();
        resetSentRowLabels(0);
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
                            durationValue.setText(Utils.formattedMillis(backend.getSendDuration()));
                            setRemainingTime(backend.getSendRemainingDuration());
                            sentRowsValue.setText(""+backend.getNumSentRows());
                            remainingRowsValue.setText("" + backend.getNumRemainingRows());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        this.resetTimerLabels();

        if (timer != null){ timer.stop(); }
        timer = new Timer(1000, actionListener);

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
        remainingRowsValue.setText("" + backend.getNumRemainingRows());

        java.awt.EventQueue.invokeLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}

            // Stop the timer after a delay to make sure it is updated.
            timer.stop();
        });
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

    private void resetSentRowLabels(long numRows) {
        // Reset labels
        String totalRows =  String.valueOf(numRows);
        resetTimerLabels();
        this.sentRowsValue.setText("0");
        this.remainingRowsValue.setText(totalRows);
        this.rowsValue.setText(totalRows);
    }

    private void initComponents() {
        // MigLayout... 3rd party layout library.
        setLayout(new MigLayout("fill, wrap 2"));
        add(rowsLabel, "al right");
        add(rowsValue);
        add(sentRowsLabel, "al right");
        add(sentRowsValue);
        add(remainingRowsLabel, "al right");
        add(remainingRowsValue);
        add(remainingTimeLabel, "al right");
        add(remainingTimeValue);
        add(durationLabel, "al right");
        add(durationValue);
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        // Look for a send beginning.
        if (evt.isStateChangeEvent()) {
            switch (evt.getControlState()) {
                case COMM_SENDING:
                    beginSend();
                    break;
                default:
                    break;
            }
        }

        // On file event, reset the rows.
        if (evt.isFileChangeEvent()) {
            switch(evt.getFileState()) {
                case FILE_LOADING:
                    break;
                case FILE_LOADED:
                    try {
                        try (GcodeStreamReader gsr = new GcodeStreamReader(backend.getProcessedGcodeFile())) {
                            resetSentRowLabels(gsr.getNumRows());
                        }
                    } catch (IOException ex) {}
                    break;
                default:
                    break;
            }
        }
    }

    // Controller events below.

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
    public void messageForConsole(String msg, Boolean verbose) {
    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
    }

    @Override
    public void postProcessData(int numRows) {
    }
}
