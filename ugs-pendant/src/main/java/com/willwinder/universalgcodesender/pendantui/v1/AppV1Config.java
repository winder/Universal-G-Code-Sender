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
package com.willwinder.universalgcodesender.pendantui.v1;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.ExceptionMapper;
import com.willwinder.universalgcodesender.pendantui.html.StaticResource;
import com.willwinder.universalgcodesender.pendantui.v1.resources.FilesResource;
import com.willwinder.universalgcodesender.pendantui.v1.resources.MachineResource;
import com.willwinder.universalgcodesender.pendantui.v1.resources.MacrosResource;
import com.willwinder.universalgcodesender.pendantui.v1.resources.SettingsResource;
import com.willwinder.universalgcodesender.pendantui.v1.resources.StatusResource;
import com.willwinder.universalgcodesender.pendantui.v1.resources.TextResource;
import com.willwinder.universalgcodesender.services.JogService;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.WadlFeature;

public class AppV1Config extends ResourceConfig {
    public AppV1Config(BackendAPI backendAPI, JogService jogService) {

        register(StatusResource.class);
        register(MachineResource.class);
        register(TextResource.class);
        register(MacrosResource.class);
        register(SettingsResource.class);
        register(FilesResource.class);
        register(MultiPartFeature.class);
        register(WadlFeature.class);
        register(StaticResource.class);
        register(ExceptionMapper.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(backendAPI).to(BackendAPI.class);
                bind(jogService).to(JogService.class);
            }
        });
    }
}