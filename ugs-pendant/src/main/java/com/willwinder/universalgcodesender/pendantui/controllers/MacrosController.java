package com.willwinder.universalgcodesender.pendantui.controllers;

import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/macros")
public class MacrosController {

    @Inject
    private BackendAPI backendAPI;

    @Inject
    private JogService jogService;

    @GET
    @Path("getMacroList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMacroList() {
        return Response.ok(SettingsFactory.loadSettings().getMacros()).build();
    }
}
