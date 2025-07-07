package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineModel;
import javafx.application.Platform;
import javafx.scene.Group;

public class GenmitsuProMaxModel extends MachineModel {

    public GenmitsuProMaxModel() {
        super();
        createModel();
        machineSizeXProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::createModel));
        machineSizeYProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::createModel));
        machineSizeZProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::createModel));
    }

    private void createModel() {
        getChildren().clear();
        Group machineGroup = new Group();

        BasePlate basePlate = new BasePlate(machineSizeXProperty().doubleValue(), machineSizeYProperty().doubleValue(), 10);
        basePlate.setTranslateX(30);
        machineGroup.getChildren().add(basePlate);

        double baseWidth = machineSizeXProperty().doubleValue() + 58;
        double baseDepth = machineSizeYProperty().doubleValue() + 110;

        Base base = new Base(baseWidth, baseDepth);
        base.setTranslateZ(-base.getHeight());
        machineGroup.getChildren().add(base);

        Gantry gantry = new Gantry(baseWidth + 20);
        gantry.setTranslateX(-gantry.getSideDepth());
        gantry.setTranslateZ(-base.getHeight());
        gantry.setTranslateY(baseDepth - gantry.getSideWidth() - base.getFrontThickness());
        machineGroup.getChildren().add(gantry);
        getChildren().add(machineGroup);

        // Align the spindle with the machine zero
        gantry.getHeadstock().translateXProperty().bind(machinePositionXProperty().add(2));
        basePlate.translateYProperty()
                .bind(machinePositionYProperty()
                        .multiply(-1d)
                        .subtract(35.5));
        gantry.getHeadstock().getSpindle().translateZProperty().bind(machinePositionZProperty());

        // Align the machine with the current coordinate
        translateXProperty().bind(workPositionXProperty()
                        .subtract(machinePositionXProperty())
                .subtract(30));
        translateYProperty().bind(workPositionYProperty()
                .subtract(machineSizeYProperty())
                .add( 35.5));
        translateZProperty().bind(workPositionZProperty()
                .subtract(machinePositionZProperty())
                .subtract(10));
    }
}
