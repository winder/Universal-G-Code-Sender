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
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.util.prefs.PreferenceChangeEvent;

import static com.willwinder.universalgcodesender.utils.SwingHelpers.getDouble;

public class ProbeZPanel extends JPanel {
    private final SpinnerNumberModel zProbeDistance;
    private final SpinnerNumberModel zProbeOffset;

    public ProbeZPanel() {
        zProbeDistance = new SpinnerNumberModel(ProbeSettings.getzDistance(), null, null, 0.1);
        zProbeOffset = new SpinnerNumberModel(ProbeSettings.getzOffset(), null, null, 0.1);

        createLayout();
        registerListeners();
    }

    @Override
    public void setEnabled(boolean enabled) {
        for(Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void createLayout() {
        setLayout(new MigLayout("insets 10, gap 12"));
        add(new JLabel(Localization.getString("probe.plate-thickness")));
        add(new JSpinner(zProbeOffset), "growx, wrap");
        add(new JLabel(Localization.getString("probe.probe-distance") + ":"));
        add(new JSpinner(zProbeDistance), "growx, wrap");
        add(new JButton(new ProbeZAction()), "spanx 2, growx, growy");
    }

    private void registerListeners() {
        zProbeDistance.addChangeListener(l -> ProbeSettings.setzDistance(getDouble(zProbeDistance)));
        zProbeOffset.addChangeListener(l -> ProbeSettings.setzOffset(getDouble(zProbeOffset)));
        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    private void onSettingsChanged(PreferenceChangeEvent event) {
        switch (event.getKey()) {
            case ProbeSettings.Z_DISTANCE:
                zProbeDistance.setValue(ProbeSettings.getzDistance());
                break;
            case ProbeSettings.Z_OFFSET:
                zProbeOffset.setValue(ProbeSettings.getzOffset());
                break;
        }
    }
}
