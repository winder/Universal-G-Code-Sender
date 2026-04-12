package com.willwinder.universalgcodesender.fx.component.dro;

import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.EnumMap;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MachineStatusPane extends GridPane {

    private static final int AXIS_START_ROW = 1;

    private final BackendAPI backend;
    private final StateLabel state;
    private final Map<Axis, AxisRow> axisLabels = new EnumMap<>(Axis.class);
    private final HighlightableLabel feedRate;
    private final HighlightableLabel spindleSpeed;
    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000");

    public MachineStatusPane() {
        int row = 0;
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dro.css")).toExternalForm());
        backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onUGSEvent);
        setMaxWidth(Double.MAX_VALUE);

        setColumnConstraints();

        setPadding(new Insets(10, 10, 10, 10));
        state = new StateLabel();
        add(state, row++, 0, 2, 1);

        feedRate = new HighlightableLabel("0", new Icon("icons/gauge.svg", 24));
        spindleSpeed = new HighlightableLabel("0", new Icon("icons/tool.svg", 24));
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
        if (event instanceof ControllerStatusEvent || event instanceof SettingChangedEvent) {
            updateState();
        }
    }

    private boolean containsAxis(Position position, Axis axis) {
        if (position == null) {
            return false;
        }

        double value = position.get(axis);
        return Double.isFinite(value);
    }

    private boolean shouldShowAxis(Axis axis, Position workPosition, Position machinePosition) {
        return containsAxis(workPosition, axis) || containsAxis(machinePosition, axis);
    }

    private void syncAxisRows(Position workPosition, Position machinePosition) {
        for (Axis axis : Axis.values()) {
            boolean shouldShow = shouldShowAxis(axis, workPosition, machinePosition);
            AxisRow axisRow = axisLabels.get(axis);

            if (shouldShow && axisRow == null) {
                axisRow = new AxisRow(axis, this::showWorkCoordinatePopup);
                axisLabels.put(axis, axisRow);
                add(axisRow, 0, AXIS_START_ROW, 2, 1);
            } else if (!shouldShow && axisRow != null) {
                getChildren().remove(axisRow);
                axisLabels.remove(axis);
            }
        }

        int row = AXIS_START_ROW;
        for (Axis axis : Axis.values()) {
            AxisRow axisRow = axisLabels.get(axis);
            if (axisRow != null) {
                GridPane.setRowIndex(axisRow, row++);
            }
        }

        GridPane.setRowIndex(feedRate, row);
        GridPane.setRowIndex(spindleSpeed, row);
    }

    private void showWorkCoordinatePopup(AxisRow axisRow) {
        if (!backend.isConnected() || backend.getControllerState() != ControllerState.IDLE) {
            return;
        }

        Axis axis = axisRow.getAxis();
        UnitUtils.Units preferredUnits = backend.getSettings().getPreferredUnits();
        String currentValue = decimalFormatter.format(backend.getWorkPosition().getPositionIn(preferredUnits).get(axis));

        WorkCoordinatePopup popup = new WorkCoordinatePopup(
                "Set " + axis + " work position",
                currentValue,
                value -> {
                    try {
                        backend.setWorkPositionUsingExpression(axis, value);
                    } catch (Exception ex) {
                        GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
                    }
                });

        popup.showBelow(axisRow.getWorkCoordinateLabel());
    }

    private void updateState() {
        UnitUtils.Units preferredUnits = backend.getSettings().getPreferredUnits();
        Position workPosition = backend.getWorkPosition().getPositionIn(backend.getSettings().getPreferredUnits());
        Position machinePosition = backend.getMachinePosition().getPositionIn(backend.getSettings().getPreferredUnits());
        ControllerState controllerState = backend.getControllerState();

        state.setState(controllerState);

        Platform.runLater(() -> {
            syncAxisRows(workPosition, machinePosition);
            UnitValue feedSpeedWithUnit = new UnitValue(Unit.MM_PER_MINUTE, Optional.ofNullable(backend.getController()).map(c -> c.getControllerStatus().getFeedSpeed()).orElse(0.0));

            axisLabels.values().forEach(label -> {
                label.updatePosition(controllerState, workPosition, machinePosition);
                label.setDisable(controllerState != ControllerState.IDLE);
            });

            if (backend.getController() == null) return;
            feedRate.setText(String.format("%d", Math.round(feedSpeedWithUnit.convertTo(preferredUnits == UnitUtils.Units.MM ? Unit.MM_PER_MINUTE : Unit.INCHES_PER_MINUTE).doubleValue())));
            spindleSpeed.setText(String.format("%d", Math.round(backend.getController().getControllerStatus().getSpindleSpeed())));
        });

        setDisabled(controllerState != ControllerState.IDLE);
        feedRate.setDisable(controllerState != ControllerState.IDLE);
        spindleSpeed.setDisable(controllerState != ControllerState.IDLE);
    }
}
