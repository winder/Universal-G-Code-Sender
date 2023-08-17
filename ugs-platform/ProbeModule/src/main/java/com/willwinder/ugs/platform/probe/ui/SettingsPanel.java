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
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.WorkCoordinateSystem;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.util.prefs.PreferenceChangeEvent;

import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.*;
import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.G59;
import static com.willwinder.universalgcodesender.utils.SwingHelpers.getDouble;

public class SettingsPanel extends JPanel {
    private final JComboBox<WorkCoordinateSystem> settingsWorkCoordinate;
    private final SpinnerNumberModel settingsProbeDiameter;
    private final SpinnerNumberModel settingsFastFindRate;
    private final SpinnerNumberModel settingsSlowMeasureRate;
    private final SpinnerNumberModel settingsRetractAmount;

    public SettingsPanel() {
        settingsWorkCoordinate = new JComboBox<>(new WorkCoordinateSystem[]{G54, G55, G56, G57, G58, G59});
        settingsWorkCoordinate.setSelectedItem(ProbeSettings.getSettingsWorkCoordinate());

        settingsProbeDiameter = new SpinnerNumberModel(ProbeSettings.getSettingsProbeDiameter(), 0.d, null, 0.1);
        settingsFastFindRate = new SpinnerNumberModel(ProbeSettings.getSettingsFastFindRate(), 1d, null, 1.);
        settingsSlowMeasureRate = new SpinnerNumberModel(ProbeSettings.getSettingsSlowMeasureRate(), 1d, null, 1.);
        settingsRetractAmount = new SpinnerNumberModel(ProbeSettings.getSettingsRetractAmount(), 0.01d, null, 0.1);

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
        setLayout(new MigLayout("wrap 4, insets 10, gap 12", "[shrink][90:90, sg 1][shrink][90:90, sg 1]"));

        add(new JLabel(Localization.getString("gcode.setting.endmill-diameter") + ":"), "al right");
        add(new JSpinner(settingsProbeDiameter), "growx");

        add(new JLabel(Localization.getString("probe.find-rate") + ":"), "al right");
        add(new JSpinner(settingsFastFindRate), "growx");

        add(new JLabel(Localization.getString("probe.work-coordinates") + ":"), "al right");
        add(settingsWorkCoordinate, "growx");

        add(new JLabel(Localization.getString("probe.measure-rate") + ":"), "al right");
        add(new JSpinner(settingsSlowMeasureRate), "growx");

        add(new JLabel(Localization.getString("probe.retract-amount") + ":"), "al right");
        JSpinner retractSpinner = new JSpinner(settingsRetractAmount);
        retractSpinner.setToolTipText(Localization.getString("probe.retract-amount.tooltip"));
        add(retractSpinner, "growx");
    }

    private void registerListeners() {
        settingsWorkCoordinate.addActionListener(l -> {
            WorkCoordinateSystem selectedItem = (WorkCoordinateSystem) settingsWorkCoordinate.getSelectedItem();
            if (selectedItem == null) {
                selectedItem = WorkCoordinateSystem.G54;
            }
            ProbeSettings.setSettingsWorkCoordinate(selectedItem);
        });

        settingsProbeDiameter.addChangeListener(l -> ProbeSettings.setSettingsProbeDiameter(getDouble(settingsProbeDiameter)));
        settingsFastFindRate.addChangeListener(l -> ProbeSettings.setSettingsFastFindRate(getDouble(settingsFastFindRate)));
        settingsSlowMeasureRate.addChangeListener(l -> ProbeSettings.setSettingsSlowMeasureRate(getDouble(settingsSlowMeasureRate)));
        settingsRetractAmount.addChangeListener(l -> ProbeSettings.setSettingsRetractAmount(getDouble(settingsRetractAmount)));

        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        switch (e.getKey()) {
            case ProbeSettings.SETTINGS_WORK_COORDINATE_SYSTEM:
                settingsWorkCoordinate.getModel().setSelectedItem(ProbeSettings.getSettingsWorkCoordinate());
                break;
            case ProbeSettings.SETTINGS_PROBE_DIAMETER:
                settingsProbeDiameter.setValue(ProbeSettings.getSettingsProbeDiameter());
                break;
            case ProbeSettings.SETTINGS_FAST_FIND_RATE:
                settingsFastFindRate.setValue(ProbeSettings.getSettingsFastFindRate());
                break;
            case ProbeSettings.SETTINGS_SLOW_MEASURE_RATE:
                settingsSlowMeasureRate.setValue(ProbeSettings.getSettingsSlowMeasureRate());
                break;
            case ProbeSettings.SETTINGS_RETRACT_AMOUNT:
                settingsRetractAmount.setValue(ProbeSettings.getSettingsRetractAmount());
                break;
        }
    }
}
