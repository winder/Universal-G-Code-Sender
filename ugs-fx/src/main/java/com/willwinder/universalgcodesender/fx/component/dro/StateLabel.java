package com.willwinder.universalgcodesender.fx.component.dro;

import com.willwinder.universalgcodesender.listeners.ControllerState;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Objects;

public class StateLabel extends HBox {
    private final Label status;

    public StateLabel() {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dro.css")).toExternalForm());
        status = new Label();
        status.setMaxWidth(Double.MAX_VALUE);
        getChildren().add(status);
        HBox.setHgrow(status, Priority.ALWAYS);
        setState(ControllerState.DISCONNECTED);
    }

    public void setState(ControllerState state) {
        Platform.runLater(() -> {
            status.textProperty().set(state.toString());
            getStyleClass().clear();
            getStyleClass().add("state-label");
            getStyleClass().add(state.toString().toLowerCase());
        });
    }
}
