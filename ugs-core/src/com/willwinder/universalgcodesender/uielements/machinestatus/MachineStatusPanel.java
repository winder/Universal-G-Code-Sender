package com.willwinder.universalgcodesender.uielements.machinestatus;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import net.miginfocom.swing.MigLayout;


import javax.swing.*;
import java.awt.*;

public class MachineStatusPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final JLabel activeStateLabel  = new JLabel("Active State:");
    private final JLabel activeStateValueLabel = new JLabel(" ");

    private final JLabel machinePositionLabel = new JLabel(Localization.getString("mainWindow.swing.machinePosition"));
    private final JLabel machinePositionXLabel = new JLabel("X:");
    private final JLabel machinePositionXValue = new JLabel("0");
    private final JLabel machinePositionYLabel = new JLabel("Y:");
    private final JLabel machinePositionYValue = new JLabel("0");
    private final JLabel machinePositionZLabel = new JLabel("Z:");
    private final JLabel machinePositionZValue = new JLabel("0");

    private final JLabel workPositionLabel = new JLabel(Localization.getString("mainWindow.swing.workPositionLabel"));
    private final JLabel workPositionXLabel = new JLabel("X:");
    private final JLabel workPositionXValue = new JLabel("0");
    private final JLabel workPositionYLabel = new JLabel("Y:");
    private final JLabel workPositionYValue = new JLabel("0");
    private final JLabel workPositionZLabel = new JLabel("Z:");
    private final JLabel workPositionZValue = new JLabel("0");

    private final JLabel latestCommentLabel = new JLabel(Localization.getString("mainWindow.swing.latestCommentLabel"));
    private final JLabel latestCommentValueLabel = new JLabel(" ");

    private final BackendAPI backend;

    public MachineStatusPanel() {
        this(null);
    }

    public MachineStatusPanel(BackendAPI backend) {
//        setBorder(BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.statusPanel")));
//        setMinimumSize(new java.awt.Dimension(247, 160));
//        setPreferredSize(new java.awt.Dimension(247, 160));
        activeStateLabel.setOpaque(true);
        activeStateValueLabel.setOpaque(true);

        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerListener(this);
        }

        initComponents();
    }


    private void initComponents() {
        // MigLayout... 3rd party layout library.
        MigLayout layout = new MigLayout("fill, wrap 4");
        setLayout(layout);
        add(activeStateLabel, "al right, span 2");
        add(activeStateValueLabel, "span 2");
        add(latestCommentLabel, "al right, span 2");
        add(latestCommentValueLabel, "span 2");
        add(workPositionLabel, "al right, span 2");
        add(machinePositionLabel, "span 2");
        add(workPositionXLabel, "al right");
        add(workPositionXValue);
        add(machinePositionXLabel, "al right");
        add(machinePositionXValue);
        add(workPositionYLabel, "al right");
        add(workPositionYValue);
        add(machinePositionYLabel, "al right");
        add(machinePositionYValue);
        add(workPositionZLabel, "al right");
        add(workPositionZValue);
        add(machinePositionZLabel, "al right");
        add(machinePositionZValue);
    }

    @Override
    public void doLayout() {
        super.doLayout();
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
    public void messageForConsole(MessageType type, String msg) {

    }

    @Override
    public void postProcessData(int numRows) {

    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
        this.activeStateValueLabel.setText( state );
        this.setStatusColorForState( state );

        if (machineCoord != null) {
            this.machinePositionXValue.setText(Utils.formatter.format(machineCoord.x) + machineCoord.getUnits().abbreviation);
            this.machinePositionYValue.setText(Utils.formatter.format(machineCoord.y) + machineCoord.getUnits().abbreviation);
            this.machinePositionZValue.setText(Utils.formatter.format(machineCoord.z) + machineCoord.getUnits().abbreviation);
        }

        if (workCoord != null) {
            this.workPositionXValue.setText(Utils.formatter.format(workCoord.x) + workCoord.getUnits().abbreviation);
            this.workPositionYValue.setText(Utils.formatter.format(workCoord.y) + workCoord.getUnits().abbreviation);
            this.workPositionZValue.setText(Utils.formatter.format(workCoord.z) + workCoord.getUnits().abbreviation);
        }
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
