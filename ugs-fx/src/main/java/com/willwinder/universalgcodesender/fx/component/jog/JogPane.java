/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class JogPane extends BorderPane {
    private final DirectionalPadPane directionalPadPane;
    private final BackendAPI backend;

    private static final ObservableList<Integer> FEED_RATES = FXCollections.observableArrayList(10, 20, 50, 100, 200, 500, 1000, 2000, 5000);
    private static final ObservableList<Double> STEP_SIZES = FXCollections.observableArrayList(0.001, 0.01, 0.1, 1d, 10d, 100d);
    private final ComboBox<Integer> feedRate;
    private final ComboBox<Double> stepSizeXY;
    private final ComboBox<Double> stepSizeZ;

    public JogPane() {
        setPadding(new Insets(10, 10, 10, 10));

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);

        directionalPadPane = new DirectionalPadPane();
        directionalPadPane.setDisable(!backend.isConnected());
        setCenter(directionalPadPane);

        HBox settings = new HBox();
        settings.setPadding(new Insets(10, 10, 10, 10));
        settings.setSpacing(10);

        feedRate = new ComboBox<>(FEED_RATES);
        feedRate.setEditable(true);
        feedRate.setValue((int) Math.round(backend.getSettings().getJogFeedRate()));
        feedRate.valueProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setJogFeedRate(newValue));
        feedRate.setConverter(new IntegerStringConverter());
        feedRate.setDisable(!backend.isConnected());
        Label label = new Label(Localization.getString("platform.plugin.jog.feedRate"));
        label.setLabelFor(feedRate);
        label.disableProperty().bind(feedRate.disabledProperty());
        settings.getChildren().add(new VBox(5, label, feedRate));

        stepSizeXY = new ComboBox<>(STEP_SIZES);
        stepSizeXY.setEditable(true);
        stepSizeXY.setValue(backend.getSettings().getManualModeStepSize());
        stepSizeXY.valueProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setManualModeStepSize(newValue));
        stepSizeXY.setConverter(new DoubleStringConverter());
        stepSizeXY.setDisable(!backend.isConnected());
        label = new Label(Localization.getString("platform.plugin.jog.stepSize"));
        label.setLabelFor(stepSizeXY);
        label.disableProperty().bind(stepSizeXY.disabledProperty());
        settings.getChildren().add(new VBox(5, label, stepSizeXY));

        stepSizeZ = new ComboBox<>(STEP_SIZES);
        stepSizeZ.setEditable(true);
        stepSizeZ.setValue(backend.getSettings().getZJogStepSize());
        stepSizeZ.valueProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setZJogStepSize(newValue));
        stepSizeZ.setConverter(new DoubleStringConverter());
        stepSizeZ.setDisable(!backend.isConnected());
        label = new Label(Localization.getString("platform.plugin.jog.stepSizeZ"));
        label.setLabelFor(stepSizeZ);
        label.disableProperty().bind(stepSizeZ.disabledProperty());
        settings.getChildren().add(new VBox(5, label, stepSizeZ));

        setBottom(settings);

        VerticalPad verticalPadPane = new VerticalPad();
        verticalPadPane.setDisable(!backend.isConnected());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent controllerStateEvent) {
            boolean canJog = !backend.isConnected() || !(controllerStateEvent.getState() == ControllerState.JOG ||
                    controllerStateEvent.getState() == ControllerState.IDLE);
            directionalPadPane.setDisable(canJog);
            feedRate.setDisable(canJog);
            stepSizeXY.setDisable(canJog);
            stepSizeZ.setDisable(canJog);
        } else if (event instanceof SettingChangedEvent) {
            feedRate.setValue((int) Math.round(backend.getSettings().getJogFeedRate()));
            stepSizeXY.setValue(backend.getSettings().getManualModeStepSize());
            stepSizeZ.setValue(backend.getSettings().getZJogStepSize());
        }
    }
}
