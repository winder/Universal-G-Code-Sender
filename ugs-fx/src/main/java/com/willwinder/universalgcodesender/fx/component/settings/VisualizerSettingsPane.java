package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Consumer;


public class VisualizerSettingsPane extends BorderPane {

    private final BackendAPI backend;
    private final VBox settingsGroup;

    public VisualizerSettingsPane() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        settingsGroup = new VBox();
        settingsGroup.setSpacing(20);
        addColor(Localization.getString("platform.visualizer.color.rapid"), Color.web(backend.getSettings().getFxSettings().getVisualizerRapidColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerRapidColor(value));
        addColor(Localization.getString("platform.visualizer.color.linear.min.speed"), Color.web(backend.getSettings().getFxSettings().getVisualizerFeedMinColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerFeedMinColor(value));
        addColor(Localization.getString("platform.visualizer.color.linear"), Color.web(backend.getSettings().getFxSettings().getVisualizerFeedMaxColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerFeedMaxColor(value));
        addColor(Localization.getString("platform.visualizer.color.spindle.min.speed"), Color.web(backend.getSettings().getFxSettings().getVisualizerSpindleMinColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerSpindleMinColor(value));
        addColor(Localization.getString("platform.visualizer.color.spindle.max.speed"), Color.web(backend.getSettings().getFxSettings().getVisualizerSpindleMaxColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerSpindleMaxColor(value));
        addColor(Localization.getString("platform.visualizer.color.arc"), Color.web(backend.getSettings().getFxSettings().getVisualizerArcColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerArcColor(value));
        addColor(Localization.getString("platform.visualizer.color.completed"), Color.web(backend.getSettings().getFxSettings().getVisualizerCompletedColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerCompletedColor(value));
        addColor(Localization.getString("platform.visualizer.color.plunge"), Color.web(backend.getSettings().getFxSettings().getVisualizerPlungeColor()), (value) -> backend.getSettings().getFxSettings().setVisualizerPlungeColor(value));

        addTitleSection();
        setCenter(settingsGroup);
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("platform.window.visualizer"));
        title.setPadding(new Insets(0, 0, 15, 0));
        title.setFont(Font.font(20));
        setTop(title);
    }

    private void addColor(String text, Color value, Consumer<String> onChange) {
        VBox vBox = new VBox(5);
        ColorPicker colorPicker = new ColorPicker(value);
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> onChange.accept(Colors.toWeb(newValue)));
        Label label = new Label(text);
        vBox.getChildren().add(label);
        vBox.getChildren().add(colorPicker);
        settingsGroup.getChildren().add(vBox);
    }

}
