package com.willwinder.universalgcodesender.uielements.machinestatus;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import javax.swing.*;
import java.awt.*;

public class MachineStatusPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final javax.swing.JLabel activeStateLabel  = new javax.swing.JLabel("Active State:");
    private final javax.swing.JLabel activeStateValueLabel = new javax.swing.JLabel(" ");

    private final javax.swing.JLabel machinePosition = new javax.swing.JLabel(Localization.getString("mainWindow.swing.machinePosition"));
    private final javax.swing.JLabel machinePositionXLabel = new javax.swing.JLabel("X:");
    private final javax.swing.JLabel machinePositionXValueLabel = new javax.swing.JLabel("0");
    private final javax.swing.JLabel machinePositionYLabel = new javax.swing.JLabel("Y:");
    private final javax.swing.JLabel machinePositionYValueLabel = new javax.swing.JLabel("0");
    private final javax.swing.JLabel machinePositionZLabel = new javax.swing.JLabel("Z:");
    private final javax.swing.JLabel machinePositionZValueLabel = new javax.swing.JLabel("0");

    private final javax.swing.JLabel workPositionLabel = new javax.swing.JLabel(Localization.getString("mainWindow.swing.workPositionLabel"));
    private final javax.swing.JLabel workPositionXLabel = new javax.swing.JLabel("X:");
    private final javax.swing.JLabel workPositionXValueLabel = new javax.swing.JLabel("0");
    private final javax.swing.JLabel workPositionYLabel = new javax.swing.JLabel("Y:");
    private final javax.swing.JLabel workPositionYValueLabel = new javax.swing.JLabel("0");
    private final javax.swing.JLabel workPositionZLabel = new javax.swing.JLabel("Z:");
    private final javax.swing.JLabel workPositionZValueLabel = new javax.swing.JLabel("0");

    private final javax.swing.JLabel latestCommentLabel = new javax.swing.JLabel(Localization.getString("mainWindow.swing.latestCommentLabel"));
    private final javax.swing.JLabel latestCommentValueLabel = new javax.swing.JLabel(" ");

    private BackendAPI backend;

    public MachineStatusPanel() {
        this(null);
    }

    public MachineStatusPanel(BackendAPI backend) {
        setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.statusPanel")));
        setMinimumSize(new java.awt.Dimension(247, 160));
        setPreferredSize(new java.awt.Dimension(247, 160));
        activeStateLabel.setOpaque(true);
        activeStateValueLabel.setOpaque(true);

        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerListener(this);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    private void updateControls() {

        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                this.setStatusColorForState("");
                break;
            case COMM_IDLE:
//                this.setStatusColorForState("");
                break;
            case COMM_SENDING:
                break;
            case COMM_SENDING_PAUSED:
                break;
            default:
        }
    }


    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {

    }

    @Override
    public void commandComment(String comment) {
        latestCommentValueLabel.setText(comment);
    }

    @Override
    public void commandComplete(GcodeCommand command) {

    }

    @Override
    public void messageForConsole(String msg, Boolean verbose) {

    }

    @Override
    public void postProcessData(int numRows) {

    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
        this.activeStateValueLabel.setText( state );
        this.setStatusColorForState( state );

        if (machineCoord != null) {
            this.machinePositionXValueLabel.setText( Utils.formatter.format(machineCoord.x) + machineCoord.getUnits().abbreviation );
            this.machinePositionYValueLabel.setText( Utils.formatter.format(machineCoord.y) + machineCoord.getUnits().abbreviation );
            this.machinePositionZValueLabel.setText( Utils.formatter.format(machineCoord.z) + machineCoord.getUnits().abbreviation );
        }

        if (workCoord != null) {
            this.workPositionXValueLabel.setText( Utils.formatter.format(workCoord.x) + workCoord.getUnits().abbreviation );
            this.workPositionYValueLabel.setText( Utils.formatter.format(workCoord.y) + workCoord.getUnits().abbreviation );
            this.workPositionZValueLabel.setText( Utils.formatter.format(workCoord.z) + workCoord.getUnits().abbreviation );
        }
    }

    @Override
    public void doLayout() {
        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(this);
        setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
                statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(statusPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(statusPanelLayout.createSequentialGroup()
                                                .add(latestCommentLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(latestCommentValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .add(statusPanelLayout.createSequentialGroup()
                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                .add(activeStateLabel)
                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                .add(activeStateValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                        .add(workPositionLabel)
                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                .add(17, 17, 17)
                                                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                                .add(workPositionZLabel)
                                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                .add(workPositionZValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                                .add(workPositionYLabel)
                                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                .add(workPositionYValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                                .add(workPositionXLabel)
                                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                .add(workPositionXValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                                        .add(machinePosition)
                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                .add(17, 17, 17)
                                                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                                .add(machinePositionZLabel)
                                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                .add(machinePositionZValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                                .add(machinePositionYLabel)
                                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                .add(machinePositionYValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                        .add(statusPanelLayout.createSequentialGroup()
                                                                                                .add(machinePositionXLabel)
                                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                                                .add(machinePositionXValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))))
                                                .add(0, 13, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
                statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(statusPanelLayout.createSequentialGroup()
                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(activeStateLabel)
                                        .add(activeStateValueLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(latestCommentLabel)
                                        .add(latestCommentValueLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                        .add(statusPanelLayout.createSequentialGroup()
                                                .add(workPositionLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                        .add(workPositionXLabel)
                                                        .add(workPositionXValueLabel))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                        .add(workPositionYLabel)
                                                        .add(workPositionYValueLabel))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                        .add(workPositionZLabel)
                                                        .add(workPositionZValueLabel)))
                                        .add(statusPanelLayout.createSequentialGroup()
                                                .add(machinePosition)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                        .add(machinePositionXLabel)
                                                        .add(machinePositionXValueLabel))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                        .add(machinePositionYLabel)
                                                        .add(machinePositionYValueLabel))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                        .add(machinePositionZLabel)
                                                        .add(machinePositionZValueLabel))))
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );


        super.doLayout();
    }

    private void setStatusColorForState(String state) {
        if (backend.getSettings().isDisplayStateColor()) {
            java.awt.Color color = null; // default to a transparent background.
            if (state.equals(Localization.getString("mainWindow.status.alarm"))) {
                color = Color.RED;
            } else if (state.equals(Localization.getString("mainWindow.status.hold"))) {
                color = Color.YELLOW;
            } else if (state.equals(Localization.getString("mainWindow.status.queue"))) {
                color = Color.YELLOW;
            } else if (state.equals(Localization.getString("mainWindow.status.run"))) {
                color = Color.GREEN;
            } else {
                color = Color.WHITE;
            }

            this.activeStateLabel.setBackground(color);
            this.activeStateValueLabel.setBackground(color);
        } else {
            this.activeStateLabel.setBackground(null);
            this.activeStateValueLabel.setBackground(null);
        }
    }





}
