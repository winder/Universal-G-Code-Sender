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

    private JLabel rowsValue;
    private JLabel sentRowsValue;
    private JLabel remainingRowsValue;
    private JLabel remainingTimeValue;
    private JLabel durationValue;

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
                            remainingTimeValue.setText(Utils.formattedMillis(backend.getSendRemainingDuration()));

                            //sentRowsValueLabel.setText(""+sentRows);
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
        remainingTimeValue.setText(Utils.formattedMillis(0));
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
            if (this.backend.getSendDuration() < 0) {
                this.remainingTimeValue.setText("estimating...");
            } else if (this.backend.getSendDuration() == 0) {
                this.remainingTimeValue.setText("--:--:--");
            } else {
                this.remainingTimeValue.setText(Utils.formattedMillis(this.backend.getSendDuration()));
            }
        } else {
            this.remainingTimeValue.setText("--:--:--");
        }
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
        // Components
        JLabel rowsLabel = new JLabel(Localization.getString("mainWindow.swing.rowsLabel"));
        JLabel sentRowsLabel = new JLabel(Localization.getString("mainWindow.swing.sentRowsLabel"));
        JLabel remainingRowsLabel = new JLabel(Localization.getString("mainWindow.swing.remainingRowsLabel"));
        JLabel remainingTimeLabel = new JLabel(Localization.getString("mainWindow.swing.remainingTimeLabel"));
        JLabel durationLabel = new JLabel(Localization.getString("mainWindow.swing.durationLabel"));

        rowsValue = new JLabel();
        sentRowsValue = new JLabel();
        remainingRowsValue = new JLabel();
        remainingTimeValue = new JLabel();
        durationValue = new JLabel();

        /*
        // This didn't seem to be needed...
        int minSizeLeft = 0;
        minSizeLeft = Math.max(minSizeLeft, rowsLabel.getWidth());
        minSizeLeft = Math.max(minSizeLeft, sentRowsLabel.getWidth());
        minSizeLeft = Math.max(minSizeLeft, remainingRowsLabel.getWidth());
        minSizeLeft = Math.max(minSizeLeft, remainingTimeLabel.getWidth());
        minSizeLeft = Math.max(minSizeLeft, durationLabel.getWidth());

        int minSizeRight = 0;
        minSizeRight = Math.max(minSizeRight, rowsValue.getWidth());
        minSizeRight = Math.max(minSizeRight, sentRowsValue.getWidth());
        minSizeRight = Math.max(minSizeRight, remainingRowsValue.getWidth());
        minSizeRight = Math.max(minSizeRight, remainingTimeValue.getWidth());
        minSizeRight = Math.max(minSizeRight, durationValue.getWidth());

        int minSize = minSizeLeft + minSizeRight;
        */

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
