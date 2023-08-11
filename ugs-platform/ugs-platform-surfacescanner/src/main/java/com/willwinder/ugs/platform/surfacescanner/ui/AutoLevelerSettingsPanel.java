/*
    Copyright 2017-2023 Will Winder

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
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 *
 * @author wwinder
 */
public class AutoLevelerSettingsPanel extends AbstractUGSSettings {
    private final Spinner zHeightSpinner = new Spinner(
                Localization.getString("autoleveler.option.z-zero"),
                new SpinnerNumberModel(0., null, null, 1.));

    private final Spinner probeFeedRate = new Spinner(
                Localization.getString("probe.feed-rate"),
                new SpinnerNumberModel(1., 0.0, null, 1.));

    private final Spinner probeScanFeedRate = new Spinner(
            Localization.getString("probe.scan-rate"),
            new SpinnerNumberModel(1., 0.0, null, 1.));

    private final Spinner arcSegmentLengthSpinner = new Spinner(
                Localization.getString("autoleveler.option.arc-segment-length"),
                new SpinnerNumberModel(0.2, 0.001, null, 0.1));

    private final Spinner xOffsetSpinner = new Spinner(
                Localization.getString("autoleveler.option.offset-x"),
                new SpinnerNumberModel(0., 0., null, 1.));

    private final Spinner yOffsetSpinner = new Spinner(
                Localization.getString("autoleveler.option.offset-y"),
                new SpinnerNumberModel(0., 0., null, 1.));

    private final Spinner zOffsetSpinner = new Spinner(
                Localization.getString("autoleveler.option.offset-z"),
                new SpinnerNumberModel(0., 0., null, 1.));

    public AutoLevelerSettingsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
    }

    @Override
    protected void updateComponentsInternal(Settings s) {
        this.removeAll();

        AutoLevelSettings autoLevelSettings = s.getAutoLevelSettings();

        setLayout(new MigLayout("wrap 1", "grow, fill"));

        this.zHeightSpinner.setValue(autoLevelSettings.getAutoLevelProbeZeroHeight());
        add(this.zHeightSpinner);

        this.probeFeedRate.setValue(autoLevelSettings.getProbeSpeed());
        add(this.probeFeedRate);

        this.probeScanFeedRate.setValue(autoLevelSettings.getProbeScanFeedRate());
        add(this.probeScanFeedRate);

        this.arcSegmentLengthSpinner.setValue(autoLevelSettings.getAutoLevelArcSliceLength());
        add(this.arcSegmentLengthSpinner);

        this.xOffsetSpinner.setValue(autoLevelSettings.getAutoLevelProbeOffset().x);
        add(this.xOffsetSpinner);

        this.yOffsetSpinner.setValue(autoLevelSettings.getAutoLevelProbeOffset().y);
        add(this.yOffsetSpinner);

        this.zOffsetSpinner.setValue(autoLevelSettings.getAutoLevelProbeOffset().z);
        add(this.zOffsetSpinner);
    }

    @Override
    public void save() {
        AutoLevelSettings values = new AutoLevelSettings(settings.getAutoLevelSettings());
        values.setAutoLevelProbeZeroHeight((double) this.zHeightSpinner.getValue());
        values.setProbeSpeed((double) this.probeFeedRate.getValue());
        values.setProbeScanFeedRate( (double) this.probeScanFeedRate.getValue());
        values.setAutoLevelArcSliceLength((double)this.arcSegmentLengthSpinner.getValue());
        values.setAutoLevelProbeOffset(new Position(
                (double)this.xOffsetSpinner.getValue(),
                (double)this.yOffsetSpinner.getValue(),
                (double)this.zOffsetSpinner.getValue(),
                Units.MM));
        settings.getAutoLevelSettings().apply(values);
    }

    @Override
    public String getHelpMessage() {
        return "";
    }

    @Override
    public void restoreDefaults() throws Exception {
    }
}
