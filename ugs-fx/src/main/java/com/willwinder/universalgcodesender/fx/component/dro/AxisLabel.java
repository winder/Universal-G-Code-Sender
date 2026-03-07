package com.willwinder.universalgcodesender.fx.component.dro;

import com.willwinder.universalgcodesender.fx.settings.Settings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;

public class AxisLabel extends HBox {
    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000", Localization.dfs);
    private final Label workCoordinate;
    private final Label machineCoordinate;
    private final Axis axis;
    private final Label unitsLabel;

    public AxisLabel(Axis axis) {
        super(5);
        this.axis = axis;
        getStyleClass().add("axis-label");
        getChildren().add(new Label(axis.toString()));

        workCoordinate = new Label();
        workCoordinate.setAlignment(Pos.CENTER_RIGHT);
        workCoordinate.setMaxWidth(Double.MAX_VALUE);
        workCoordinate.setWrapText(false);

        unitsLabel = new Label(UnitUtils.Units.MM.abbreviation);
        unitsLabel.getStyleClass().add("units");
        unitsLabel.setMaxHeight(Double.MAX_VALUE);
        unitsLabel.setAlignment(Pos.BOTTOM_RIGHT);

        machineCoordinate = new Label();
        machineCoordinate.setAlignment(Pos.CENTER_RIGHT);
        machineCoordinate.setMaxWidth(Double.MAX_VALUE);
        machineCoordinate.getStyleClass().add("machine-coordinate");
        machineCoordinate.managedProperty().bind(Settings.getInstance().showMachinePositionProperty());
        machineCoordinate.visibleProperty().bind(Settings.getInstance().showMachinePositionProperty());

        HBox hBox = new HBox(workCoordinate, unitsLabel);
        HBox.setHgrow(workCoordinate, Priority.ALWAYS);

        VBox labels = new VBox(0, hBox, machineCoordinate);
        labels.setFillWidth(true);
        labels.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(labels, Priority.ALWAYS);

        getChildren().add(labels);
        updatePosition(ControllerState.DISCONNECTED, Position.ZERO, Position.ZERO);
    }

    public void updatePosition(ControllerState state, Position workPosition, Position machinePosition) {
        if (state == ControllerState.DISCONNECTED) {
            getStyleClass().remove("connected");
        }

        Platform.runLater(() -> {
            double work = workPosition.get(axis);
            double machine = machinePosition.get(axis);
            unitsLabel.setText(workPosition.getUnits().abbreviation);
            workCoordinate.textProperty().set(decimalFormatter.format(work));
            machineCoordinate.textProperty().set(decimalFormatter.format(machine));
        });
    }
}
