package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsu3020;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.Machine;
import javafx.scene.Group;
import javafx.scene.paint.Color;

public class Genmitsu3020 extends Machine {
    public static final Color COLOR_TITANIUM = Color.web("#797982");
    public static final Color COLOR_ALUMINIUM = Color.web("#A9ACB6");
    public static final Color COLOR_STEEL = Color.web("#B5C0C9");

    public Genmitsu3020() {
        double basePlateWidth = 300;
        double basePlateHeight = 200;
        double basePlateDepth = 64;


        double baseWidth = basePlateWidth + 58;
        double baseDepth = basePlateHeight + 110;

        Group machineGroup = new Group();

        BasePlate basePlate = new BasePlate(basePlateWidth, basePlateHeight, 10);
        basePlate.setTranslateX(30);
        machineGroup.getChildren().add(basePlate);

        Base base = new Base(baseWidth, baseDepth);
        base.setTranslateZ(-base.getHeight());
        machineGroup.getChildren().add(base);


        Gantry gantry = new Gantry(baseWidth + 20);
        gantry.setTranslateX(-gantry.getSideDepth() );
        gantry.setTranslateZ(-base.getHeight());
        gantry.setTranslateY(baseDepth - gantry.getSideWidth() - base.getFrontThickness());
        machineGroup.getChildren().add(gantry);
        getChildren().add(machineGroup);

        // Align the spindle with the machine zero
        gantry.getHeadstock().translateXProperty().bind(machinePositionXProperty().add(2).add(basePlateWidth));
        basePlate.translateYProperty().bind(machinePositionYProperty().multiply(-1d).subtract(35.5));
        gantry.getHeadstock().getSpindle().translateZProperty().bind(machinePositionZProperty().add(basePlateDepth));

        // Align the machine with the current coordinate
        translateXProperty().bind(workPositionXProperty().subtract(machinePositionXProperty()).subtract(30).subtract(basePlateWidth));
        translateYProperty().bind(workPositionYProperty().subtract(basePlateHeight - 35.5));
        translateZProperty().bind(workPositionZProperty().subtract(machinePositionZProperty()).subtract(10).subtract(basePlateDepth));
    }


}
