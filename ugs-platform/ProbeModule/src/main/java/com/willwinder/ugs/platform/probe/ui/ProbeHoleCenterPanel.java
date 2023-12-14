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

import com.willwinder.ugs.nbp.core.ui.HintLabel;
import com.willwinder.ugs.platform.probe.ProbeSettings;
import com.willwinder.ugs.platform.probe.actions.ProbeHoleCenterAction;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.util.prefs.PreferenceChangeEvent;

public class ProbeHoleCenterPanel extends JPanel {
    private final UnitSpinner hcDiameterSpinner;

    public ProbeHoleCenterPanel() {
        var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
        hcDiameterSpinner = new UnitSpinner(ProbeSettings.getHcDiameter(), units);

        createLayout();
        registerListeners();
    }

    private void registerListeners() {
        hcDiameterSpinner.addChangeListener(e -> ProbeSettings.setHcDiameter(hcDiameterSpinner.getDoubleValue()));
        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void createLayout() {
        setLayout(new MigLayout("wrap 2, insets 12", "[shrink][120:120, sg1]"));
        add(new JLabel(Localization.getString("probe.hole-diameter")));
        add(this.hcDiameterSpinner, "growx, wrap");
        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx 2, growx, growy, wrap");
        add(new HintLabel(Localization.getString("probe.hole-center-hint")), "spanx 2, growx, wrap, gapy 8");
        add(new JButton(new ProbeHoleCenterAction()), "spanx 2, growx, growy, gapy 8, height 40:40");
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        if (e.getKey().endsWith(ProbeSettings.SETTINGS_UNITS)) {
            var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
            hcDiameterSpinner.setUnits(units);
        } else if (e.getKey().equals(ProbeSettings.HC_DIAMETER)) {
            hcDiameterSpinner.setValue(ProbeSettings.getHcDiameter());
        }
    }
}
