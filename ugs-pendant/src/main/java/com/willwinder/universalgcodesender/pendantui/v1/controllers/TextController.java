package com.willwinder.universalgcodesender.pendantui.v1.controllers;

import com.willwinder.universalgcodesender.i18n.Localization;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/text")
public class TextController {
    @GET
    @Path("getTexts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        return Response.ok(Localization.getStrings()).build();
    }
}
