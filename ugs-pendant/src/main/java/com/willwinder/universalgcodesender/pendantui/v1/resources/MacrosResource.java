package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.v1.model.Macro;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/v1/macros")
public class MacrosResource {

    @Inject
    private BackendAPI backendAPI;

    @Inject
    private JogService jogService;

    @GET
    @Path("getMacroList")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Macro> getMacroList() {
        return SettingsFactory.loadSettings().getMacros()
                .stream()
                .map(macro -> {
                    Macro result = new Macro();
                    result.setGcode(macro.getGcode());
                    result.setDescription(macro.getDescription());
                    result.setName(macro.getName());
                    return result;
                })
                .collect(Collectors.toList());
    }

    @POST
    @Path("runMacro")
    @Consumes(MediaType.APPLICATION_JSON)
    public void executeMacro(Macro macro) throws Exception {
        MacroHelper.executeCustomGcode(macro.getGcode(), backendAPI);
    }
}
