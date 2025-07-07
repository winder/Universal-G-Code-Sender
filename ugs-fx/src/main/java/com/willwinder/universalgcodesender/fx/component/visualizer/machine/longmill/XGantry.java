package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineModel;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.MGN12Rail;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.VWheel;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.XZGantryPlate;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class XGantry extends Group {
    public XGantry(MachineModel model) {
        getChildren().add(new XZGantryPlate());

        VWheel wheel1 = new VWheel();
        wheel1.getTransforms().setAll(
                new Rotate(-90, Rotate.Z_AXIS),
                new Translate(-9, -70, 38));
        getChildren().add(wheel1);

        VWheel wheel2 = new VWheel();
        wheel2.getTransforms().setAll(
                new Rotate(-90, Rotate.Z_AXIS),
                new Translate(-9, -70 + 140, 38));
        getChildren().add(wheel2);

        VWheel wheel3 = new VWheel();
        wheel3.getTransforms().setAll(
                new Rotate(-90, Rotate.Z_AXIS),
                new Translate(-9, -70 + 1.5, -89));
        getChildren().add(wheel3);

        VWheel wheel4 = new VWheel();
        wheel4.getTransforms().setAll(
                new Rotate(-90, Rotate.Z_AXIS),
                new Translate(-9, -70 + 138, -89));
        getChildren().add(wheel4);


        MGN12Rail mgn12Rail1 = new MGN12Rail(200);
        mgn12Rail1.getTransforms().setAll(
                new Rotate(180, Rotate.Z_AXIS),
                new Translate(-57, 4, -100)
        );
        getChildren().add(mgn12Rail1);

        MGN12Rail mgn12Rail2 = new MGN12Rail(200);
        mgn12Rail2.getTransforms().setAll(
                new Rotate(180, Rotate.Z_AXIS),
                new Translate(45, 4, -100)
        );
        getChildren().add(mgn12Rail2);


        getChildren().add(new ZGantry(model));

        translateXProperty().bind(model.machinePositionXProperty());
    }
}
