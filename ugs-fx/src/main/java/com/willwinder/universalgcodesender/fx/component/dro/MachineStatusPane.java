package com.willwinder.universalgcodesender.fx.component.dro;

import com.willwinder.universalgcodesender.fx.helper.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MachineStatusPane extends GridPane {

    private final BackendAPI backend;
    private final StateLabel state;
    private final List<AxisRow> axisLabels = new ArrayList<>();
    private final HiglightableLabel feedRate;
    private final HiglightableLabel spindleSpeed;

    public MachineStatusPane() {
        int row = 0;
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dro.css")).toExternalForm());
        backend = CentralLookup.lookup(BackendAPI.class).orElseThrow();
        backend.addUGSEventListener(this::onUGSEvent);
        setMaxWidth(Double.MAX_VALUE);

        setColumnConstraints();

        setPadding(new Insets(10, 10, 10, 10));
        state = new StateLabel();
        add(state, row++, 0, 2, 1);

        for (Axis axis : Axis.values()) {
            AxisRow axisLabel = new AxisRow(axis);
            add(axisLabel, 0, row++, 2, 1);
            axisLabels.add(axisLabel);
        }

        feedRate = new HiglightableLabel("0", new Icon("icons/gauge.svg", 24));
        spindleSpeed = new HiglightableLabel("0", new Icon("icons/tool.svg", 24));
        add(feedRate, 0, row);
        add(spindleSpeed, 1, row);

        updateState();
    }

    private void setColumnConstraints() {
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHgrow(Priority.ALWAYS);
        c0.setFillWidth(true);
        c0.setPercentWidth(50);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);
        c1.setPercentWidth(50);
        getColumnConstraints().setAll(c0, c1);

        setHgap(10);
        setVgap(10);
    }

    private void onUGSEvent(UGSEvent event) {
        if (event instanceof ControllerStatusEvent) {
            updateState();
        }
    }

    private void updateState() {
        state.setState(backend.getControllerState());
        axisLabels.forEach(label -> label.updatePosition(backend.getControllerState(), backend.getWorkPosition(), backend.getMachinePosition()));
        Platform.runLater(() -> {
            feedRate.setText(String.format("%d", Math.round(backend.getController().getControllerStatus().getFeedSpeed())));
            spindleSpeed.setText(String.format("%d", Math.round(backend.getController().getControllerStatus().getSpindleSpeed())));
        });

        setDisabled(backend.getControllerState() != ControllerState.IDLE);
        axisLabels.forEach(label -> label.setDisable(backend.getControllerState() != ControllerState.IDLE));
        feedRate.setDisable(backend.getControllerState() != ControllerState.IDLE);
        spindleSpeed.setDisable(backend.getControllerState() != ControllerState.IDLE);
    }
}
