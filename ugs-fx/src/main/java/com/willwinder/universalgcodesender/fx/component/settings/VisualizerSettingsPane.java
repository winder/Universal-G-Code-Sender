package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.universalgcodesender.fx.component.visualizer.VisualizerSettings;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;


public class VisualizerSettingsPane extends BorderPane {

    private final VBox settingsGroup;

    public VisualizerSettingsPane() {
        settingsGroup = new VBox(10);
        addTitle(Localization.getString("settings.visualizer.machine"));
        addMachineCombo();

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

    private void addTitle(String text) {
        Label title = new Label(text);
        title.setFont(Font.font(16));
        settingsGroup.getChildren().add(title);
        VBox.setMargin(title, new Insets(10, 0, 0, 0));
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("platform.window.visualizer"));
        title.setPadding(new Insets(0, 0, 15, 0));
        title.setFont(Font.font(20));
        setTop(title);
    }

    private void addColor(String text, StringProperty stringProperty) {
        Color value = stringProperty.map(Color::web).getValue();
        VBox vBox = new VBox(5);
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
