/*
    Copyright 2025-2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.settings.Settings;
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
    private final VBox settings;

    public GeneralSettingsPane() {
        setSpacing(20);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        addTitleSection();

        settings = new VBox(10);
        getChildren().add(settings);
        addUnitSection();
        addLanguageSection();
        addToolbarSection();
    }

    private void addToolbarSection() {
        SwitchButton showToolbarText = new SwitchButton();
        showToolbarText.selectedProperty().bindBidirectional(Settings.getInstance().showToolbarTextProperty());
        settings.getChildren().add(new SettingsRow(Localization.getString("settings.showToolbarText"), showToolbarText));
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.general"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

    private void addUnitSection() {
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

        settings.getChildren().add(new SettingsRow( Localization.getString("gcode.setting.units"), metricRadio, imperialRadio));
    }

    private void addLanguageSection() {
        ComboBox<Language> languageComboBox = new ComboBox<>(FXCollections.observableList(new ArrayList<>(AvailableLanguages.getAvailableLanguages())));

        languageComboBox.getItems()
                .stream()
                .filter(language -> language.getLanguageCode().equals(backend.getSettings().getLanguage()))
                .findFirst()
                .ifPresent(language -> languageComboBox.getSelectionModel().select(language));

        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                backend.getSettings().setLanguage(newValue.getLanguageCode()));
        settings.getChildren().add(new SettingsRow(Localization.getString("settings.language"), languageComboBox));
    }
}
