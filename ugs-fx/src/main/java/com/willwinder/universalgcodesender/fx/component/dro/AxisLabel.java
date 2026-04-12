package com.willwinder.universalgcodesender.fx.component.dro;

import com.willwinder.universalgcodesender.fx.settings.Settings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class AxisLabel extends HBox {
    private static final Duration HIGHLIGHT_TIME = Duration.millis(500);

    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000", Localization.dfs);
    private final Label workCoordinate;
    private final Label machineCoordinate;
    private final Axis axis;
    private final Label workUnitsLabel;
    private final Label machineUnitsLabel;
    private final PauseTransition highlightTimer;

    public AxisLabel(Axis axis) {
        super(5);
        this.axis = axis;
        getStyleClass().add("axis-label");
        getChildren().add(new Label(axis.toString()));

        workCoordinate = new Label();
        workCoordinate.setAlignment(Pos.CENTER_RIGHT);
        workCoordinate.setMaxWidth(Double.MAX_VALUE);
        workCoordinate.setWrapText(false);
        workCoordinate.getStyleClass().add("work-coordinate");
        workCoordinate.setCursor(Cursor.HAND);

        workUnitsLabel = new Label(UnitUtils.Units.MM.abbreviation);
        workUnitsLabel.getStyleClass().add("units");
        workUnitsLabel.setMaxHeight(Double.MAX_VALUE);
        workUnitsLabel.setAlignment(Pos.BOTTOM_RIGHT);

        machineCoordinate = new Label();
        machineCoordinate.setAlignment(Pos.CENTER_RIGHT);
        machineCoordinate.setMaxWidth(Double.MAX_VALUE);
        machineCoordinate.getStyleClass().add("machine-coordinate");

        machineUnitsLabel = new Label(UnitUtils.Units.MM.abbreviation);
        machineUnitsLabel.getStyleClass().add("units");
        machineUnitsLabel.setMaxHeight(Double.MAX_VALUE);
        machineUnitsLabel.setAlignment(Pos.BOTTOM_RIGHT);

        HBox workHbox = new HBox(workCoordinate, workUnitsLabel);
        HBox.setHgrow(workCoordinate, Priority.ALWAYS);

        HBox machineHbox = new HBox(machineCoordinate, machineUnitsLabel);
        machineHbox.managedProperty().bind(Settings.getInstance().showMachinePositionProperty());
        machineHbox.visibleProperty().bind(Settings.getInstance().showMachinePositionProperty());
        HBox.setHgrow(machineCoordinate, Priority.ALWAYS);

        VBox labels = new VBox(0, workHbox, machineHbox);
        labels.setFillWidth(true);
        labels.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(labels, Priority.ALWAYS);

        getChildren().add(labels);

        highlightTimer = new PauseTransition(HIGHLIGHT_TIME);
        highlightTimer.setOnFinished(e -> getStyleClass().remove("highlight"));

        workCoordinate.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.equals(oldValue, newValue)) {
                flashHighlight();
            }
        });

        machineCoordinate.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.equals(oldValue, newValue)) {
                flashHighlight();
            }
        });

        BackendAPI backendAPI = LookupService.lookup(BackendAPI.class);
        UnitUtils.Units preferredUnits = backendAPI.getSettings().getPreferredUnits();
        updatePosition(ControllerState.DISCONNECTED, Position.ZERO.getPositionIn(preferredUnits), Position.ZERO.getPositionIn(preferredUnits));
    }

    public void setOnWorkCoordinateClicked(Runnable onWorkCoordinateClicked) {
        workCoordinate.setOnMouseClicked(event -> {
            if (!isDisabled() && onWorkCoordinateClicked != null) {
                onWorkCoordinateClicked.run();
            }
        });
    }

    public Label getWorkCoordinateLabel() {
        return workCoordinate;
    }

    public void updatePosition(ControllerState state, Position workPosition, Position machinePosition) {
        if (state == ControllerState.DISCONNECTED) {
            getStyleClass().remove("connected");
        }

        Platform.runLater(() -> {
            double work = workPosition.get(axis);
            double machine = machinePosition.get(axis);
            workUnitsLabel.setText(workPosition.getUnits() == UnitUtils.Units.MM ? Unit.MM.getAbbreviation() : Unit.INCH.getAbbreviation());
            machineUnitsLabel.setText(machinePosition.getUnits() == UnitUtils.Units.MM ? Unit.MM.getAbbreviation() : Unit.INCH.getAbbreviation());
            workCoordinate.setText(decimalFormatter.format(work));
            machineCoordinate.setText(decimalFormatter.format(machine));
        });
    }

    private void flashHighlight() {
        if (!getStyleClass().contains("highlight")) {
            getStyleClass().add("highlight");
        }
        highlightTimer.playFromStart();
    }
}
