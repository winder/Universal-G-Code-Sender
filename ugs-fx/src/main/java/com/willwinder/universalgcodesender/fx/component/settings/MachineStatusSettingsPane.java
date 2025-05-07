package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MachineStatusSettingsPane extends VBox {

    private final BackendAPI backend;

    public MachineStatusSettingsPane() {
        setSpacing(20);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        addTitleSection();
        addShowMachinePositionSection();
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.machineStatus"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

    private void addShowMachinePositionSection() {
        CheckBox checkBox = new CheckBox(Localization.getString("settings.showMachinePosition"));
        checkBox.setSelected(backend.getSettings().isShowMachinePosition());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setShowMachinePosition(newValue));
        getChildren().add(checkBox);
    }
}