/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.probe.ui;

import com.willwinder.ugs.platform.probe.ProbeSettings;
import com.willwinder.ugs.platform.probe.actions.ProbeXYZAction;
import com.willwinder.universalgcodesender.i18n.Localization;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.PreferenceChangeEvent;

import static com.willwinder.universalgcodesender.utils.SwingHelpers.getDouble;

public class ProbeXYZPanel extends JPanel {
    private final SpinnerNumberModel xyzXDistanceModel;
    private final SpinnerNumberModel xyzYDistanceModel;
    private final SpinnerNumberModel xyzZDistanceModel;
    private final SpinnerNumberModel xyzXOffsetModel;
    private final SpinnerNumberModel xyzYOffsetModel;
    private final SpinnerNumberModel xyzZOffsetModel;

    public ProbeXYZPanel() {
        xyzXDistanceModel = new SpinnerNumberModel(ProbeSettings.getXyzXDistance(), null, null, 0.1);
        xyzYDistanceModel = new SpinnerNumberModel(ProbeSettings.getXyzYDistance(), null, null, 0.1);
        xyzZDistanceModel = new SpinnerNumberModel(ProbeSettings.getXyzZDistance(), null, null, 0.1);
        xyzXOffsetModel = new SpinnerNumberModel(ProbeSettings.getXyzXOffset(), null, null, 0.1);
        xyzYOffsetModel = new SpinnerNumberModel(ProbeSettings.getXyzYOffset(), null, null, 0.1);
        xyzZOffsetModel = new SpinnerNumberModel(ProbeSettings.getXyzZOffset(), null, null, 0.1);

        createLayout();
        registerListeners();
    }

    @Override
    public void setEnabled(boolean enabled) {
        for(Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void registerListeners() {
        xyzXDistanceModel.addChangeListener(l -> ProbeSettings.setXyzXDistance(getDouble(xyzXDistanceModel)));
        xyzYDistanceModel.addChangeListener(l -> ProbeSettings.setXyzYDistance(getDouble(xyzYDistanceModel)));
        xyzZDistanceModel.addChangeListener(l -> ProbeSettings.setXyzZDistance(getDouble(xyzZDistanceModel)));
        xyzXOffsetModel.addChangeListener(l -> ProbeSettings.setXyzXOffset(getDouble(xyzXOffsetModel)));
        xyzYOffsetModel.addChangeListener(l -> ProbeSettings.setXyzYOffset(getDouble(xyzYOffsetModel)));
        xyzZOffsetModel.addChangeListener(l -> ProbeSettings.setXyzZOffset(getDouble(xyzZOffsetModel)));

        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    private void createLayout() {
        setLayout(new MigLayout("wrap 3,  insets 10, gap 12"));
        add(new JLabel(Localization.getString("probe.x-distance") + ":"));
        add(new JLabel(Localization.getString("probe.y-distance") + ":"));
        add(new JLabel(Localization.getString("probe.probe-distance") + ":"));
        add(new JSpinner(xyzXDistanceModel), "growx");
        add(new JSpinner(xyzYDistanceModel), "growx");
        add(new JSpinner(xyzZDistanceModel), "growx");

        add(new JLabel(Localization.getString("autoleveler.option.offset-x") + ":"));
        add(new JLabel(Localization.getString("autoleveler.option.offset-y") + ":"));
        add(new JLabel(Localization.getString("probe.plate-thickness")));
        add(new JSpinner(xyzXOffsetModel), "growx");
        add(new JSpinner(xyzYOffsetModel), "growx");
        add(new JSpinner(xyzZOffsetModel), "growx");

        add(new JButton(new ProbeXYZAction()), "spanx 2, growx, growy");
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        switch (e.getKey()) {
            case ProbeSettings.XYZ_X_DISTANCE:
                xyzXDistanceModel.setValue(ProbeSettings.getXyzXDistance());
                break;
            case ProbeSettings.XYZ_Y_DISTANCE:
                xyzYDistanceModel.setValue(ProbeSettings.getXyzYDistance());
                break;
            case ProbeSettings.XYZ_Z_DISTANCE:
                xyzZDistanceModel.setValue(ProbeSettings.getXyzZDistance());
                break;
            case ProbeSettings.XYZ_X_OFFSET:
                xyzXOffsetModel.setValue(ProbeSettings.getXyzXOffset());
                break;
            case ProbeSettings.XYZ_Y_OFFSET:
                xyzYOffsetModel.setValue(ProbeSettings.getXyzYOffset());
                break;
            case ProbeSettings.XYZ_Z_OFFSET:
                xyzZOffsetModel.setValue(ProbeSettings.getXyzZOffset());
                break;
        }
    }
}
