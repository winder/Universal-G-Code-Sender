package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.universalgcodesender.fx.component.visualizer.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class VisualizerSettingsPane extends BorderPane {

    private final VBox settingsGroup;

    public VisualizerSettingsPane() {
        settingsGroup = new VBox();
        settingsGroup.setSpacing(20);
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
