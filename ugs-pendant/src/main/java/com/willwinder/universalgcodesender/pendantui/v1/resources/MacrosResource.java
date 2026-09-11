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
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Machine", description = "Endpoints for managing the macros")
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
                .map(MacrosResource::toDto)
                .collect(Collectors.toList());
    }

    @POST
    @Path("runMacro")
    @Consumes(MediaType.APPLICATION_JSON)
    public void executeMacro(Macro macro) throws Exception {
        MacroHelper.executeCustomGcode(macro.getGcode(), backendAPI);
    }

    // Deliberately a single "replace the whole list" save, mirroring how the
    // native desktop Settings > Macros panel already works (edit a local
    // list, one Save writes it back) - covers create/update/delete/reorder
    // in one atomic operation instead of four endpoints that could partially
    // fail relative to each other. List order here becomes the stored order,
    // which is also what the native app and the right-rail run buttons use.
    @POST
    @Path("saveMacroList")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace the entire macro list")
    public List<Macro> saveMacroList(List<Macro> macros) {
        if (macros == null) {
            throw new BadRequestException("Missing macro list");
        }
        for (Macro macro : macros) {
            if (macro.getUuid() == null || macro.getUuid().isBlank()) {
                throw new BadRequestException("Every macro requires a uuid");
            }
            if (macro.getName() == null || macro.getName().isBlank()) {
                throw new BadRequestException("Every macro requires a name");
            }
        }

        List<com.willwinder.universalgcodesender.types.Macro> coreMacros = macros.stream()
                .map(MacrosResource::toCoreMacro)
                .collect(Collectors.toList());

        Settings settings = SettingsFactory.loadSettings();
        settings.setMacros(coreMacros);
        SettingsFactory.saveSettings(settings);

        return getMacroList();
    }

    private static Macro toDto(com.willwinder.universalgcodesender.types.Macro macro) {
        Macro result = new Macro();
        result.setUuid(macro.getUuid());
        result.setGcode(macro.getGcode());
        result.setDescription(macro.getDescription());
        result.setName(macro.getName());
        result.setColor(macro.getColor());
        result.setIcon(macro.getIcon());
        return result;
    }

    private static com.willwinder.universalgcodesender.types.Macro toCoreMacro(Macro dto) {
        com.willwinder.universalgcodesender.types.Macro macro = new com.willwinder.universalgcodesender.types.Macro(
                dto.getUuid(), dto.getName(), dto.getDescription(), dto.getGcode());
        macro.setColor(dto.getColor());
        macro.setIcon(dto.getIcon());
        return macro;
    }
}
