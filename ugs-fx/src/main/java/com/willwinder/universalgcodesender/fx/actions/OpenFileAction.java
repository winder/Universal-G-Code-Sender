package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;


public class OpenFileAction extends BaseAction {

    private static final String ICON_BASE = "icons/open.svg";
    private final BackendAPI backend;

    public OpenFileAction() {
        super(LocalizingService.OpenTitle, ICON_BASE);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(!backend.isConnected() || backend.isIdle());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent || event instanceof FileStateEvent) {
            enabledProperty().set(!backend.isConnected() || backend.isIdle());
        }
    }

    @Override
    public void handle(ActionEvent event) {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File(backend.getSettings().getLastWorkingDirectory()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gcode files", "*.nc", "*.txt", "*.gcode"));

        Window window = ((Node) event.getSource()).getScene().getWindow();

        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            try {
                backend.setGcodeFile(selectedFile);
                backend.getSettings().setLastWorkingDirectory(selectedFile.getParent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
