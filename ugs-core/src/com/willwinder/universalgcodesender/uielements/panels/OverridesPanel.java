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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.AccessoryStates;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Send speed override commands to the backend.
 *
 * @author wwinder
 */
public final class OverridesPanel extends JPanel implements UGSEventListener {
    private final BackendAPI backend;
    private final ArrayList<Component> components = new ArrayList<>();

    private final JLabel feedSpeed = new JLabel("100%");
    private final JRadioButton feedRadio = new JRadioButton(FEED_SHORT);

    private final JLabel spindleSpeed = new JLabel("100%");
    private final JRadioButton spindleRadio = new JRadioButton(SPINDLE_SHORT);

    private final JLabel rapidSpeed = new JLabel("100%");
    private final JRadioButton rapidRadio = new JRadioButton(RAPID_SHORT);

    private final JButton adjust1 = new JButton("");
    private final JButton adjust2 = new JButton("");
    private final JButton adjust3 = new JButton("");
    private final JButton adjust4 = new JButton("");
    private final JButton adjust5 = new JButton("");

    private final JButton toggleSpindle = new JButton(SPINDLE_SHORT);
    private final JButton toggleFloodCoolant = new JButton(FLOOD);
    private final JButton toggleMistCoolant = new JButton(MIST);

    private final ArrayList<RealTimeAction> rapidActions = new ArrayList<>();
    private final ArrayList<RealTimeAction> spindleActions = new ArrayList<>();
    private final ArrayList<RealTimeAction> feedActions = new ArrayList<>();

    public static final String FEED_SHORT = Localization.getString("overrides.feed.short");
    public static final String SPINDLE_SHORT = Localization.getString("overrides.spindle.short");
    public static final String RAPID_SHORT = Localization.getString("overrides.rapid.short");
    public static final String TOGGLE_SHORT = Localization.getString("overrides.toggle.short");
    public static final String RESET_SPINDLE = Localization.getString("overrides.spindle.reset");
    public static final String RESET_FEED = Localization.getString("overrides.feed.reset");
    public static final String MINUS_COARSE = "--";
    public static final String MINUS_FINE = "-";
    public static final String PLUS_COARSE = "++";
    public static final String PLUS_FINE = "+";
    public static final String RAPID_LOW = Localization.getString("overrides.rapid.low");
    public static final String RAPID_MEDIUM = Localization.getString("overrides.rapid.medium");
    public static final String RAPID_FULL = Localization.getString("overrides.rapid.full");
    public static final String MIST = Localization.getString("overrides.mist");
    public static final String FLOOD = Localization.getString("overrides.flood");

    public OverridesPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        initComponents();
        updateControls();
    }

    public void updateControls() {
        boolean enabled = backend.isConnected() &&
                backend.getController().getCapabilities().hasOverrides();

        this.setEnabled(enabled);
        for (Component c : components) { 
            c.setEnabled(enabled);
        }

        if (enabled) {
            radioSelected();
        } else {
            toggleSpindle.setBackground(null);
            toggleMistCoolant.setBackground(null);
            toggleFloodCoolant.setBackground(null);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            updateControls();
        } else if (evt instanceof ControllerStatusEvent) {
            ControllerStatus status = ((ControllerStatusEvent) evt).getStatus();
            if (status.getOverrides() != null) {
                this.feedSpeed.setText(status.getOverrides().feed + "%");
                this.spindleSpeed.setText(status.getOverrides().spindle + "%");
                this.rapidSpeed.setText(status.getOverrides().rapid + "%");
            }
            if (status.getAccessoryStates() != null) {
                AccessoryStates states = status.getAccessoryStates();

                toggleSpindle.setBackground((states.SpindleCW || states.SpindleCCW) ? Color.GREEN : Color.RED);
                toggleFloodCoolant.setBackground(states.Flood ? Color.GREEN : Color.RED);
                toggleMistCoolant.setBackground(states.Mist ? Color.GREEN : Color.RED);

                toggleSpindle.setOpaque(true);
                toggleFloodCoolant.setOpaque(true);
                toggleMistCoolant.setOpaque(true);
            }
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

            adjust1.setText(RAPID_LOW);
            adjust2.setText("");
            adjust3.setText(RAPID_MEDIUM);
            adjust4.setText(RAPID_FULL);
            adjust5.setText("");

            adjust1.setAction(rapidActions.get(0));
            adjust3.setAction(rapidActions.get(1));
            adjust4.setAction(rapidActions.get(2));
        } else {
            adjust2.setEnabled(true);
            adjust5.setEnabled(true);

            adjust1.setText(MINUS_COARSE);
            adjust2.setText(MINUS_FINE);
            adjust4.setText(PLUS_FINE);
            adjust5.setText(PLUS_COARSE);

            ArrayList<RealTimeAction> actions = null;
            if (feedRadio.isSelected()) {
                adjust3.setText(RESET_FEED);
                actions = feedActions;
            } else if (spindleRadio.isSelected()) {
                adjust3.setText(RESET_SPINDLE);
                actions = spindleActions;
            }

            if (actions != null) {
                adjust1.setAction(actions.get(0));
                adjust2.setAction(actions.get(1));
                adjust3.setAction(actions.get(2));
                adjust4.setAction(actions.get(3));
                adjust5.setAction(actions.get(4));
            }
        }
    }

    private void initComponents() {
        rapidActions.add(new RealTimeAction(RAPID_LOW, Overrides.CMD_RAPID_OVR_LOW, backend));
        rapidActions.add(new RealTimeAction(RAPID_MEDIUM, Overrides.CMD_RAPID_OVR_MEDIUM, backend));
        rapidActions.add(new RealTimeAction(RAPID_FULL, Overrides.CMD_RAPID_OVR_RESET, backend));

        spindleActions.add(new RealTimeAction(MINUS_COARSE, Overrides.CMD_SPINDLE_OVR_COARSE_MINUS, backend));
        spindleActions.add(new RealTimeAction(MINUS_FINE, Overrides.CMD_SPINDLE_OVR_FINE_MINUS, backend));
        spindleActions.add(new RealTimeAction(RESET_SPINDLE, Overrides.CMD_SPINDLE_OVR_RESET, backend));
        spindleActions.add(new RealTimeAction(PLUS_FINE, Overrides.CMD_SPINDLE_OVR_FINE_PLUS, backend));
        spindleActions.add(new RealTimeAction(PLUS_COARSE, Overrides.CMD_SPINDLE_OVR_COARSE_PLUS, backend));

        feedActions.add(new RealTimeAction(MINUS_COARSE, Overrides.CMD_FEED_OVR_COARSE_MINUS, backend));
        feedActions.add(new RealTimeAction(MINUS_FINE, Overrides.CMD_FEED_OVR_FINE_MINUS, backend));
        feedActions.add(new RealTimeAction(RESET_FEED, Overrides.CMD_FEED_OVR_RESET, backend));
        feedActions.add(new RealTimeAction(PLUS_FINE, Overrides.CMD_FEED_OVR_FINE_PLUS, backend));
        feedActions.add(new RealTimeAction(PLUS_COARSE, Overrides.CMD_FEED_OVR_COARSE_PLUS, backend));

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

        this.add(new JLabel(FEED_SHORT + ":"));
        this.add(feedSpeed);
        this.add(adjust1);
        this.add(adjust2);

        this.add(new JLabel(SPINDLE_SHORT + ":"));
        this.add(spindleSpeed);
        this.add(adjust3, "span 2");

        this.add(new JLabel("Rapid:"));
        this.add(rapidSpeed);
        this.add(adjust4);
        this.add(adjust5, "wrap");

        this.add(new JLabel(TOGGLE_SHORT + ":"));
        this.add(toggleSpindle);
        this.add(toggleFloodCoolant);
        this.add(toggleMistCoolant);
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
