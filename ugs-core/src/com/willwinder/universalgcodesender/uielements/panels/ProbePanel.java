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
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.StepSizeSpinnerModel;
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

/**
 *
 * @author wwinder
 */
public class ProbePanel extends JPanel implements UGSEventListener {
    private final BackendAPI backend;
    private final Settings settings;
    private Position start = null;
    private Position probePosition = null;

    // State
    private boolean probing = false;
    private boolean finalizing = false;

    // UI Components
    private final JSpinner feedRate = new JSpinner();
    private final JSpinner probeOffset = new JSpinner();
    private final JSpinner probeDistance = new JSpinner();
    private final JSpinner retractHeight = new JSpinner();
    private final JButton probeButton = new JButton(Localization.getString("probe.button"));

    public ProbePanel(BackendAPI backend) {
        this.backend = backend;
        this.settings = backend.getSettings();

        backend.addUGSEventListener(this);

        initComponents();
        updateControls();
    }

    private void doProbe(ActionEvent evt) {
        probing = true;

        try {
            this.feedRate.commitEdit();
            this.probeOffset.commitEdit();
            this.probeDistance.commitEdit();
        } catch (ParseException ex) {
            Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            this.start = backend.getMachinePosition();
            this.probePosition = null;
            backend.probe(
                    "Z",
                    settings.getProbeFeed(),
                    settings.getProbeDistance(),
                    UnitUtils.Units.MM);
        } catch (Exception ex) {
            probing = false;
            Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void probeDone(Position p) {
        if (!finalizing) return;

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

        finalizing = false;
    }

    private static double getSpinnerDouble(JSpinner spinner) {
        return Double.parseDouble(spinner.getValue().toString());
    }

    private void initComponents() {
        probing = false;

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

    @Override
    public void UGSEvent(UGSEvent evt) {
        switch(evt.getEventType()){
            case STATE_EVENT:
            case SETTING_EVENT:
                updateControls();
                break;
            case PROBE_EVENT:
                // Wait for the next controller status to calculate an offset.
                finalizing = probing;
                probing = false;
                break;
            case CONTROLLER_STATUS_EVENT:
                if (finalizing) {
                    probeDone(this.probePosition);
                }
                break;
            case FILE_EVENT:
                default:
                return;
        }
    }
}
