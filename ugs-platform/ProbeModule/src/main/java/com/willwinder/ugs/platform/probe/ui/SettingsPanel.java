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
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.WorkCoordinateSystem;
import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.G54;
import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.G55;
import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.G56;
import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.G57;
import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.G58;
import static com.willwinder.universalgcodesender.model.WorkCoordinateSystem.G59;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.util.prefs.PreferenceChangeEvent;

public class SettingsPanel extends JPanel {
    private final JComboBox<WorkCoordinateSystem> settingsWorkCoordinate;
    private final UnitSpinner settingsProbeDiameter;
    private final UnitSpinner settingsFastFindRate;
    private final UnitSpinner settingsSlowMeasureRate;
    private final UnitSpinner settingsRetractAmount;
    private final UnitSpinner settingsDelayAfterRetract;
    private final JCheckBox settingsCompensateForSoftLimits;

    public SettingsPanel() {
        var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
        var rateUnits = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM_PER_MINUTE : TextFieldUnit.INCHES_PER_MINUTE;

        settingsWorkCoordinate = new JComboBox<>(new WorkCoordinateSystem[]{G54, G55, G56, G57, G58, G59});
        settingsWorkCoordinate.setSelectedItem(ProbeSettings.getSettingsWorkCoordinate());

        settingsProbeDiameter = new UnitSpinner(Math.max(ProbeSettings.getSettingsProbeDiameter(), 0), units, 0.d, null, 0.1d);
        settingsFastFindRate = new UnitSpinner(Math.max(ProbeSettings.getSettingsFastFindRate(), 0.1), rateUnits, 0.1d, null, 1.);
        settingsSlowMeasureRate = new UnitSpinner(Math.max(ProbeSettings.getSettingsSlowMeasureRate(), 0.1), rateUnits, 0.1d, null, 1.);
        settingsRetractAmount = new UnitSpinner(Math.max(ProbeSettings.getSettingsRetractAmount(), 0.01), units, 0.01d, null, 0.1);
        settingsDelayAfterRetract = new UnitSpinner(Math.max(ProbeSettings.getSettingsDelayAfterRetract(), 0.0), TextFieldUnit.SECONDS, 0d, null, 0.1d);
        settingsCompensateForSoftLimits = new JCheckBox();
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
        setLayout(new MigLayout("wrap 2, insets 10, gap 12", "[shrink][140:140, sg 1]"));

        add(new JLabel(Localization.getString("gcode.setting.endmill-diameter") + ":"), "al right");
        add(settingsProbeDiameter, "growx");

        add(new JLabel(Localization.getString("probe.find-rate") + ":"), "al right");
        settingsFastFindRate.setToolTipText(Localization.getString("probe.find-rate.tooltip"));
        add(settingsFastFindRate, "growx");

        add(new JLabel(Localization.getString("probe.measure-rate") + ":"), "al right");
        settingsSlowMeasureRate.setToolTipText(Localization.getString("probe.measure-rate.tooltip"));
        add(settingsSlowMeasureRate, "growx");

        add(new JLabel(Localization.getString("probe.retract-amount") + ":"), "al right");
        settingsRetractAmount.setToolTipText(Localization.getString("probe.retract-amount.tooltip"));
        add(settingsRetractAmount, "growx");

        add(new JLabel(Localization.getString("probe.delay-after-retract") + ":"), "al right");
        settingsDelayAfterRetract.setToolTipText(Localization.getString("probe.delay-after-retract.tooltip"));
        add(settingsDelayAfterRetract, "growx");

        add(new JLabel(Localization.getString("probe.work-coordinates") + ":"), "al right");
        add(settingsWorkCoordinate, "growx");

        add(new JLabel(Localization.getString("probe.compensate-for-soft-limits") + ":"), "al right");
        settingsCompensateForSoftLimits.setToolTipText(Localization.getString("probe.compensate-for-soft-limits.tooltip"));
        add(settingsCompensateForSoftLimits, "growx");
        settingsCompensateForSoftLimits.setSelected(ProbeSettings.getCompensateForSoftLimits());
    }

    private void registerListeners() {
        settingsWorkCoordinate.addActionListener(l -> {
            WorkCoordinateSystem selectedItem = (WorkCoordinateSystem) settingsWorkCoordinate.getSelectedItem();
            if (selectedItem == null) {
                selectedItem = WorkCoordinateSystem.G54;
            }
            ProbeSettings.setSettingsWorkCoordinate(selectedItem);
        });

        settingsProbeDiameter.addChangeListener(l -> ProbeSettings.setSettingsProbeDiameter(settingsProbeDiameter.getDoubleValue()));
        settingsFastFindRate.addChangeListener(l -> ProbeSettings.setSettingsFastFindRate(settingsFastFindRate.getDoubleValue()));
        settingsSlowMeasureRate.addChangeListener(l -> ProbeSettings.setSettingsSlowMeasureRate(settingsSlowMeasureRate.getDoubleValue()));
        settingsRetractAmount.addChangeListener(l -> ProbeSettings.setSettingsRetractAmount(settingsRetractAmount.getDoubleValue()));
        settingsDelayAfterRetract.addChangeListener(l -> ProbeSettings.setSettingsDelayAfterRetract(settingsDelayAfterRetract.getDoubleValue()));
        settingsCompensateForSoftLimits.addChangeListener(l -> ProbeSettings.setCompensateForSoftLimits(settingsCompensateForSoftLimits.isSelected()));

        ProbeSettings.addPreferenceChangeListener(this::onSettingsChanged);
    }

    private void onSettingsChanged(PreferenceChangeEvent e) {
        switch (e.getKey()) {
            case ProbeSettings.SETTINGS_UNITS:
                var units = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
                var rateUnits = ProbeSettings.getSettingsUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM_PER_MINUTE : TextFieldUnit.INCHES_PER_MINUTE;
                settingsProbeDiameter.setUnits(units);
                settingsRetractAmount.setUnits(units);
                settingsFastFindRate.setUnits(rateUnits);
                settingsSlowMeasureRate.setUnits(rateUnits);
                break;
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
            case ProbeSettings.SETTINGS_DELAY_AFTER_RETRACT:
                settingsDelayAfterRetract.setValue(ProbeSettings.getSettingsDelayAfterRetract());
                break;
            case ProbeSettings.SETTINGS_COMPENSATE_FOR_SOFT_LIMITS:
                settingsCompensateForSoftLimits.setSelected(ProbeSettings.getCompensateForSoftLimits());
                break;
        }
    }
}
