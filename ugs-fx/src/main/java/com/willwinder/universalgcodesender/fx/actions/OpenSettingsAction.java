package com.willwinder.universalgcodesender.fx.actions;


import com.willwinder.universalgcodesender.fx.stage.SettingsStage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Window;

public class OpenSettingsAction extends BaseAction {

    private static final String ICON_BASE = "icons/settings.svg";


    public OpenSettingsAction() {
        super("Open settings", ICON_BASE);
    }

    @Override
    public void handleAction(ActionEvent event) {
        // Connect modal
        Window window = ((Node) event.getSource()).getScene().getWindow();
        SettingsStage modal = new SettingsStage(window);
        modal.showAndWait();
    }
}
