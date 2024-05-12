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
package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.v1.model.Settings;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/settings")
public class SettingsResource {
    @Inject
    private BackendAPI backendAPI;

    @GET
    @Path("getSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Settings getSettings() {
        com.willwinder.universalgcodesender.utils.Settings settings = backendAPI.getSettings();
        Settings response = new Settings();
        response.setJogFeedRate(settings.getJogFeedRate());
        response.setJogStepSizeXY(settings.getManualModeStepSize());
        response.setJogStepSizeZ(settings.getZJogStepSize());
        response.setPreferredUnits(settings.getPreferredUnits());
        response.setPort(settings.getPort());
        response.setPortRate(settings.getPortRate());
        response.setFirmwareVersion(settings.getFirmwareVersion());
        response.setUseZStepSize(settings.useZStepSize());
        return response;
    }

    @POST
    @Path("setSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public void setSettings(Settings settings) throws Exception {
        com.willwinder.universalgcodesender.utils.Settings backendSettings = backendAPI.getSettings();
        backendSettings.setJogFeedRate(settings.getJogFeedRate());
        backendSettings.setManualModeStepSize(settings.getJogStepSizeXY());
        backendSettings.setZJogStepSize(settings.getJogStepSizeZ());
        backendSettings.setPreferredUnits(settings.getPreferredUnits());
        backendSettings.setPort(settings.getPort());
        backendSettings.setPortRate(settings.getPortRate());
        backendSettings.setFirmwareVersion(settings.getFirmwareVersion());
        backendSettings.setUseZStepSize(settings.isUseZStepSize());
    }
}
