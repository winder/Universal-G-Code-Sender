package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.services.JogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class JogPane extends BorderPane {
    private final DirectionalPadPane directionalPadPane;
    private final BackendAPI backend;
    private final JogService jogService;

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
        directionalPadPane.setOnAction(this::onAction);
        directionalPadPane.setOnMousePressed(this::onMousePressed);
        directionalPadPane.setOnMouseReleased(this::onMouseReleased);
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

        jogService = new JogService(backend);
    }

    private void onMouseReleased(MouseEvent mouseEvent) {
        System.out.println("onMouseReleased: " + mouseEvent);
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        System.out.println("onMousePressed: " + mouseEvent);
    }

    private void onAction(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof JogButton jogButton) {
            JogButtonEnum button = jogButton.getButton();
            switch (button) {
                case BUTTON_XNEG:
                    jogService.adjustManualLocationXY(-1, 0);
                    break;
                case BUTTON_XPOS:
                    jogService.adjustManualLocationXY(1, 0);
                    break;
                case BUTTON_YNEG:
                    jogService.adjustManualLocationXY(0, -1);
                    break;
                case BUTTON_YPOS:
                    jogService.adjustManualLocationXY(0, 1);
                    break;
                case BUTTON_DIAG_XNEG_YNEG:
                    jogService.adjustManualLocationXY(-1, -1);
                    break;
                case BUTTON_DIAG_XNEG_YPOS:
                    jogService.adjustManualLocationXY(-1, 1);
                    break;
                case BUTTON_DIAG_XPOS_YNEG:
                    jogService.adjustManualLocationXY(1, -1);
                    break;
                case BUTTON_DIAG_XPOS_YPOS:
                    jogService.adjustManualLocationXY(1, 1);
                    break;
                case BUTTON_ZNEG:
                    jogService.adjustManualLocationZ(-1);
                    break;
                case BUTTON_ZPOS:
                    jogService.adjustManualLocationZ(1);
                    break;
                case BUTTON_ANEG:
                    jogService.adjustManualLocationABC(-1, 0, 0);
                    break;
                case BUTTON_APOS:
                    jogService.adjustManualLocationABC(1, 0, 0);
                    break;
                case BUTTON_BNEG:
                    jogService.adjustManualLocationABC(0, -1, 0);
                    break;
                case BUTTON_BPOS:
                    jogService.adjustManualLocationABC(0, 1, 0);
                    break;
                case BUTTON_CNEG:
                    jogService.adjustManualLocationABC(0, 0, -1);
                    break;
                case BUTTON_CPOS:
                    jogService.adjustManualLocationABC(0, 0, 1);
                    break;
                case BUTTON_CANCEL:
                    jogService.cancelJog();
                    break;
                default:
            }
        }
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
