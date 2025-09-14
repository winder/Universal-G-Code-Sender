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

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.platform.probe.ProbeParameters;
import com.willwinder.ugs.platform.probe.ProbeService;
import com.willwinder.ugs.platform.probe.ProbeSettings;
import com.willwinder.ugs.platform.probe.renderable.ProbePreviewManager;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

import java.util.logging.Logger;

@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "com.willwinder.ugs.platform.probe.actions.ProbeZAction")
@ActionRegistration(
        iconBase = ProbeZAction.BASE_ICON,
        displayName = "Probe and zero Z",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE_PROBE,
                position = 10)
})
public class ProbeZAction extends AbstractProbeAction {
    private static final Logger LOGGER = Logger.getLogger(ProbeZAction.class.getName());

    public static final String BASE_ICON = "com/willwinder/ugs/platform/probe/icons/zprobe.svg";
    public static final String BASE_ICON_LARGE = "com/willwinder/ugs/platform/probe/icons/zprobe24.svg";

    public ProbeZAction() {
        putValue("iconBase", BASE_ICON);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(BASE_ICON, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(BASE_ICON_LARGE, false));
        putValue("menuText", Localization.getString("probe.action.z"));
        putValue(NAME, Localization.getString("probe.action.z"));
    }

    @Override
    public void performProbeAction() {
        double probeDistance = calculateSafeProbeDistance();

        ProbeService probeService = Lookup.getDefault().lookup(ProbeService.class);
        ProbeParameters pc = new ProbeParameters(
                ProbeSettings.getSettingsProbeDiameter(), getBackend().getMachinePosition(),
                0., 0., probeDistance,
                0., 0., ProbeSettings.getzOffset(),
                0.0,
                ProbeSettings.getSettingsFastFindRate(), ProbeSettings.getSettingsSlowMeasureRate(),
                ProbeSettings.getSettingsRetractAmount(), ProbeSettings.getSettingsDelayAfterRetract(), getBackend().getSettings().getPreferredUnits(), ProbeSettings.getSettingsWorkCoordinate());

        ProbePreviewManager probePreviewManager = Lookup.getDefault().lookup(ProbePreviewManager.class);
        probePreviewManager.updateContext(pc, getBackend().getWorkPosition(), getBackend().getMachinePosition());

        probeService.performZProbe(pc);
    }

    protected double calculateSafeProbeDistance() {
        if (!shouldCompensateForSoftLimits()) {
            return getSettingProbeDistance();
        }

        double distanceToSoftLimitAfterProbing = getDistanceToSoftLimit();
        double probeDistance = getSettingProbeDistance() * UnitUtils.scaleUnits(getSettingsUnits(), UnitUtils.Units.MM);

        if (distanceToSoftLimitAfterProbing < 0.0) {
            LOGGER.info(String.format("Subtracting the soft limit overshoot %fmm from the probe distance %fmm to avoid soft limit alarm", distanceToSoftLimitAfterProbing, probeDistance));
            probeDistance = probeDistance - distanceToSoftLimitAfterProbing;
        }

        return probeDistance * UnitUtils.scaleUnits(UnitUtils.Units.MM, getSettingsUnits());
    }

    protected double getDistanceToSoftLimit() {
        return ControllerUtils.getDistanceToSoftLimit(getBackend().getController(), Axis.Z) + (getSettingProbeDistance() * UnitUtils.scaleUnits(getSettingsUnits(), UnitUtils.Units.MM));
    }

    protected boolean shouldCompensateForSoftLimits() {
        try {
            return getSettingsCompensateForSoftLimits() && getBackend().getController().getFirmwareSettings().isSoftLimitsEnabled();
        } catch (FirmwareSettingsException e) {
            return false;
        }
    }

    protected UnitUtils.Units getSettingsUnits() {
        return ProbeSettings.getSettingsUnits();
    }

    protected double getSettingProbeDistance() {
        return ProbeSettings.getzDistance();
    }

    protected boolean getSettingsCompensateForSoftLimits() {
        return ProbeSettings.getCompensateForSoftLimits();
    }

    @Override
    public String getProbeConfirmationText() {
        return Localization.getString("probe.action.z.confirmation");
    }
}
