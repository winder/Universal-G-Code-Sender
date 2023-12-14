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
import com.willwinder.ugs.platform.probe.actions.ProbeZAction;
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

public class ProbeZPanel extends JPanel {
    private final UnitSpinner zProbeDistanceSpinner;
    private final UnitSpinner zProbeOffsetSpinner;

    public ProbeZPanel() {
        var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
        zProbeDistanceSpinner = new UnitSpinner(ProbeSettings.getzDistance(), units);
        zProbeOffsetSpinner = new UnitSpinner(ProbeSettings.getzOffset(), units);
        createLayout();
        registerListeners();
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void createLayout() {
        setLayout(new MigLayout("insets 10, gap 12", "[shrink][120:120, sg1]"));
        add(new JLabel(Localization.getString("probe.plate-thickness")));
        add(zProbeOffsetSpinner, "growx, wrap");
        add(new JLabel(Localization.getString("probe.probe-distance") + ":"));
        add(zProbeDistanceSpinner, "growx, wrap");
        add(new JButton(new ProbeZAction()), "spanx 2, growx, growy, height 40:40");
    }

    private void registerListeners() {
        zProbeDistanceSpinner.addChangeListener(l -> ProbeSettings.setzDistance(zProbeDistanceSpinner.getDoubleValue()));
        zProbeOffsetSpinner.addChangeListener(l -> ProbeSettings.setzOffset(zProbeOffsetSpinner.getDoubleValue()));
        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    private void onSettingsChanged(PreferenceChangeEvent event) {
        switch (event.getKey()) {
            case ProbeSettings.SETTINGS_UNITS:
                var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
                zProbeOffsetSpinner.setUnits(units);
                zProbeDistanceSpinner.setUnits(units);
                break;
            case ProbeSettings.Z_DISTANCE:
                zProbeDistanceSpinner.setValue(ProbeSettings.getzDistance());
                break;
            case ProbeSettings.Z_OFFSET:
                zProbeOffsetSpinner.setValue(ProbeSettings.getzOffset());
                break;
        }
    }
}
