package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.AvailableLanguages;
import com.willwinder.universalgcodesender.i18n.Language;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class GeneralSettingsPane extends VBox {

    private final BackendAPI backend;

    public GeneralSettingsPane() {
        setSpacing(20);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        addTitleSection();
        addUnitSection();
        addLanguageSection();
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.general"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

    private void addUnitSection() {
        Label unitLabel = new Label(Localization.getString("gcode.setting.units"));

        ToggleGroup unitToggleGroup = new ToggleGroup();
        RadioButton metricRadio = new RadioButton(Localization.getString("settings.metric"));
        metricRadio.setToggleGroup(unitToggleGroup);

        RadioButton imperialRadio = new RadioButton(Localization.getString("settings.imperial"));
        imperialRadio.setToggleGroup(unitToggleGroup);

        metricRadio.setSelected(backend.getSettings().getPreferredUnits() == UnitUtils.Units.MM);
        imperialRadio.setSelected(backend.getSettings().getPreferredUnits() != UnitUtils.Units.MM);

        unitToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == metricRadio) {
                backend.getSettings().setPreferredUnits(UnitUtils.Units.MM);
            } else {
                backend.getSettings().setPreferredUnits(UnitUtils.Units.INCH);
            }
        });

        getChildren().add(new VBox(5, unitLabel, metricRadio, imperialRadio));
    }

    private void addLanguageSection() {
        Label label = new Label(Localization.getString("settings.language"));
        ComboBox<Language> languageComboBox = new ComboBox<>(FXCollections.observableList(new ArrayList<>(AvailableLanguages.getAvailableLanguages())));

        languageComboBox.getItems()
                .stream()
                .filter(language -> language.getLanguageCode().equals(backend.getSettings().getLanguage()))
                .findFirst()
                .ifPresent(language -> languageComboBox.getSelectionModel().select(language));

        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                backend.getSettings().setLanguage(newValue.getLanguageCode()));
        getChildren().add(new VBox(5, label, languageComboBox));
    }
}
