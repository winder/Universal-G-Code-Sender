/*
    Copywrite 2017 Will Winder

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
import com.willwinder.universalgcodesender.listeners.ControllerStateListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.StepSizeSpinnerModel;
import static com.willwinder.universalgcodesender.uielements.panels.ProbePanel.ProbeState.*;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import net.miginfocom.swing.MigLayout;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

/**
 *
 * @author wwinder
 */
public class ProbePanel extends JPanel implements UGSEventListener, ControllerStateListener {
    private final BackendAPI backend;
    private final Settings settings;
    private Position probePosition = null;

    // UI Components
    private final JSpinner feedRate = new JSpinner();
    private final JSpinner probeOffset = new JSpinner();
    private final JSpinner probeDistance = new JSpinner();
    private final JSpinner retractHeight = new JSpinner();
    private final JButton probeButton = new JButton(Localization.getString("probe.button"));

    static enum ProbeState {
        Waiting, FastProbe, SmallRetract, SlowProbe, SlowProbed, Finalizing
    }

    static enum ProbeEvent {
        Start, Probed, Position, Idle
    }

    public static class ZProbeStateMachine extends AbstractStateMachine<ZProbeStateMachine, ProbeState, ProbeEvent, UGSEvent> {
        private final BackendAPI backend;
        private final Settings settings;
        private Position start = null;
        private Position probePosition = null;

        public ZProbeStateMachine(BackendAPI backend) {
            this.backend = backend;
            this.settings = backend.getSettings();
        }

        public void probe(ProbeState from, ProbeState to, ProbeEvent event, UGSEvent ugsState){
            try {
                double probeSpeed;

                if (to == ProbeState.FastProbe) {
                    probeSpeed = settings.getProbeFeed();
                } else if (to == ProbeState.SlowProbe) {
                    probeSpeed = settings.getProbeFeed() / 2;
                } else {
                    return;
                }

                this.start = backend.getMachinePosition();
                this.probePosition = null;
                backend.probe(
                        "Z",
                        probeSpeed,
                        settings.getProbeDistance(),
                        UnitUtils.Units.MM);
            } catch (Exception ex) {
                Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void smallRetract(ProbeState from, ProbeState to, ProbeEvent event, UGSEvent ugsState){
            try {
                backend.sendGcodeCommand(true, "G91 G21 G0 Z1");
            } catch (Exception ex) {
                Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void setOffsetFinalRetract(ProbeState from, ProbeState to, ProbeEvent event, UGSEvent ugsState){
            try {
                Position cur = backend.getWorkPosition().getPositionIn(UnitUtils.Units.MM);
                double offset = cur.z;

                // Gcode to update location adjusting for thickness
                backend.offsetTool(
                        "Z",
                        offset - settings.getProbeOffset(),
                        UnitUtils.Units.MM);
                backend.sendGcodeCommand(true, "G91 G21 G0 Z" + settings.getRetractHeight());
            } catch (Exception ex) {
                Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    private final StateMachine<ZProbeStateMachine, ProbeState, ProbeEvent, UGSEvent> fsm;
    public ProbePanel(BackendAPI backend) {
        this.backend = backend;
        this.settings = backend.getSettings();

        backend.addUGSEventListener(this);
        backend.addControllerStateListener(this);

        fsm = initStateMachine();
        initComponents();
        updateControls();
    }

    private StateMachine<ZProbeStateMachine, ProbeState, ProbeEvent, UGSEvent> initStateMachine() {
        StateMachineBuilder builder = StateMachineBuilderFactory.create
                (ZProbeStateMachine.class, ProbeState.class, ProbeEvent.class, UGSEvent.class, BackendAPI.class);

        // Send the fast probe
        builder.externalTransition().from(Waiting).to(FastProbe)
                .on(ProbeEvent.Start).callMethod("probe");
        // After fast-probe retract a small amount
        builder.externalTransition().from(FastProbe).to(SmallRetract)
                .on(ProbeEvent.Probed).callMethod("smallRetract");
        // Once retracted do a slow probe
        builder.externalTransition().from(SmallRetract).to(SlowProbe)
                .on(ProbeEvent.Idle).callMethod("probe");
        // After the slow probe, wait for a position event
        builder.externalTransition().from(SlowProbe).to(SlowProbed)
                .on(ProbeEvent.Probed);
        // Finalize.
        builder.externalTransition().from(SlowProbed).to(Finalizing)
                .on(ProbeEvent.Position).callMethod("setOffsetFinalRetract");
        // Reset when the final retract finishes.
        builder.externalTransition().from(Finalizing).to(Waiting).on(ProbeEvent.Position);

        return builder.newStateMachine(ProbeState.Waiting, new Object[] {backend});
    }

    private void doProbe(ActionEvent evt) {
        try {
            this.feedRate.commitEdit();
            this.probeOffset.commitEdit();
            this.probeDistance.commitEdit();
        } catch (ParseException ex) {
            Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.probeButton.setEnabled(false);
        fsm.fire(ProbeEvent.Start);
    }

    private void probeDone(Position p) {
        try {
            Position cur = backend.getWorkPosition().getPositionIn(UnitUtils.Units.MM);
            double offset = cur.z;

            // Gcode to update location adjusting for thickness
            backend.offsetTool(
                    "Z",
                    offset - settings.getProbeOffset(),
                    UnitUtils.Units.MM);
            backend.sendGcodeCommand(true, "G91 G21 G0 Z" + settings.getRetractHeight());
        } catch (Exception ex) {
            Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static double getSpinnerDouble(JSpinner spinner) {
        return Double.parseDouble(spinner.getValue().toString());
    }

    private void initComponents() {
        this.feedRate.setModel(new StepSizeSpinnerModel(settings.getProbeFeed(), 1., null, 1.));
        this.probeOffset.setModel(new StepSizeSpinnerModel(settings.getProbeOffset(), null, null, 0.1));
        this.probeDistance.setModel(new StepSizeSpinnerModel(settings.getProbeDistance(), null, null, 0.1));
        this.retractHeight.setModel(new StepSizeSpinnerModel(settings.getRetractHeight(), null, null, 1.));

        this.feedRate.addChangeListener(cl -> settings.setProbeFeed(getSpinnerDouble(this.feedRate)));
        this.probeOffset.addChangeListener(cl -> settings.setProbeOffset(getSpinnerDouble(this.probeOffset)));
        this.probeDistance.addChangeListener(cl -> settings.setProbeDistance(getSpinnerDouble(this.probeDistance)));
        this.retractHeight.addChangeListener(cl -> settings.setRetractHeight(getSpinnerDouble(this.retractHeight)));

        MigLayout layout = new MigLayout("fill, wrap 1");
        setLayout(layout);

        // Callbacks
        this.probeButton.addActionListener(this::doProbe);

        // Feed rate
        add(new JLabel(Localization.getString("probe.feed-rate")));
        add(this.feedRate, "growx");

        // Plate thickness / bit radius
        add(new JLabel(Localization.getString("probe.plate-thickness")));
        add(this.probeOffset, "growx");

        // Distance to probe
        add(new JLabel(Localization.getString("probe.probe-distance")));
        add(this.probeDistance, "growx");

        // Probe plane
        add(new JLabel(Localization.getString("probe.retract-height")));
        add(this.retractHeight, "growx");

        add(this.probeButton, "growx");
    }

    private void updateControls() {
        enableDisable(backend.isIdle());
    }

    private void enableDisable(boolean enabled) {
        this.feedRate.setEnabled(enabled);
        this.retractHeight.setEnabled(enabled);
        this.probeOffset.setEnabled(enabled);
        this.probeDistance.setEnabled(enabled);
        this.probeButton.setEnabled(enabled);
    }

    /**
     * Fire events to the probe state machine, it will handle state transitions as needed.
     */
    @Override
    public void UGSEvent(UGSEvent evt) {
        switch(evt.getEventType()){
            case STATE_EVENT:
                if (evt.getControlState() == COMM_IDLE){
                    fsm.fire(ProbeEvent.Idle, evt);
                }
            case SETTING_EVENT:
                updateControls();
                break;
            case PROBE_EVENT:
                fsm.fire(ProbeEvent.Probed, evt);
                break;
            case CONTROLLER_STATUS_EVENT:
                fsm.fire(ProbeEvent.Position, evt);
                break;
            case FILE_EVENT:
                default:
                return;
        }
    }
}
