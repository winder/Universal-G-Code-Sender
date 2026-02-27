/*
    Copyright 2026 Joacim Breiler

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

import com.willwinder.universalgcodesender.fx.component.BorderedTitledPane;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineType;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.StringConverter;


public class VisualizerSettingsPane extends BorderPane {

    private final VBox settingsGroup;

    public VisualizerSettingsPane() {
        settingsGroup = new VBox(32);
        addMachineCombo();
        addMouseControls();
        addColorSettings();
        addTitleSection();
        setCenter(settingsGroup);
    }

    private void addColorSettings() {
        settingsGroup.getChildren().add(new BorderedTitledPane(Localization.getString("settings.visualizer.colors"),
                new VBox(10,
                        createColorSetting(Localization.getString("platform.visualizer.color.rapid"), VisualizerSettings.getInstance().colorRapidProperty()),
                        createColorSetting(Localization.getString("platform.visualizer.color.linear.min.speed"), VisualizerSettings.getInstance().colorFeedMinProperty()),
                        createColorSetting(Localization.getString("platform.visualizer.color.linear"), VisualizerSettings.getInstance().colorFeedMaxProperty()),
                        createColorSetting(Localization.getString("platform.visualizer.color.spindle.min.speed"), VisualizerSettings.getInstance().colorSpindleMinProperty()),
                        createColorSetting(Localization.getString("platform.visualizer.color.spindle.max.speed"), VisualizerSettings.getInstance().colorSpindleMaxProperty()),
                        createColorSetting(Localization.getString("platform.visualizer.color.arc"), VisualizerSettings.getInstance().colorArcProperty()),
                        createColorSetting(Localization.getString("platform.visualizer.color.completed"), VisualizerSettings.getInstance().colorCompletedProperty()),
                        createColorSetting(Localization.getString("platform.visualizer.color.plunge"), VisualizerSettings.getInstance().colorPlungeProperty())
                )
        ));
    }

    private void addMachineCombo() {
        ComboBox<MachineType> machineTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(MachineType.values()));
        machineTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> VisualizerSettings.getInstance().machineModelProperty().set(newValue.name()));
        machineTypeComboBox.setValue(MachineType.fromValue(VisualizerSettings.getInstance().machineModelProperty().orElse(MachineType.UNKNOWN.name()).getValue()));
        machineTypeComboBox.setConverter(new StringConverter<>() {

            @Override
            public String toString(MachineType machineType) {
                return machineType.getName();
            }

            @Override
            public MachineType fromString(String name) {
                return MachineType.fromName(name);
            }
        });

        SwitchButton checkBox = new SwitchButton();
        checkBox.selectedProperty().bindBidirectional(VisualizerSettings.getInstance().showMachineProperty());

        settingsGroup.getChildren().add(new BorderedTitledPane(Localization.getString("settings.visualizer.machine"),
                        new VBox(10,
                                new SettingsRow("Show", checkBox),
                                new SettingsRow("Machine model", machineTypeComboBox))
                )
        );
    }

    private void addMouseControls() {
        VBox mouseControls = new VBox(10);

        // Invert zoom
        SwitchButton invertZoom = new SwitchButton();
        invertZoom.selectedProperty().bindBidirectional(
                VisualizerSettings.getInstance().invertZoomProperty()
        );
        mouseControls.getChildren().add(new SettingsRow("Invert scroll wheel zoom", "Inverts the zoom direction when using the mouse scroll wheel", invertZoom));

        // Pan binding
        ComboBox<String> panButton =
                new ComboBox<>(FXCollections.observableArrayList("PRIMARY", "MIDDLE", "SECONDARY"));
        panButton.valueProperty().bindBidirectional(
                VisualizerSettings.getInstance().panMouseButtonProperty()
        );
        panButton.setValue(
                VisualizerSettings.getInstance().panMouseButtonProperty().getValue()
        );

        ComboBox<VisualizerSettings.ModifierKey> panModifier =
                new ComboBox<>(FXCollections.observableArrayList(VisualizerSettings.ModifierKey.values()));
        panModifier.valueProperty().addListener((obs, oldVal, newVal) ->
                VisualizerSettings.getInstance().panModifierKeyProperty()
                        .set(newVal == null
                                ? VisualizerSettings.ModifierKey.NONE.name()
                                : newVal.name())
        );
        panModifier.setValue(
                VisualizerSettings.ModifierKey.fromString(
                        VisualizerSettings.getInstance().panModifierKeyProperty().getValue(),
                        VisualizerSettings.ModifierKey.NONE
                )
        );

        mouseControls.getChildren().add(new SettingsRow("Pan mouse button", panButton, panModifier));

        // Rotate binding
        ComboBox<String> rotateButton =
                new ComboBox<>(FXCollections.observableArrayList("PRIMARY", "MIDDLE", "SECONDARY"));
        rotateButton.valueProperty().bindBidirectional(
                VisualizerSettings.getInstance().rotateMouseButtonProperty()
        );
        rotateButton.setValue(
                VisualizerSettings.getInstance().rotateMouseButtonProperty().getValue()
        );

        ComboBox<VisualizerSettings.ModifierKey> rotateModifier =
                new ComboBox<>(FXCollections.observableArrayList(VisualizerSettings.ModifierKey.values()));
        rotateModifier.valueProperty().addListener((obs, oldVal, newVal) ->
                VisualizerSettings.getInstance().rotateModifierKeyProperty()
                        .set(newVal == null
                                ? VisualizerSettings.ModifierKey.NONE.name()
                                : newVal.name())
        );
        rotateModifier.setValue(
                VisualizerSettings.ModifierKey.fromString(
                        VisualizerSettings.getInstance().rotateModifierKeyProperty().getValue(),
                        VisualizerSettings.ModifierKey.NONE
                )
        );

        mouseControls.getChildren().add(new SettingsRow("Rotate mouse button", rotateButton, rotateModifier));

        settingsGroup.getChildren().add(new BorderedTitledPane("Mouse settings", mouseControls));
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("platform.window.visualizer"));
        title.setPadding(new Insets(0, 0, 16, 0));
        title.setFont(Font.font(20));
        setTop(title);
    }

    private SettingsRow createColorSetting(String text, StringProperty stringProperty) {
        Color value = stringProperty.map(Color::web).getValue();
        ColorPicker colorPicker = new ColorPicker(value);
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            String value1 = Colors.toWeb(newValue);
            stringProperty.set(value1);
        });
        colorPicker.setMinHeight(24);
        return new SettingsRow(text, colorPicker);
    }

}
