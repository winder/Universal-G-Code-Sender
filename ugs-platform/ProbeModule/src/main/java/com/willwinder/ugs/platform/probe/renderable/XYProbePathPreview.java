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
package com.willwinder.ugs.platform.probe.renderable;

import com.willwinder.ugs.platform.probe.ProbeSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils;

public class XYProbePathPreview extends CornerProbePathPreview {
    public XYProbePathPreview() {
        super(Localization.getString("probe.visualizer.corner-preview"));
    }

    @Override
    public void updateSettings() {
        UnitUtils.Units settingsUnits = ProbeSettings.getSettingsUnits();
        double scaleFactor = UnitUtils.scaleUnits(settingsUnits, UnitUtils.Units.MM);
        updateSpacing(
                ProbeSettings.getOutsideXDistance() * scaleFactor,
                ProbeSettings.getOutsideYDistance() * scaleFactor,
                0,
                ProbeSettings.getOutsideXOffset() * scaleFactor,
                ProbeSettings.getOutsideYOffset() * scaleFactor,
                0);
    }
}
