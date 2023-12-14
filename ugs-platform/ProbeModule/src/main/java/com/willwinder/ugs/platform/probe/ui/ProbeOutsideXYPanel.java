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
import com.willwinder.ugs.platform.probe.actions.ProbeOutsideXYAction;
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

public class ProbeOutsideXYPanel extends JPanel {
    private final UnitSpinner outsideXDistanceSpinner;
    private final UnitSpinner outsideYDistanceSpinner;
    private final UnitSpinner outsideXOffsetSpinner;
    private final UnitSpinner outsideYOffsetSpinner;

    public ProbeOutsideXYPanel() {
        var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
        outsideXDistanceSpinner = new UnitSpinner(ProbeSettings.getOutsideXDistance(), units);
        outsideYDistanceSpinner = new UnitSpinner(ProbeSettings.getOutsideYDistance(), units);
        outsideXOffsetSpinner = new UnitSpinner(ProbeSettings.getOutsideXOffset(), units);
        outsideYOffsetSpinner = new UnitSpinner(ProbeSettings.getOutsideYOffset(), units);

        createLayout();
        registerListeners();
    }

    private void registerListeners() {
        outsideXDistanceSpinner.addChangeListener(l -> ProbeSettings.setOutsideXDistance(outsideXDistanceSpinner.getDoubleValue()));
        outsideYDistanceSpinner.addChangeListener(l -> ProbeSettings.setOutsideYDistance(outsideYDistanceSpinner.getDoubleValue()));
        outsideXOffsetSpinner.addChangeListener(l -> ProbeSettings.setOutsideXOffset(outsideXOffsetSpinner.getDoubleValue()));
        outsideYOffsetSpinner.addChangeListener(l -> ProbeSettings.setOutsideYOffset(outsideYOffsetSpinner.getDoubleValue()));
        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for(Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void createLayout() {
        setLayout(new MigLayout("wrap 2,  insets 10, gap 12", "[120:120, sg1][120:120, sg1]"));
        add(new JLabel(Localization.getString("probe.x-distance") + ":"));
        add(new JLabel(Localization.getString("probe.y-distance") + ":"));
        add(outsideXDistanceSpinner, "growx");
        add(outsideYDistanceSpinner, "growx");

        add(new JLabel(Localization.getString("autoleveler.option.offset-x") + ":"));
        add(new JLabel(Localization.getString("autoleveler.option.offset-y") + ":"));
        add(outsideXOffsetSpinner, "growx");
        add(outsideYOffsetSpinner, "growx");

        add(new JButton(new ProbeOutsideXYAction()), "spanx 2, spany 2, growx, growy, height 40:40");
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        switch (e.getKey()) {
            case ProbeSettings.SETTINGS_UNITS:
                var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
                outsideXDistanceSpinner.setUnits(units);
                outsideYDistanceSpinner.setUnits(units);
                outsideXOffsetSpinner.setUnits(units);
                outsideYOffsetSpinner.setUnits(units);
                break;
            case ProbeSettings.OUTSIDE_X_DISTANCE:
                outsideXDistanceSpinner.setValue(ProbeSettings.getOutsideXDistance());
                break;
            case ProbeSettings.OUTSIDE_Y_DISTANCE:
                outsideYDistanceSpinner.setValue(ProbeSettings.getOutsideYDistance());
                break;
            case ProbeSettings.OUTSIDE_X_OFFSET:
                outsideXOffsetSpinner.setValue(ProbeSettings.getOutsideXOffset());
                break;
            case ProbeSettings.OUTSIDE_Y_OFFSET:
                outsideYOffsetSpinner.setValue(ProbeSettings.getOutsideYOffset());
                break;
        }
    }
}
