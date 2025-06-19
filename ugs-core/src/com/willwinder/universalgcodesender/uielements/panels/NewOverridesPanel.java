/*
 * Copyright (C) 2023 Simone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import static com.willwinder.universalgcodesender.uielements.panels.OverridesPanel.FEED_SHORT;
import static com.willwinder.universalgcodesender.uielements.panels.OverridesPanel.MINUS_COARSE;
import static com.willwinder.universalgcodesender.uielements.panels.OverridesPanel.MINUS_FINE;
import static com.willwinder.universalgcodesender.uielements.panels.OverridesPanel.PLUS_COARSE;
import static com.willwinder.universalgcodesender.uielements.panels.OverridesPanel.PLUS_FINE;
import static com.willwinder.universalgcodesender.uielements.panels.OverridesPanel.RESET_FEED;
import static com.willwinder.universalgcodesender.uielements.panels.OverridesPanel.SPINDLE_SHORT;
import static java.lang.Math.abs;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Simone
 */
public class NewOverridesPanel extends javax.swing.JPanel implements UGSEventListener, ChangeListener {
    private final BackendAPI backend;
    private final ArrayList<Component> components = new ArrayList<>();

    private final JLabel feedLabel = new JLabel();
    private final JSlider feedSlider = new JSlider(0, 200);
    private final JSlider spindleSlider = new JSlider(0, 100);
    private final JSlider rapidSlider = new JSlider(0, 100);

    private final JButton feedMinusCoarseButton;
    private final JButton feedMinusFineButton;
    private final JButton feedResetButton;
    private final JButton feedPlusFineButton;   
    private final JButton feedPlusCoarseButton;

    private final JButton spindleMinusCoarseButton;
    private final JButton spindleMinusFineButton;
    private final JButton spindleResetButton;
    private final JButton spindlePlusFineButton;
    private final JButton spindlePlusCoarseButton;
//
//    private final JButton rapidMinusCoarseButton;
//    private final JButton rapidMinusFineButton;
//    private final JButton rapidResetButton;
//    private final JButton rapidPlusFineButton;
//    private final JButton rapidPlusCoarseButton;
    
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
    /**
     * Creates new form NewOverridesPanel
     */
    public NewOverridesPanel(BackendAPI backend) {
        
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        feedMinusCoarseButton = new JButton(new OverrideAction(MINUS_COARSE, Overrides.CMD_FEED_OVR_COARSE_MINUS, backend));
        feedMinusFineButton = new JButton(new OverrideAction(MINUS_FINE, Overrides.CMD_FEED_OVR_FINE_MINUS, backend));
        feedResetButton = new JButton(new OverrideAction("RESET", Overrides.CMD_FEED_OVR_RESET, backend));
        feedPlusFineButton = new JButton(new OverrideAction(PLUS_FINE, Overrides.CMD_FEED_OVR_FINE_PLUS, backend));
        feedPlusCoarseButton = new JButton(new OverrideAction(PLUS_COARSE, Overrides.CMD_FEED_OVR_COARSE_PLUS, backend));

        spindleMinusCoarseButton = new JButton(new OverrideAction(MINUS_COARSE, Overrides.CMD_SPINDLE_OVR_FINE_MINUS, backend));
        spindleMinusFineButton = new JButton(new OverrideAction(MINUS_FINE, Overrides.CMD_SPINDLE_OVR_FINE_MINUS, backend));
        spindleResetButton = new JButton(new OverrideAction("RESET", Overrides.CMD_SPINDLE_OVR_RESET, backend));
        spindlePlusFineButton = new JButton(new OverrideAction(PLUS_FINE, Overrides.CMD_SPINDLE_OVR_FINE_PLUS, backend));
        spindlePlusCoarseButton = new JButton(new OverrideAction(PLUS_COARSE, Overrides.CMD_SPINDLE_OVR_COARSE_PLUS, backend));

        initComponents();
        updateControls();
        
        feedSlider.addChangeListener(this);
        feedSlider.setMajorTickSpacing(50);
        feedSlider.setMinorTickSpacing(10);
        feedSlider.setPaintTicks(true);
        feedSlider.setPaintLabels(true);
        feedSlider.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                feedSlider.setValue(feedSlider.getValue() + e.getWheelRotation() * -1);
            }
        });
        spindleSlider.addChangeListener(this);
        rapidSlider.addChangeListener(this);

        feedLabel.setText(FEED_SHORT + ":");
    }
    
    public void updateControls() {
        boolean enabled = backend.isConnected() &&
                backend.getController().getCapabilities().hasOverrides();

        this.setEnabled(enabled);
        for (Component c : components) { 
            c.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            updateControls();
        } else if (evt instanceof ControllerStatusEvent) {
            ControllerStatus status = ((ControllerStatusEvent) evt).getStatus();
            if (status.getOverrides() != null) {
                //this.feedSlider.removeChangeListener(this);
                //this.feedSlider.setValue(status.getOverrides().feed);
                //this.feedSlider.addChangeListener(this);

                this.feedLabel.setText(FEED_SHORT + ":" + status.getOverrides().feed + "%");
                this.spindleSlider.setValue(status.getOverrides().feed);
                this.rapidSlider.setValue(status.getOverrides().spindle);
            }
        }
    }

    private void initComponents() {
        this.setLayout(new MigLayout("fillx, wrap 6" ));
        
        this.add(feedLabel, "span 1 2");
        this.add(feedSlider, "span 5, grow");
        this.add(feedMinusCoarseButton, "skip, grow");
        this.add(feedMinusFineButton, "grow");
        this.add(feedResetButton, "grow");
        this.add(feedPlusFineButton, "grow");
        this.add(feedPlusCoarseButton, "grow");
        
        this.add(new JLabel(SPINDLE_SHORT + ":"));
        this.add(spindleSlider, "span 5");
        
        this.add(new JLabel("Rapid:"));
        this.add(rapidSlider, "span 5");
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == feedSlider) {
            if (feedSlider.getValueIsAdjusting())
                return;

            int sp = feedSlider.getValue();
            int act = backend.getController().getControllerStatus().getOverrides().feed;
            int delta = sp-act;
            int coarse = abs(delta) / 10;
            int fine = abs(delta) % 10;

            Logger.getLogger(NewOverridesPanel.class.getName()).log(Level.INFO, (delta>0?"+":"-") + coarse + ":" + fine);

            Overrides coarseCommand = (delta<0) ? Overrides.CMD_FEED_OVR_COARSE_MINUS : Overrides.CMD_FEED_OVR_COARSE_PLUS;
            Overrides fineCommand = (delta<0) ? Overrides.CMD_FEED_OVR_FINE_MINUS : Overrides.CMD_FEED_OVR_FINE_PLUS;

            try {
                for (int i = 0; i < coarse; i++) {
                    backend.sendOverrideCommand(coarseCommand);
                }
                for (int i = 0; i < fine; i++) {
                    backend.sendOverrideCommand(fineCommand);
                }
            } catch (Exception ex) {
                Logger.getLogger(NewOverridesPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    private class OverrideAction extends AbstractAction {
        private final Overrides command;
        private final BackendAPI backend;
        
        public OverrideAction(String name, Overrides override, BackendAPI backend) {
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
