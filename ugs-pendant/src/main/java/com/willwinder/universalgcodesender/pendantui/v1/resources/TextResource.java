package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.i18n.Localization;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/v1/text")
public class TextResource {
    @GET
    @Path("getTexts")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getStatus() {
        return Localization.getStrings();
    }
}
