package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.v1.controllers.FilesController;
import com.willwinder.universalgcodesender.pendantui.v1.controllers.MachineController;
import com.willwinder.universalgcodesender.pendantui.v1.controllers.MacrosController;
import com.willwinder.universalgcodesender.pendantui.v1.controllers.SettingsController;
import com.willwinder.universalgcodesender.pendantui.v1.controllers.StatusController;
import com.willwinder.universalgcodesender.pendantui.v1.controllers.TextController;
import com.willwinder.universalgcodesender.services.JogService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        register(StatusController.class);
        register(MachineController.class);
        register(TextController.class);
        register(MacrosController.class);
        register(SettingsController.class);
        register(FilesController.class);
        register(MultiPartFeature.class);

        BackendAPI backendAPI = BackendAPIFactory.getInstance().getBackendAPI();
        JogService jogService = new JogService(backendAPI);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(backendAPI).to(BackendAPI.class);
                bind(jogService).to(JogService.class);
            }
        });
    }
}