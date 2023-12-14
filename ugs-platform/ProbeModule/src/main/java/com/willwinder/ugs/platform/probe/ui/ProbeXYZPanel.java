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
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.util.prefs.PreferenceChangeEvent;

public class ProbeXYZPanel extends JPanel {
    private final UnitSpinner xyzXDistanceSpinner;
    private final UnitSpinner xyzYDistanceSpinner;
    private final UnitSpinner xyzZDistanceSpinner;
    private final UnitSpinner xyzXOffsetSpinner;
    private final UnitSpinner xyzYOffsetSpinner;
    private final UnitSpinner xyzZOffsetSpinner;

    public ProbeXYZPanel() {
        var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
        xyzXDistanceSpinner = new UnitSpinner(ProbeSettings.getXyzXDistance(), units);
        xyzYDistanceSpinner = new UnitSpinner(ProbeSettings.getXyzYDistance(), units);
        xyzZDistanceSpinner = new UnitSpinner(ProbeSettings.getXyzZDistance(), units);
        xyzXOffsetSpinner = new UnitSpinner(ProbeSettings.getXyzXOffset(), units);
        xyzYOffsetSpinner = new UnitSpinner(ProbeSettings.getXyzYOffset(), units);
        xyzZOffsetSpinner = new UnitSpinner(ProbeSettings.getXyzZOffset(), units);

        createLayout();
        registerListeners();
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void registerListeners() {
        xyzXDistanceSpinner.addChangeListener(l -> ProbeSettings.setXyzXDistance(xyzXDistanceSpinner.getDoubleValue()));
        xyzYDistanceSpinner.addChangeListener(l -> ProbeSettings.setXyzYDistance(xyzYDistanceSpinner.getDoubleValue()));
        xyzZDistanceSpinner.addChangeListener(l -> ProbeSettings.setXyzZDistance(xyzZDistanceSpinner.getDoubleValue()));
        xyzXOffsetSpinner.addChangeListener(l -> ProbeSettings.setXyzXOffset(xyzXOffsetSpinner.getDoubleValue()));
        xyzYOffsetSpinner.addChangeListener(l -> ProbeSettings.setXyzYOffset(xyzYOffsetSpinner.getDoubleValue()));
        xyzZOffsetSpinner.addChangeListener(l -> ProbeSettings.setXyzZOffset(xyzZOffsetSpinner.getDoubleValue()));

        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    private void createLayout() {
        setLayout(new MigLayout("wrap 3,  insets 10, gap 12", "[120:120, sg1][120:120, sg1][120:120, sg1]"));
        add(new JLabel(Localization.getString("probe.x-distance") + ":"));
        add(new JLabel(Localization.getString("probe.y-distance") + ":"));
        add(new JLabel(Localization.getString("probe.probe-distance") + ":"));
        add(xyzXDistanceSpinner, "growx");
        add(xyzYDistanceSpinner, "growx");
        add(xyzZDistanceSpinner, "growx");

        add(new JLabel(Localization.getString("autoleveler.option.offset-x") + ":"));
        add(new JLabel(Localization.getString("autoleveler.option.offset-y") + ":"));
        add(new JLabel(Localization.getString("probe.plate-thickness")));
        add(xyzXOffsetSpinner, "growx");
        add(xyzYOffsetSpinner, "growx");
        add(xyzZOffsetSpinner, "growx");

        add(new JButton(new ProbeXYZAction()), "spanx 2, growx, growy, height 40:40");
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        switch (e.getKey()) {
            case ProbeSettings.SETTINGS_UNITS:
                var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
                xyzXDistanceSpinner.setUnits(units);
                xyzYDistanceSpinner.setUnits(units);
                xyzZDistanceSpinner.setUnits(units);
                xyzXOffsetSpinner.setUnits(units);
                xyzYOffsetSpinner.setUnits(units);
                xyzZOffsetSpinner.setUnits(units);
                break;
            case ProbeSettings.XYZ_X_DISTANCE:
                xyzXDistanceSpinner.setValue(ProbeSettings.getXyzXDistance());
                break;
            case ProbeSettings.XYZ_Y_DISTANCE:
                xyzYDistanceSpinner.setValue(ProbeSettings.getXyzYDistance());
                break;
            case ProbeSettings.XYZ_Z_DISTANCE:
                xyzZDistanceSpinner.setValue(ProbeSettings.getXyzZDistance());
                break;
            case ProbeSettings.XYZ_X_OFFSET:
                xyzXOffsetSpinner.setValue(ProbeSettings.getXyzXOffset());
                break;
            case ProbeSettings.XYZ_Y_OFFSET:
                xyzYOffsetSpinner.setValue(ProbeSettings.getXyzYOffset());
                break;
            case ProbeSettings.XYZ_Z_OFFSET:
                xyzZOffsetSpinner.setValue(ProbeSettings.getXyzZOffset());
                break;
        }
    }
}
