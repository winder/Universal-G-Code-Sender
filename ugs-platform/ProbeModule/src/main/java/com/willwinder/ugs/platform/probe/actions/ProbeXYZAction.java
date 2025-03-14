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
package com.willwinder.ugs.platform.probe.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.platform.probe.ProbeParameters;
import com.willwinder.ugs.platform.probe.ProbeService;
import com.willwinder.ugs.platform.probe.ProbeSettings;
import com.willwinder.ugs.platform.probe.renderable.ProbePreviewManager;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "com.willwinder.ugs.platform.probe.actions.ProbeXYZAction")
@ActionRegistration(
        iconBase = ProbeOutsideXYAction.BASE_ICON,
        displayName = "Probe and zero XYZ",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE_PROBE,
                position = 30)})
public class ProbeXYZAction extends AbstractProbeAction {

    public static final String BASE_ICON = "com/willwinder/ugs/platform/probe/icons/xyzprobe.svg";
    public static final String BASE_ICON_LARGE = "com/willwinder/ugs/platform/probe/icons/xyzprobe24.svg";

    public ProbeXYZAction() {
        putValue("iconBase", BASE_ICON);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(BASE_ICON, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(BASE_ICON_LARGE, false));
        putValue("menuText", Localization.getString("probe.action.xyz"));
        putValue(NAME, Localization.getString("probe.action.xyz"));
    }

    @Override
    public void performProbeAction() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        ProbeService probeService = Lookup.getDefault().lookup(ProbeService.class);

        ProbeParameters pc = new ProbeParameters(ProbeSettings.getSettingsProbeDiameter(),
                backend.getMachinePosition(),
                ProbeSettings.getXyzXDistance(),
                ProbeSettings.getXyzYDistance(),
                ProbeSettings.getXyzZDistance(),
                ProbeSettings.getXyzXOffset(),
                ProbeSettings.getXyzYOffset(),
                ProbeSettings.getXyzZOffset(),
                0.0,
                ProbeSettings.getSettingsFastFindRate(),
                ProbeSettings.getSettingsSlowMeasureRate(),
                ProbeSettings.getSettingsRetractAmount(),
                ProbeSettings.getSettingsDelayAfterRetract(),
                backend.getSettings().getPreferredUnits(),
                ProbeSettings.getSettingsWorkCoordinate());

        ProbePreviewManager probePreviewManager = Lookup.getDefault().lookup(ProbePreviewManager.class);
        probePreviewManager.updateContext(pc, getBackend().getWorkPosition(), getBackend().getMachinePosition());

        probeService.performXYZProbe(pc);
    }

    @Override
    public String getProbeConfirmationText() {
        return Localization.getString("probe.action.xyz.confirmation");
    }
}
