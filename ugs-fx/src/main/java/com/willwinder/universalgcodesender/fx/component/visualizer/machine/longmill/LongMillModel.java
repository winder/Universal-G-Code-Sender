package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineModel;
import javafx.application.Platform;

public class LongMillModel extends MachineModel {
    public LongMillModel() {

        createModel();
        machineSizeXProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::createModel));
        machineSizeYProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::createModel));
        machineSizeZProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::createModel));

        translateXProperty().bind(workPositionXProperty().subtract(machinePositionXProperty()).subtract(100));
        translateYProperty().bind(workPositionYProperty().subtract(machinePositionYProperty()).subtract(40.8));
        translateZProperty().bind(workPositionZProperty().subtract(machinePositionZProperty()).add(10).add(machineSizeZProperty()));
    }

    private void createModel() {
        double railWidthOffset = 175;
        double railDepthOffset = 140;
        getChildren().clear();
        getChildren().add(new YGantry(machineSizeXProperty().add(railWidthOffset).get(), this));
        getChildren().add(new Base(machineSizeXProperty().add(railWidthOffset).get(), machineSizeYProperty().add(railDepthOffset).get()));
    }
}
