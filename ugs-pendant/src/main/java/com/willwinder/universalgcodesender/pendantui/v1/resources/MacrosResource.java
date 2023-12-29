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

import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.v1.model.Macro;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Path("/macros")
public class MacrosResource {

    @Inject
    private BackendAPI backendAPI;

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
