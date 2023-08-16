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
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import java.awt.*;
import java.util.prefs.PreferenceChangeEvent;

import static com.willwinder.universalgcodesender.utils.SwingHelpers.getDouble;

public class ProbeHoleCenterPanel extends JPanel {
    private final SpinnerNumberModel hcDiameterModel;

    public ProbeHoleCenterPanel() {
        hcDiameterModel = new SpinnerNumberModel(ProbeSettings.getHcDiameter(), null, null, 0.1);

        createLayout();
        registerListeners();
    }

    private void registerListeners() {
        hcDiameterModel.addChangeListener(e -> ProbeSettings.setHcDiameter(getDouble(hcDiameterModel)));
        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for(Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void createLayout() {
        JButton measureHC = new JButton(new ProbeHoleCenterAction());

        setLayout(new MigLayout("wrap 2, insets 12"));
        add(new JLabel(Localization.getString("probe.hole-diameter")));
        add(new JSpinner(this.hcDiameterModel), "growx, wrap");
        add(new JSeparator(JSeparator.HORIZONTAL), "spanx 2, growx, growy, wrap");
        add(new HintLabel(Localization.getString("probe.hole-center-hint")), "spanx 2, growx, wrap, gapy 8");
        add(measureHC, "spanx 2, growx, growy, gapy 8");
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        if (e.getKey().equals(ProbeSettings.HC_DIAMETER)) {
            hcDiameterModel.setValue(ProbeSettings.getHcDiameter());
        }
    }
}
