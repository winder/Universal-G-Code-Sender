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
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.uielements.components.StepSizeSpinnerModel;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

    // State
    boolean probing = false;

    // UI Components
    private final JSpinner feedRate = new JSpinner();
    private final JSpinner probeOffset = new JSpinner();
    private final JSpinner probeDistance = new JSpinner();
    private final JComboBox<String> plane = new JComboBox<>(new String[]{"Z", "X","Y"});
    private final JButton probeButton = new JButton(Localization.getString("probe.button"));
    private final JLabel thicknessDiameter = new JLabel();

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
            backend.probe(
                    this.plane.getSelectedItem().toString(),
                    settings.getProbeFeed(),
                    settings.getProbeDistance(),
                    UnitUtils.Units.MM);
        } catch (Exception ex) {
            probing = false;
            Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void probeDone(Position p) {
        if (!probing) return;

        try {
            String axis = this.plane.getSelectedItem().toString();
            double offset = 0;
            switch (axis) {
                case "X":
                    offset = p.x;
                    break;
                case "Y":
                    offset = p.y;
                    break;
                case "Z":
                    offset = p.z;
                    break;
            }

            // Gcode to update location adjusting for thickness/diameter/plane
            backend.offsetTool(
                    this.plane.getSelectedItem().toString(),
                    offset - settings.getProbeOffset(),
                    UnitUtils.Units.MM);
        } catch (Exception ex) {
            Logger.getLogger(ProbePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        probing = false;
    }

    private static double getSpinnerDouble(JSpinner spinner) {
        return Double.parseDouble(spinner.getValue().toString());
    }

    private void initComponents() {
        probing = false;

        this.feedRate.setModel(new StepSizeSpinnerModel(settings.getProbeFeed(), 1., null, 1.));
        this.probeOffset.setModel(new StepSizeSpinnerModel(settings.getProbeOffset(), null, null, 0.1));
        this.probeDistance.setModel(new StepSizeSpinnerModel(settings.getProbeDistance(), null, null, 0.1));

        this.feedRate.addChangeListener(cl -> settings.setProbeFeed(getSpinnerDouble(this.feedRate)));
        this.probeOffset.addChangeListener(cl -> settings.setProbeOffset(getSpinnerDouble(this.probeOffset)));
        this.probeDistance.addChangeListener(cl -> settings.setProbeDistance(getSpinnerDouble(this.probeDistance)));

        MigLayout layout = new MigLayout("fill, wrap 1");
        setLayout(layout);

        // Callbacks
        this.plane.addActionListener(e -> this.updateControls());
        this.probeButton.addActionListener(this::doProbe);

        // Feed rate
        add(new JLabel(Localization.getString("probe.feed-rate")));
        add(this.feedRate, "growx");

        // Plate thickness / bit radius
        add(this.thicknessDiameter);
        add(this.probeOffset, "growx");

        // Distance to probe
        add(new JLabel(Localization.getString("probe.probe-distance")));
        add(this.probeDistance, "growx");

        // Probe plane
        add(new JLabel(Localization.getString("probe.plane")));
        add(this.plane, "growx");

        add(this.probeButton, "growx");
    }

    private void updateControls() {
        enableDisable(backend.isIdle());

        if (this.plane.getSelectedItem().equals("Z")) {
            this.thicknessDiameter.setText(Localization.getString("probe.plate-thickness"));
        } else {
            this.thicknessDiameter.setText(Localization.getString("probe.endmill-radius"));
        }
    }

    private void enableDisable(boolean enabled) {
        this.feedRate.setEnabled(enabled);
        this.plane.setEnabled(enabled);
        this.probeOffset.setEnabled(enabled);
        this.probeDistance.setEnabled(enabled);
        this.probeButton.setEnabled(enabled);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent() || evt.isSettingChangeEvent()) {
            updateControls();
        } else if (evt.isProbeEvent()) {
            probeDone(evt.getProbePosition());
        }
    }
}
