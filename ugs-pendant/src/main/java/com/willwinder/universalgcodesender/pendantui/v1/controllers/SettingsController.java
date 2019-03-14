package com.willwinder.universalgcodesender.pendantui.v1.controllers;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.v1.model.Settings;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/settings")
public class SettingsController {

    @Inject
    private BackendAPI backendAPI;

    @GET
    @Path("getSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings() {
        try {
            com.willwinder.universalgcodesender.utils.Settings settings = backendAPI.getSettings();
            Settings response = new Settings();
            response.setJogFeedRate(settings.getJogFeedRate());
            response.setJogStepSizeXY(settings.getManualModeStepSize());
            response.setJogStepSizeZ(settings.getzJogStepSize());
            response.setPreferredUnits(settings.getPreferredUnits());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("setSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setSettings(Settings settings) {
        try {
            com.willwinder.universalgcodesender.utils.Settings backendSettings = backendAPI.getSettings();
            backendSettings.setJogFeedRate(settings.getJogFeedRate());
            backendSettings.setManualModeStepSize(settings.getJogStepSizeXY());
            backendSettings.setzJogStepSize(settings.getJogStepSizeZ());
            backendSettings.setPreferredUnits(settings.getPreferredUnits());
            backendAPI.applySettings(backendSettings);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
