package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class TerminalSettingsPane extends VBox {

    private final BackendAPI backend;

    public TerminalSettingsPane() {
        setSpacing(20);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        addTitleSection();
        addVerboseLoggingSection();
    }

    private void addVerboseLoggingSection() {
        CheckBox checkBox = new CheckBox(Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"));
        checkBox.setSelected(backend.getSettings().isVerboseOutputEnabled());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setVerboseOutputEnabled(newValue));
        getChildren().add(checkBox);
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.terminal"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

}