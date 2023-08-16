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
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.util.prefs.PreferenceChangeEvent;

import static com.willwinder.universalgcodesender.utils.SwingHelpers.getDouble;

public class ProbeOutsideXYPanel extends JPanel {
    private final SpinnerNumberModel outsideXDistanceModel;
    private final SpinnerNumberModel outsideYDistanceModel;
    private final SpinnerNumberModel outsideXOffsetModel;
    private final SpinnerNumberModel outsideYOffsetModel;

    public ProbeOutsideXYPanel() {
        outsideXDistanceModel = new SpinnerNumberModel(ProbeSettings.getOutsideXDistance(), null, null, 0.1);
        outsideYDistanceModel = new SpinnerNumberModel(ProbeSettings.getOutsideYDistance(), null, null, 0.1);
        outsideXOffsetModel = new SpinnerNumberModel(ProbeSettings.getOutsideXOffset(), null, null, 0.1);
        outsideYOffsetModel = new SpinnerNumberModel(ProbeSettings.getOutsideYOffset(), null, null, 0.1);

        createLayout();
        registerListeners();
    }

    private void registerListeners() {
        outsideXDistanceModel.addChangeListener(l -> ProbeSettings.setOutsideXDistance(getDouble(outsideXDistanceModel)));
        outsideYDistanceModel.addChangeListener(l -> ProbeSettings.setOutsideYDistance(getDouble(outsideYDistanceModel)));
        outsideXOffsetModel.addChangeListener(l -> ProbeSettings.setOutsideXOffset(getDouble(outsideXOffsetModel)));
        outsideYOffsetModel.addChangeListener(l -> ProbeSettings.setOutsideYOffset(getDouble(outsideYOffsetModel)));
        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for(Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void createLayout() {
        setLayout(new MigLayout("wrap 2,  insets 10, gap 12"));
        add(new JLabel(Localization.getString("probe.x-distance") + ":"));
        add(new JLabel(Localization.getString("probe.y-distance") + ":"));
        add(new JSpinner(outsideXDistanceModel), "growx");
        add(new JSpinner(outsideYDistanceModel), "growx");

        add(new JLabel(Localization.getString("autoleveler.option.offset-x") + ":"));
        add(new JLabel(Localization.getString("autoleveler.option.offset-y") + ":"));
        add(new JSpinner(outsideXOffsetModel), "growx");
        add(new JSpinner(outsideYOffsetModel), "growx");

        add(new JButton(new ProbeOutsideXYAction()), "spanx 2, spany 2, growx, growy");
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        switch (e.getKey()) {
            case ProbeSettings.OUTSIDE_X_DISTANCE:
                outsideXDistanceModel.setValue(ProbeSettings.getOutsideXDistance());
                break;
            case ProbeSettings.OUTSIDE_Y_DISTANCE:
                outsideYDistanceModel.setValue(ProbeSettings.getOutsideYDistance());
                break;
            case ProbeSettings.OUTSIDE_X_OFFSET:
                outsideXOffsetModel.setValue(ProbeSettings.getOutsideXOffset());
                break;
            case ProbeSettings.OUTSIDE_Y_OFFSET:
                outsideYOffsetModel.setValue(ProbeSettings.getOutsideYOffset());
                break;
        }
    }
}
