package com.willwinder.universalgcodesender.fx.component.dro;

import com.willwinder.universalgcodesender.fx.actions.ResetAxisZeroAction;
import com.willwinder.universalgcodesender.fx.actions.ReturnToAxisZeroAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class AxisRow extends HBox {
    private final AxisLabel axisLabel;

    public AxisRow(Axis axis) {
        getStyleClass().add("axis-row");
        setSpacing(5);
        setFillHeight(true);
        managedProperty().bind(visibleProperty());

        axisLabel = new AxisLabel(axis);
        axisLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(axisLabel, Priority.ALWAYS);

        ActionButton button = new ActionButton(new ResetAxisZeroAction(axis), 24, false);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setMinHeight(0);
        button.setMinWidth(40);


        ActionButton goToZeroButton = new ActionButton(new ReturnToAxisZeroAction(axis), 24, false);
        goToZeroButton.setMaxHeight(Double.MAX_VALUE);
        goToZeroButton.setMinHeight(0);
        goToZeroButton.setMinWidth(40);

        getChildren().add(axisLabel);
        getChildren().add(button);
        getChildren().add(goToZeroButton);

        updatePosition(ControllerState.DISCONNECTED, new Position(0, 0,0, UnitUtils.Units.MM), new Position(0, 0,0, UnitUtils.Units.MM));
    }

    public void updatePosition(ControllerState state, Position workPosition, Position machinePosition) {
        Platform.runLater(() -> axisLabel.updatePosition(state, workPosition, machinePosition));
    }
}
