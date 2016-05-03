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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Overrides;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public final class OverridesPanel extends JPanel implements UGSEventListener {
    private final BackendAPI backend;
    private ArrayList<Component> components = new ArrayList<>();
    public OverridesPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        initComponents();
        updateControls();
    }

    public void updateControls() {
        boolean enabled = backend.getControlState() == COMM_IDLE;

        for (Component c : components) { 
            c.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    public Component add(Component comp) {
        Component ret = super.add(comp);
        if (comp instanceof JButton)
            components.add(comp);
        return ret;
    }
    
    private void initComponents() {
        this.setLayout(new MigLayout("wrap 6"));
        this.add(new JLabel("Feed:"));
        this.add(new JButton(new RealTimeAction("--", Overrides.CMD_FEED_OVR_COARSE_MINUS, backend)));
        this.add(new JButton(new RealTimeAction("-", Overrides.CMD_FEED_OVR_FINE_MINUS, backend)));
        this.add(new JButton(new RealTimeAction("reset", Overrides.CMD_FEED_OVR_RESET, backend)));
        this.add(new JButton(new RealTimeAction("+", Overrides.CMD_FEED_OVR_FINE_PLUS, backend)));
        this.add(new JButton(new RealTimeAction("++", Overrides.CMD_FEED_OVR_COARSE_PLUS, backend)));

        this.add(new JLabel("Spindle:"));
        this.add(new JButton(new RealTimeAction("--", Overrides.CMD_SPINDLE_OVR_COARSE_MINUS, backend)));
        this.add(new JButton(new RealTimeAction("-", Overrides.CMD_SPINDLE_OVR_FINE_MINUS, backend)));
        this.add(new JButton(new RealTimeAction("reset", Overrides.CMD_SPINDLE_OVR_RESET, backend)));
        this.add(new JButton(new RealTimeAction("+", Overrides.CMD_SPINDLE_OVR_FINE_PLUS, backend)));
        this.add(new JButton(new RealTimeAction("++", Overrides.CMD_SPINDLE_OVR_COARSE_PLUS, backend)));
       
        this.add(new JLabel("Rapid:"));
        this.add(new JButton(new RealTimeAction("low", Overrides.CMD_RAPID_OVR_LOW, backend)), "span 2");
        this.add(new JButton(new RealTimeAction("medium", Overrides.CMD_RAPID_OVR_MEDIUM, backend)));
        this.add(new JButton(new RealTimeAction("full", Overrides.CMD_RAPID_OVR_RESET, backend)), "span 2");
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
