/**
 * Send speed override commands to the backend.
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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.AccessoryStates;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Overrides;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_DISCONNECTED;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public final class OverridesPanel extends JPanel implements UGSEventListener, ControllerListener {
    private final BackendAPI backend;
    private ArrayList<Component> components = new ArrayList<>();

    private final JLabel feedSpeed = new JLabel("100%");
    private final JRadioButton feedRadio = new JRadioButton("Feed");

    private final JLabel spindleSpeed = new JLabel("100%");
    private final JRadioButton spindleRadio = new JRadioButton("Spindle");

    private final JLabel rapidSpeed = new JLabel("100%");
    private final JRadioButton rapidRadio = new JRadioButton("Rapid");

    private final JButton adjust1 = new JButton("");
    private final JButton adjust2 = new JButton("");
    private final JButton adjust3 = new JButton("");
    private final JButton adjust4 = new JButton("");
    private final JButton adjust5 = new JButton("");

    private final JButton toggleSpindle = new JButton("Spindle");
    private final JButton toggleFloodCoolant = new JButton("Flood Coolant");
    private final JButton toggleMistCoolant = new JButton("Mist Coolant");

    public OverridesPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
            backend.addControllerListener(this);
        }

        initComponents();
        updateControls();
    }

    public void updateControls() {
        boolean enabled = backend.getControlState() != COMM_DISCONNECTED;
        this.setEnabled(enabled);

        for (Component c : components) { 
            c.setEnabled(enabled);
        }

        if (enabled) {
            radioSelected();
        }
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    public void add(Component comp, String str) {
        super.add(comp, str);
        if (comp instanceof JButton || comp instanceof JRadioButton)
            components.add(comp);
    }

    public Component add(Component comp) {
        Component ret = super.add(comp);
        if (comp instanceof JButton || comp instanceof JRadioButton)
            components.add(comp);
        return ret;
    }

    private void radioSelected() {
        if (rapidRadio.isSelected()) {
            adjust2.setEnabled(false);
            adjust5.setEnabled(false);

            adjust1.setText("low");
            adjust2.setText("");
            adjust3.setText("medium");
            adjust4.setText("full");
            adjust5.setText("");

            adjust1.setAction(new RealTimeAction("low", Overrides.CMD_RAPID_OVR_LOW, backend));
            adjust3.setAction(new RealTimeAction("medium", Overrides.CMD_RAPID_OVR_MEDIUM, backend));
            adjust4.setAction(new RealTimeAction("full", Overrides.CMD_RAPID_OVR_RESET, backend));
        } else {
            adjust2.setEnabled(true);
            adjust5.setEnabled(true);

            adjust1.setText("--");
            adjust2.setText("-");
            adjust3.setText("reset");
            adjust4.setText("+");
            adjust5.setText("++");

            if (feedRadio.isSelected()) {
                adjust1.setAction(new RealTimeAction("--", Overrides.CMD_FEED_OVR_COARSE_MINUS, backend));
                adjust2.setAction(new RealTimeAction("-", Overrides.CMD_FEED_OVR_FINE_MINUS, backend));
                adjust3.setAction(new RealTimeAction("reset", Overrides.CMD_FEED_OVR_RESET, backend));
                adjust4.setAction(new RealTimeAction("+", Overrides.CMD_FEED_OVR_FINE_PLUS, backend));
                adjust5.setAction(new RealTimeAction("++", Overrides.CMD_FEED_OVR_COARSE_PLUS, backend));
            } else if (spindleRadio.isSelected()) {
                adjust1.setAction(new RealTimeAction("--", Overrides.CMD_SPINDLE_OVR_COARSE_MINUS, backend));
                adjust2.setAction(new RealTimeAction("-", Overrides.CMD_SPINDLE_OVR_FINE_MINUS, backend));
                adjust3.setAction(new RealTimeAction("reset", Overrides.CMD_SPINDLE_OVR_RESET, backend));
                adjust4.setAction(new RealTimeAction("+", Overrides.CMD_SPINDLE_OVR_FINE_PLUS, backend));
                adjust5.setAction(new RealTimeAction("++", Overrides.CMD_SPINDLE_OVR_COARSE_PLUS, backend));
            }
        }
    }
    
    private void initComponents() {
        adjust1.setEnabled(false);
        adjust2.setEnabled(false);
        adjust3.setEnabled(false);
        adjust4.setEnabled(false);
        adjust5.setEnabled(false);

        ButtonGroup group = new ButtonGroup();
        group.add(feedRadio);
        group.add(spindleRadio);
        group.add(rapidRadio);

        // Initialize callbacks
        // Radio buttons
        feedRadio.addActionListener((ActionEvent ae) -> radioSelected());
        spindleRadio.addActionListener((ActionEvent ae) -> radioSelected());
        rapidRadio.addActionListener((ActionEvent ae) -> radioSelected());
        // Toggle actions
        toggleSpindle.setAction(new RealTimeAction("spindle", Overrides.CMD_TOGGLE_SPINDLE, backend));
        toggleSpindle.setBackground(Color.RED);
        toggleFloodCoolant.setAction(new RealTimeAction("flood", Overrides.CMD_TOGGLE_FLOOD_COOLANT, backend));
        toggleFloodCoolant.setBackground(Color.RED);
        toggleMistCoolant.setAction(new RealTimeAction("mist", Overrides.CMD_TOGGLE_MIST_COOLANT, backend));
        toggleMistCoolant.setBackground(Color.RED);

        // Layout components
        this.setLayout(new MigLayout("wrap 4"));

        this.add(feedRadio);
        this.add(spindleRadio);
        this.add(rapidRadio, "wrap");

        this.add(new JLabel("Feed:"));
        this.add(feedSpeed);
        this.add(adjust1);
        this.add(adjust2);

        this.add(new JLabel("Spindle:"));
        this.add(spindleSpeed);
        this.add(adjust3, "span 2");

        this.add(new JLabel("Rapid:"));
        this.add(rapidSpeed);
        this.add(adjust4);
        this.add(adjust5, "wrap");

        this.add(new JLabel("Toggle:"));
        this.add(toggleSpindle);
        this.add(toggleFloodCoolant);
        this.add(toggleMistCoolant);
    }

    @Override
    public void controlStateChange(com.willwinder.universalgcodesender.model.UGSEvent.ControlState state) {
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
    public void commandComplete(GcodeCommand command) {
    }

    @Override
    public void commandComment(String comment) {
    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
    }

    @Override
    public void statusStringListener(ControllerStatus status) {
        if (status.getOverrides() != null) {
            this.feedSpeed.setText(status.getOverrides().feed + "%");
            this.spindleSpeed.setText(status.getOverrides().spindle + "%");
            this.rapidSpeed.setText(status.getOverrides().rapid + "%");
        }
        if (status.getAccessoryStates() != null) {
            Color defaultBackground = UIManager.getColor("Panel.background");
            AccessoryStates states = status.getAccessoryStates();

            toggleSpindle.setBackground((states.SpindleCW || states.SpindleCCW) ? Color.GREEN : Color.RED);
            toggleFloodCoolant.setBackground(states.Flood ? Color.GREEN : Color.RED);
            toggleMistCoolant.setBackground(states.Mist ? Color.GREEN : Color.RED);

            toggleSpindle.setOpaque(true);
            toggleFloodCoolant.setOpaque(true);
            toggleMistCoolant.setOpaque(true);
        }
    }

    @Override
    public void postProcessData(int numRows) {
    }

    private static class RealTimeAction extends AbstractAction {
        private final Overrides command;
        private final BackendAPI backend;
        public RealTimeAction(String name, Overrides override, BackendAPI backend) {
            this.putValue(Action.NAME, name);
            this.command = override;
            this.backend = backend;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isEnabled()) {
                try {
                    backend.sendOverrideCommand(command);
                } catch (Exception ex) {
                    Logger.getLogger(OverridesPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
