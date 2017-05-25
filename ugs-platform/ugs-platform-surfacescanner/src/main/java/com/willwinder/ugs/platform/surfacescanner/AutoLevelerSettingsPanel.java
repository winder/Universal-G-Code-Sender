/*
    Copyright 2017 Will Winder

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
package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class AutoLevelerSettingsPanel extends AbstractUGSSettings {
    final Spinner zHeightSpinner = new Spinner(
                Localization.getString("autoleveler.option.z-zero"),
                new SpinnerNumberModel(0d, null, null, 1d));

    final Spinner arcSegmentLengthSpinner = new Spinner(
                Localization.getString("autoleveler.option.arc-segment-length"),
                new SpinnerNumberModel(0.2, 0.001, null, 0.1));

    final Spinner feedRateSpinner = new Spinner(
                Localization.getString("probe.feed-rate"),
                new SpinnerNumberModel(1., 1., null, 1.));

    final Spinner xOffsetSpinner = new Spinner(
                Localization.getString("autoleveler.option.offset-x"),
                new SpinnerNumberModel(0., 0., null, 1.));

    final Spinner yOffsetSpinner = new Spinner(
                Localization.getString("autoleveler.option.offset-y"),
                new SpinnerNumberModel(0., 0., null, 1.));

    final Spinner zOffsetSpinner = new Spinner(
                Localization.getString("autoleveler.option.offset-z"),
                new SpinnerNumberModel(0., 0., null, 1.));

    public AutoLevelerSettingsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
    }

    @Override
    protected void updateComponentsInternal(Settings s) {
        this.removeAll();

        Settings.AutoLevelSettings autoLevelSettings = s.getAutoLevelSettings();

        setLayout(new MigLayout("wrap 1", "grow, fill", "grow, fill"));

        this.zHeightSpinner.setValue(autoLevelSettings.autoLevelProbeZeroHeight);
        add(this.zHeightSpinner);

        this.arcSegmentLengthSpinner.setValue(autoLevelSettings.autoLevelArcSliceLength);
        add(this.arcSegmentLengthSpinner);

        this.feedRateSpinner.setValue(autoLevelSettings.autoLevelFeedRate);
        add(this.feedRateSpinner);

        this.xOffsetSpinner.setValue(autoLevelSettings.autoLevelProbeOffset.x);
        add(this.xOffsetSpinner);

        this.yOffsetSpinner.setValue(autoLevelSettings.autoLevelProbeOffset.y);
        add(this.yOffsetSpinner);

        this.zOffsetSpinner.setValue(autoLevelSettings.autoLevelProbeOffset.z);
        add(this.zOffsetSpinner);
    }

    @Override
    public void save() {
        Settings.AutoLevelSettings values = new Settings.AutoLevelSettings();

        values.autoLevelProbeZeroHeight = (double) this.zHeightSpinner.getValue();
        values.autoLevelArcSliceLength = (double)this.arcSegmentLengthSpinner.getValue();
        values.autoLevelFeedRate = (double)this.feedRateSpinner.getValue();
        values.autoLevelProbeOffset = new Position(
                (double)this.xOffsetSpinner.getValue(),
                (double)this.yOffsetSpinner.getValue(),
                (double)this.zOffsetSpinner.getValue(),
                Units.UNKNOWN);
    }

    @Override
    public String getHelpMessage() {
        return "";
    }

    @Override
    public void restoreDefaults() throws Exception {
    }
}