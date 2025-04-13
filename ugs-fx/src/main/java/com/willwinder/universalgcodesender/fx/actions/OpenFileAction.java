package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;


public class OpenFileAction extends BaseAction {

    public OpenFileAction() {
        titleProperty().set("Open");
        iconProperty().set("icons/open.svg");
    }

    @Override
    public void handle(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File(SettingsFactory.loadSettings().getLastWorkingDirectory()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gcode files", "*.nc", "*.txt", "*.gcode"));

        Window window = ((Node) event.getSource()).getScene().getWindow();

        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            System.out.println(selectedFile.getAbsolutePath());
            BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
            try {
                backendAPI.setGcodeFile(selectedFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
