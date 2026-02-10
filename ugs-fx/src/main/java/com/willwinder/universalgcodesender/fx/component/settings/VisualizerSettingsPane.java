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

import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineType;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;


public class VisualizerSettingsPane extends BorderPane {

    private final VBox settingsGroup;

    public VisualizerSettingsPane() {
        settingsGroup = new VBox(16);
        addTitle(Localization.getString("settings.visualizer.machine"));
        addMachineCombo();

        addTitle("Mouse controls");
        addMouseControls();

        addTitle(Localization.getString("settings.visualizer.colors"));
        addColor(Localization.getString("platform.visualizer.color.rapid"), VisualizerSettings.getInstance().colorRapidProperty());
        addColor(Localization.getString("platform.visualizer.color.linear.min.speed"), VisualizerSettings.getInstance().colorFeedMinProperty());
        addColor(Localization.getString("platform.visualizer.color.linear"), VisualizerSettings.getInstance().colorFeedMaxProperty());
        addColor(Localization.getString("platform.visualizer.color.spindle.min.speed"), VisualizerSettings.getInstance().colorSpindleMinProperty());
        addColor(Localization.getString("platform.visualizer.color.spindle.max.speed"), VisualizerSettings.getInstance().colorSpindleMaxProperty());
        addColor(Localization.getString("platform.visualizer.color.arc"), VisualizerSettings.getInstance().colorArcProperty());
        addColor(Localization.getString("platform.visualizer.color.completed"), VisualizerSettings.getInstance().colorCompletedProperty());
        addColor(Localization.getString("platform.visualizer.color.plunge"), VisualizerSettings.getInstance().colorPlungeProperty());

        addTitleSection();
        setCenter(settingsGroup);
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
        settingsGroup.getChildren().add(machineTypeComboBox);

        CheckBox checkBox = new CheckBox();
        checkBox.setText("Show");
        checkBox.setTextAlignment(TextAlignment.LEFT);
        checkBox.selectedProperty().bindBidirectional(VisualizerSettings.getInstance().showMachineProperty());
        settingsGroup.getChildren().add(checkBox);
    }

    private void addMouseControls() {
        // Invert zoom
        CheckBox invertZoom = new CheckBox("Invert scroll wheel zoom");
        invertZoom.selectedProperty().bindBidirectional(
                VisualizerSettings.getInstance().invertZoomProperty()
        );
        settingsGroup.getChildren().add(invertZoom);

        // Pan binding
        VBox panBox = new VBox(8);
        panBox.getChildren().add(new Label("Pan mouse button"));

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

        HBox panControls = new HBox(16, panButton, panModifier);
        panBox.getChildren().add(panControls);
        VBox.setMargin(panBox, new Insets(0, 0, 5, 0));

        settingsGroup.getChildren().add(panBox);

        // Rotate binding
        VBox rotateBox = new VBox(8);
        rotateBox.getChildren().add(new Label("Rotate mouse button"));

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

        HBox rotateControls = new HBox(16, rotateButton, rotateModifier);
        rotateBox.getChildren().add(rotateControls);

        settingsGroup.getChildren().add(rotateBox);
    }

    private void addTitle(String text) {
        Label title = new Label(text);
        title.setFont(Font.font(16));
        settingsGroup.getChildren().add(title);
        VBox.setMargin(title, new Insets(16, 0, 0, 0));
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("platform.window.visualizer"));
        title.setPadding(new Insets(0, 0, 16, 0));
        title.setFont(Font.font(20));
        setTop(title);
    }

    private void addColor(String text, StringProperty stringProperty) {
        Color value = stringProperty.map(Color::web).getValue();
        VBox vBox = new VBox(8);
        ColorPicker colorPicker = new ColorPicker(value);
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            String value1 = Colors.toWeb(newValue);
            stringProperty.set(value1);
        });
        colorPicker.setMinHeight(24);
        Label label = new Label(text);
        vBox.getChildren().add(label);
        vBox.getChildren().add(colorPicker);
        settingsGroup.getChildren().add(vBox);
    }

}
