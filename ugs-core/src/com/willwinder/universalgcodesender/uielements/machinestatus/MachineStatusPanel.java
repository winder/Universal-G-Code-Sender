/**
 * DRO style display panel with current controller state and most recent gcode
 * comment.
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
package com.willwinder.universalgcodesender.uielements.machinestatus;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.text.DecimalFormat;

public class MachineStatusPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final JLabel activeStateLabel  = new JLabel("Active State:");
    private final JLabel activeStateValueLabel = new JLabel(" ");

    private final JLabel machinePositionLabel = new JLabel(Localization.getString("mainWindow.swing.machinePosition"));
    private final JLabel machinePositionXLabel = new JLabel("X:");
    private final JLabel machinePositionXValue = new JLabel("0.00");
    private final JLabel machinePositionYLabel = new JLabel("Y:");
    private final JLabel machinePositionYValue = new JLabel("0.00");
    private final JLabel machinePositionZLabel = new JLabel("Z:");
    private final JLabel machinePositionZValue = new JLabel("0.00");

    private final JLabel workPositionLabel = new JLabel(Localization.getString("mainWindow.swing.workPositionLabel"));
    private final JLabel workPositionXLabel = new JLabel("X:");
    private final JLabel workPositionXValue = new JLabel("0.00");
    private final JLabel workPositionYLabel = new JLabel("Y:");
    private final JLabel workPositionYValue = new JLabel("0.00");
    private final JLabel workPositionZLabel = new JLabel("Z:");
    private final JLabel workPositionZValue = new JLabel("0.00");

    private final JLabel latestCommentLabel = new JLabel(Localization.getString("mainWindow.swing.latestCommentLabel"));
    private final JLabel latestCommentValueLabel = new JLabel(" ");

    private final BackendAPI backend;
    
    public Units units;
    public DecimalFormat decimalFormatter;

    /**
     * No-Arg constructor to make this control work in the UI builder tools
     * @deprecated Use constructor with BackendAPI.
     */
    @Deprecated
    public MachineStatusPanel() {
        this(null);
    }

    public MachineStatusPanel(BackendAPI backend) {
        activeStateLabel.setOpaque(true);
        activeStateValueLabel.setOpaque(true);

        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerListener(this);
        }

        applyFont();
        initComponents();
    }


    private void applyFont() {
        String fontPath="/resources/";
        String fontName="LED.ttf";
        InputStream is = getClass().getResourceAsStream(fontPath+fontName);
        Font font;
        
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, is);
            font = font.deriveFont(Font.PLAIN,28);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(fontName + " not loaded.  Using serif font.");
            font = new Font("serif", Font.PLAIN, 24);
        }
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);
        
        this.machinePositionXValue.setFont(font);
        this.machinePositionYValue.setFont(font);
        this.machinePositionZValue.setFont(font);
        
        this.workPositionXValue.setFont(font);
        this.workPositionYValue.setFont(font);
        this.workPositionZValue.setFont(font);
    }

    private void initComponents() {
        // MigLayout... 3rd party layout library.
        MigLayout layout = new MigLayout("fill, wrap 2");
        setLayout(layout);
        add(activeStateLabel, "al right");
        add(activeStateValueLabel);
        add(latestCommentLabel, "al right");
        add(latestCommentValueLabel);

        // Subpanels for work/machine read outs.
        JPanel workPanel = new JPanel();
        workPanel.setBackground(Color.LIGHT_GRAY);
        workPanel.setLayout(new MigLayout("fillx, wrap 2, inset 8"));
        workPanel.add(workPositionLabel, "span 2, wrap");
        workPanel.add(workPositionXLabel, "al right");
        workPanel.add(workPositionXValue, "al right");
        workPanel.add(workPositionYLabel, "al right");
        workPanel.add(workPositionYValue, "al right");
        workPanel.add(workPositionZLabel, "al right");
        workPanel.add(workPositionZValue, "al right");

        JPanel machinePanel = new JPanel();
        machinePanel.setLayout(new MigLayout("fillx, wrap 2, inset 8"));
        machinePanel.setBackground(Color.LIGHT_GRAY);
        machinePanel.add(machinePositionLabel, "span 2");
        machinePanel.add(machinePositionXLabel, "al right");
        machinePanel.add(machinePositionXValue, "al right");
        machinePanel.add(machinePositionYLabel, "al right");
        machinePanel.add(machinePositionYValue, "al right");
        machinePanel.add(machinePositionZLabel, "al right");
        machinePanel.add(machinePositionZValue, "al right");
        add(workPanel,"growx");
        add(machinePanel, "growx");
        
        if (this.backend.getSettings().getDefaultUnits().equals("mm")) {
            setUnits(Units.MM);
        } else {
            setUnits(Units.INCH);
        }
    }

    private void setUnits(Units u) {
        if (u == null || units == u) return;
        units = u;
        switch(u) {
            case MM:
                this.decimalFormatter = new DecimalFormat("0.00");
                break;
            case INCH:
                this.decimalFormatter = new DecimalFormat("0.000");
                break;
            default:
                units = Units.MM;
                this.decimalFormatter = new DecimalFormat("0.00");
                break;
        }
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
            this.setUnits(machineCoord.getUnits());

            this.setPostionValueColor(this.machinePositionXValue, machineCoord.x);
            this.machinePositionXValue.setText(decimalFormatter.format(machineCoord.x));
            
            this.setPostionValueColor(this.machinePositionYValue, machineCoord.y);
            this.machinePositionYValue.setText(decimalFormatter.format(machineCoord.y));
            
            this.setPostionValueColor(this.machinePositionZValue, machineCoord.z);
            this.machinePositionZValue.setText(decimalFormatter.format(machineCoord.z));
        }

        if (workCoord != null) {
            this.setUnits(workCoord.getUnits());

            this.setPostionValueColor(this.workPositionXValue, workCoord.x);
            this.workPositionXValue.setText(decimalFormatter.format(workCoord.x));
            
            this.setPostionValueColor(this.workPositionYValue, workCoord.y);
            this.workPositionYValue.setText(decimalFormatter.format(workCoord.y));
            
            this.setPostionValueColor(this.workPositionZValue, workCoord.z);
            this.workPositionZValue.setText(decimalFormatter.format(workCoord.z));
        }
    }
    
    private void setPostionValueColor(JLabel label, double newValue) {
       if (!label.getText().equals(decimalFormatter.format(newValue))) {
                label.setForeground(Color.red);
            } else {
                label.setForeground(Color.black);
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
