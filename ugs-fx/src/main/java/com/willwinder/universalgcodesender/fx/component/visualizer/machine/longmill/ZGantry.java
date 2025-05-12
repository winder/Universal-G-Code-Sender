package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineModel;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.Spindle;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.MGN12Block;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.ZGantryPlate;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class ZGantry extends Group {
    public ZGantry(MachineModel model) {
        ZGantryPlate zGantryPlate = new ZGantryPlate();
        zGantryPlate.getTransforms().add(new Translate(0, 2, 0));
        getChildren().add(zGantryPlate);

        Spindle spindle = new Spindle();
        spindle.getTransforms().setAll(new Translate(0, -50, -140));
        getChildren().add(spindle);

        MGN12Block mgn12Block1 = new MGN12Block();
        mgn12Block1.getTransforms().setAll(
                new Rotate(90, Rotate.X_AXIS),
                new Translate(-51, -46, 4));
        getChildren().add(mgn12Block1);

        MGN12Block mgn12Block2 = new MGN12Block();
        mgn12Block2.getTransforms().setAll(
                new Rotate(90, Rotate.X_AXIS),
                new Translate(50, -46, 4));
        getChildren().add(mgn12Block2);

        translateZProperty().bind(model.machinePositionZProperty().subtract(model.machineSizeZProperty()).subtract(10));
    }
}
