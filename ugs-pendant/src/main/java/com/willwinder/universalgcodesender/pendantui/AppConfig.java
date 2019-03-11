package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.controllers.FilesController;
import com.willwinder.universalgcodesender.pendantui.controllers.MachineController;
import com.willwinder.universalgcodesender.pendantui.controllers.MacrosController;
import com.willwinder.universalgcodesender.pendantui.controllers.StatusController;
import com.willwinder.universalgcodesender.pendantui.controllers.TextController;
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