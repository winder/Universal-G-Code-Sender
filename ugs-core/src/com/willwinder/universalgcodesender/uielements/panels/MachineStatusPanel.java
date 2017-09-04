/**
 * DRO style display panel with current controller state and most recent gcode
 * comment.
 */
/*
    Copyright 2016-2017 Will Winder

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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.EnabledPins;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.text.DecimalFormat;
import org.apache.commons.lang3.StringUtils;

public class MachineStatusPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final JLabel activeStateLabel  = new JLabel(Localization.getString("mainWindow.swing.activeStateLabel"));
    private final JLabel activeStateValueLabel = new JLabel(" ");

    //private final JLabel machinePositionLabel = new JLabel(Localization.getString("mainWindow.swing.machinePosition"));
    //private final JLabel machinePositionXLabel = new JLabel("X:");
    private final JLabel machinePositionXValue = new JLabel("0.00");
    //private final JLabel machinePositionYLabel = new JLabel("Y:");
    private final JLabel machinePositionYValue = new JLabel("0.00");
    //private final JLabel machinePositionZLabel = new JLabel("Z:");
    private final JLabel machinePositionZValue = new JLabel("0.00");

    //private final JLabel workPositionLabel = new JLabel(Localization.getString("mainWindow.swing.workPositionLabel"));
    private final JLabel workPositionXLabel = new JLabel("X:");
    private final JLabel workPositionXValue = new JLabel("0.00");
    private final JLabel workPositionYLabel = new JLabel("Y:");
    private final JLabel workPositionYValue = new JLabel("0.00");
    private final JLabel workPositionZLabel = new JLabel("Z:");
    private final JLabel workPositionZValue = new JLabel("0.00");

    private final JLabel latestCommentLabel = new JLabel(Localization.getString("mainWindow.swing.latestCommentLabel"));
    private final JLabel latestCommentValueLabel = new JLabel(" ");

    // Enabled pin reporting
    private final JPanel pinStatusPanel = new JPanel();
    private final JCheckBox pinX = new JCheckBox(Localization.getString("machineStatus.pin.x"));
    private final JCheckBox pinY = new JCheckBox(Localization.getString("machineStatus.pin.y"));
    private final JCheckBox pinZ = new JCheckBox(Localization.getString("machineStatus.pin.z"));
    private final JCheckBox pinProbe = new JCheckBox(Localization.getString("machineStatus.pin.probe"));
    private final JCheckBox pinDoor = new JCheckBox(Localization.getString("machineStatus.pin.door"));
    private final JCheckBox pinHold = new JCheckBox(Localization.getString("machineStatus.pin.hold"));
    private final JCheckBox pinSoftReset = new JCheckBox(Localization.getString("machineStatus.pin.softReset"));
    private final JCheckBox pinCycleStart = new JCheckBox(Localization.getString("machineStatus.pin.cycleStart"));

    // Reset individual coordinate buttons.
    private final JButton resetXButton = new JButton(Localization.getString("mainWindow.swing.reset"));
    private final JButton resetYButton = new JButton(Localization.getString("mainWindow.swing.reset"));
    private final JButton resetZButton = new JButton(Localization.getString("mainWindow.swing.reset"));

    private final BackendAPI backend;
    
    public Units units;
    public DecimalFormat decimalFormatter;

    // Don't add the pin status panel until we get a pin status update.
    private boolean addedPinStatusPanel = false;

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

        if (this.backend.getSettings().getDefaultUnits().equals(Units.MM.abbreviation)) {
            setUnits(Units.MM);
        } else {
            setUnits(Units.INCH);
        }

        updateControls();
    }


    private void applyFont() {
        String fontPath="/resources/";
        String fontName="LED.ttf";
        InputStream is = getClass().getResourceAsStream(fontPath+fontName);
        Font font;
        Font big, small;
        
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, is);
            big = font.deriveFont(Font.PLAIN,30);
            small = font.deriveFont(Font.PLAIN,18);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(fontName + " not loaded.  Using serif font.");
            big = new Font("serif", Font.PLAIN, 24);
            small = new Font("serif", Font.PLAIN, 17);
        }
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(big);
        ge.registerFont(small);

        this.machinePositionXValue.setFont(small);
        this.machinePositionXValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        this.machinePositionYValue.setFont(small);
        this.machinePositionYValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        this.machinePositionZValue.setFont(small);
        this.machinePositionZValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         
        this.workPositionXValue.setFont(big);
        this.workPositionXValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        this.workPositionYValue.setFont(big);
        this.workPositionYValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        this.workPositionZValue.setFont(big);
        this.workPositionZValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    }

    private void initComponents() {
        // Hookup the reset buttons.
        resetXButton.addActionListener(ae -> resetCoordinateButton('X'));
        resetYButton.addActionListener(ae -> resetCoordinateButton('Y'));
        resetZButton.addActionListener(ae -> resetCoordinateButton('Z'));

        String debug = "";
        //String debug = "debug, ";
        // MigLayout... 3rd party layout library.
        MigLayout layout = new MigLayout(debug + "fill, wrap 2");
        setLayout(layout);
        add(activeStateLabel, "al right");
        add(activeStateValueLabel);
        add(latestCommentLabel, "al right");
        add(latestCommentValueLabel);

        // Subpanels for work/machine read outs.
        JPanel workPanel = new JPanel();
        workPanel.setBackground(Color.LIGHT_GRAY);
        workPanel.setLayout(new MigLayout(debug + "fillx, wrap 3, inset 8", "[left][right][grow, right]"));
        //workPanel.add(workPositionLabel, "span 2, wrap");
        workPanel.add(resetXButton);
        workPanel.add(workPositionXLabel, "al right");
        workPanel.add(workPositionXValue, "growx, bottom");
        workPanel.add(machinePositionXValue, "span 3, al right, wrap");
        workPanel.add(resetYButton);
        workPanel.add(workPositionYLabel, "al right");
        workPanel.add(workPositionYValue, "growx, bottom");
        workPanel.add(machinePositionYValue, "span 3, al right, wrap");
        workPanel.add(resetZButton);
        workPanel.add(workPositionZLabel, "al right");
        workPanel.add(workPositionZValue, "growx, bottom");
        workPanel.add(machinePositionZValue, "span 3, al right, wrap");
        add(workPanel,"growx, span 2");

        // Enabled pin reporting.
        pinStatusPanel.setLayout(new MigLayout("flowy, wrap 3"));
        pinStatusPanel.add(pinX);
        pinX.setEnabled(false);
        pinStatusPanel.add(pinY);
        pinY.setEnabled(false);
        pinStatusPanel.add(pinZ);
        pinZ.setEnabled(false);
        pinStatusPanel.add(pinProbe);
        pinProbe.setEnabled(false);
        pinStatusPanel.add(pinDoor);
        pinDoor.setEnabled(false);
        pinStatusPanel.add(pinHold);
        pinHold.setEnabled(false);
        pinStatusPanel.add(pinSoftReset);
        pinSoftReset.setEnabled(false);
        pinStatusPanel.add(pinCycleStart);
        pinCycleStart.setEnabled(false);
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

    private void updateResetButtons(boolean enabled) {
        this.resetXButton.setEnabled(enabled);
        this.resetYButton.setEnabled(enabled);
        this.resetZButton.setEnabled(enabled);
    }

    private void updateControls() {
        updateResetButtons(backend.isIdle());

        if (!backend.isConnected()) {
            // Clear out the status color.
            this.setStatusColorForState("");
        }
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
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
    public void probeCoordinates(Position p) {
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
    public void statusStringListener(ControllerStatus status) {
        this.activeStateValueLabel.setText( status.getState() );
        this.setStatusColorForState( status.getState() );

        if (status.getEnabledPins() != null) {
            if (!addedPinStatusPanel) {
                addedPinStatusPanel = true;
                add(pinStatusPanel, "span 2");
                this.repaint();
            }

            EnabledPins ep = status.getEnabledPins();

            pinX.setSelected(ep.X);
            pinY.setSelected(ep.Y);
            pinZ.setSelected(ep.Z);
            pinProbe.setSelected(ep.Probe);
            pinDoor.setSelected(ep.Door);
            pinHold.setSelected(ep.Hold);
            pinSoftReset.setSelected(ep.SoftReset);
            pinCycleStart.setSelected(ep.CycleStart);
        }

        if (status.getMachineCoord() != null) {
            this.setUnits(status.getMachineCoord().getUnits());

            this.setPostionValueColor(this.machinePositionXValue, status.getMachineCoord().x);
            this.machinePositionXValue.setText(decimalFormatter.format(status.getMachineCoord().x));
            
            this.setPostionValueColor(this.machinePositionYValue, status.getMachineCoord().y);
            this.machinePositionYValue.setText(decimalFormatter.format(status.getMachineCoord().y));
            
            this.setPostionValueColor(this.machinePositionZValue, status.getMachineCoord().z);
            this.machinePositionZValue.setText(decimalFormatter.format(status.getMachineCoord().z));
        }

        if (status.getWorkCoord() != null) {
            this.setUnits(status.getWorkCoord().getUnits());

            this.setPostionValueColor(this.workPositionXValue, status.getWorkCoord().x);
            this.workPositionXValue.setText(decimalFormatter.format(status.getWorkCoord().x));
            
            this.setPostionValueColor(this.workPositionYValue, status.getWorkCoord().y);
            this.workPositionYValue.setText(decimalFormatter.format(status.getWorkCoord().y));
            
            this.setPostionValueColor(this.workPositionZValue, status.getWorkCoord().z);
            this.workPositionZValue.setText(decimalFormatter.format(status.getWorkCoord().z));
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
            if (state.equals(Localization.getString("mainWindow.status.alarm"))
                    || StringUtils.startsWithIgnoreCase(state, "Alarm")) {
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
    private void resetCoordinateButton(char coord) {
        try {
            this.backend.resetCoordinateToZero(coord);
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }
}
