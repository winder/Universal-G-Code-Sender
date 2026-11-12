/*
    Copyright 2017-2026 Will Winder

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
package com.willwinder.ugs.platform.surfacescanner.ui;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.AutoLevelArcMode;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Component;

/**
 *
 * @author wwinder
 */
public class AutoLevelerSettingsPanel extends AbstractUGSSettings {
    private final UnitSpinnerRow probeFeedRate = new UnitSpinnerRow(Localization.getString("probe.feed-rate"), 0d);
    private final UnitSpinnerRow probeScanFeedRate = new UnitSpinnerRow(Localization.getString("probe.scan-rate"), 0d);
    private final UnitSpinnerRow arcSegmentLength = new UnitSpinnerRow(Localization.getString("autoleveler.option.arc-segment-length"), 0.001d);
    private final JComboBox<AutoLevelArcMode> arcMode = new JComboBox<>(AutoLevelArcMode.values());
    private final UnitSpinnerRow plateThickness = new UnitSpinnerRow(Localization.getString("probe.plate-thickness"), 0d);
    private final UnitSpinnerRow xOffset = new UnitSpinnerRow(Localization.getString("autoleveler.option.offset-x"), null);
    private final UnitSpinnerRow yOffset = new UnitSpinnerRow(Localization.getString("autoleveler.option.offset-y"), null);
    private final UnitSpinnerRow zOffset = new UnitSpinnerRow(Localization.getString("autoleveler.option.offset-z"), null);

    private boolean updatingComponents = false;

    public AutoLevelerSettingsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
        arcMode.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AutoLevelArcMode) {
                    setText(Localization.getString(((AutoLevelArcMode) value).getLabelKey()));
                }
                return this;
            }
        });
        arcMode.setToolTipText(Localization.getString("autoleveler.option.arc-mode.tooltip"));
        arcMode.addActionListener(e -> {
            if (!updatingComponents) {
                updateArcSegmentLengthEnabled();
                change();
            }
        });
    }

    @Override
    protected void updateComponentsInternal(Settings s) {
        updatingComponents = true;
        try {
            this.removeAll();

            AutoLevelSettings autoLevelSettings = s.getAutoLevelSettings();
            Units preferredUnits = s.getPreferredUnits();
            Unit lengthUnit = preferredUnits == Units.MM ? Unit.MM : Unit.INCH;
            Unit feedRateUnit = preferredUnits == Units.MM ? Unit.MM_PER_MINUTE : Unit.INCHES_PER_MINUTE;

            setLayout(new MigLayout("wrap 1", "grow, fill"));

            probeFeedRate.setUnits(feedRateUnit);
            probeFeedRate.setValue(autoLevelSettings.getProbeSpeed(), Unit.MM_PER_MINUTE);
            add(probeFeedRate);

            probeScanFeedRate.setUnits(feedRateUnit);
            probeScanFeedRate.setValue(autoLevelSettings.getProbeScanFeedRate(), Unit.MM_PER_MINUTE);
            add(probeScanFeedRate);

            plateThickness.setUnits(lengthUnit);
            plateThickness.setValue(autoLevelSettings.getTouchPlateThickness(), Unit.MM);
            add(plateThickness);

            Position probeOffset = autoLevelSettings.getAutoLevelProbeOffset();

            xOffset.setUnits(lengthUnit);
            xOffset.setValue(probeOffset.getX(), Unit.MM);
            add(xOffset);

            yOffset.setUnits(lengthUnit);
            yOffset.setValue(probeOffset.getY(), Unit.MM);
            add(yOffset);

            zOffset.setUnits(lengthUnit);
            zOffset.setValue(probeOffset.getZ(), Unit.MM);
            add(zOffset);

            arcMode.setSelectedItem(autoLevelSettings.getArcMode());
            arcSegmentLength.setUnits(lengthUnit);
            arcSegmentLength.setValue(autoLevelSettings.getAutoLevelArcSliceLength(), Unit.MM);
            updateArcSegmentLengthEnabled();
            add(arcSection(), "gaptop 8, growx");
        } finally {
            updatingComponents = false;
        }
    }

    @Override
    public void save() {
        AutoLevelSettings values = new AutoLevelSettings(settings.getAutoLevelSettings());
        values.setProbeSpeed(probeFeedRate.getValue(Unit.MM_PER_MINUTE));
        values.setProbeScanFeedRate(probeScanFeedRate.getValue(Unit.MM_PER_MINUTE));
        values.setArcMode((AutoLevelArcMode) arcMode.getSelectedItem());
        values.setAutoLevelArcSliceLength(arcSegmentLength.getValue(Unit.MM));
        values.setTouchPlateThickness(plateThickness.getValue(Unit.MM));
        values.setAutoLevelProbeOffset(new Position(
                xOffset.getValue(Unit.MM),
                yOffset.getValue(Unit.MM),
                zOffset.getValue(Unit.MM),
                Units.MM));
        settings.getAutoLevelSettings().apply(values);
    }

    @Override
    public String getHelpMessage() {
        return "";
    }

    @Override
    public void restoreDefaults() {
    }

    private void updateArcSegmentLengthEnabled() {
        arcSegmentLength.setRowEnabled(arcMode.getSelectedItem() != AutoLevelArcMode.KEEP_ARCS);
    }

    private JPanel arcSection() {
        JPanel section = new JPanel(new MigLayout("insets 2 6 6 6, wrap 1, gapy 6", "[grow,fill]"));
        section.setBorder(BorderFactory.createTitledBorder(Localization.getString("autoleveler.option.arc-mode")));
        section.add(arcMode, "growx");
        section.add(arcSegmentLength);
        return section;
    }

    private class UnitSpinnerRow extends JPanel {
        private final UnitSpinner spinner;

        private UnitSpinnerRow(String text, Double minimum) {
            spinner = new UnitSpinner(minimum == null ? 0 : minimum, Unit.MM, minimum, null, 0.01d);
            spinner.addChangeListener(e -> {
                if (!updatingComponents) {
                    change();
                }
            });

            setLayout(new MigLayout("insets 0, wrap 2"));
            add(spinner, "w 150");
            add(new JLabel(text));
        }

        private void setRowEnabled(boolean enabled) {
            spinner.setEnabled(enabled);
            for (Component child : getComponents()) {
                child.setEnabled(enabled);
            }
        }

        private void setUnits(Unit units) {
            spinner.setUnits(units);
        }

        private void setValue(double value, Unit units) {
            spinner.setValue(value, units);
        }

        private double getValue(Unit units) {
            return spinner.getDoubleValue(units);
        }
    }
}
