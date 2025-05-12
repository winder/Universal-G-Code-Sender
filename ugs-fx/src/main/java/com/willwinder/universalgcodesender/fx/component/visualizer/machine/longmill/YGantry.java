package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineModel;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.VWheel;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.XRail;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.YGantryPlate;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class YGantry extends Group {
    YGantry(double width, MachineModel model) {
        XRail xRail = new XRail(width);
        xRail.getTransforms().add(new Rotate(-90, Rotate.Z_AXIS));
        xRail.getTransforms().add(new Rotate(180, Rotate.X_AXIS));
        xRail.getTransforms().add(new Translate(-181, -width - 13, -100));
        getChildren().add(xRail);

        YGantryPlate gantryPlate1 = new YGantryPlate();
        gantryPlate1.getTransforms().add(new Rotate(-90, Rotate.Z_AXIS));
        gantryPlate1.getTransforms().add(new Translate(-100, -18, 114));
        getChildren().add(gantryPlate1);

        addLeftSideWheels();

        YGantryPlate gantryPlate2 = new YGantryPlate();
        gantryPlate2.getTransforms().add(new Rotate(-90, Rotate.Z_AXIS));
        gantryPlate2.getTransforms().add(new Translate(-100, width - 12, 114));
        getChildren().add(gantryPlate2);

        addRightSideWheels(width);

        XGantry xGantry = new XGantry(model);
        xGantry.getTransforms().add(new Translate(100, 90.8, 140));
        getChildren().add(xGantry);

        translateYProperty().bind(model.machinePositionYProperty());
    }

    private void addLeftSideWheels() {
        VWheel wheel1 = new VWheel();
        wheel1.getTransforms().add(new Translate(-1, 22.3, 55));
        getChildren().add(wheel1);

        VWheel wheel2 = new VWheel();
        wheel2.getTransforms().add(new Translate(-1, 22.3, -53));
        getChildren().add(wheel2);

        VWheel wheel3 = new VWheel();
        wheel3.getTransforms().add(new Translate(-1, 120.3, 55));
        getChildren().add(wheel3);

        VWheel wheel4 = new VWheel();
        wheel4.getTransforms().add(new Translate(-1, 120.3, -53));
        getChildren().add(wheel4);
    }

    private void addRightSideWheels(double width) {
        VWheel wheel1 = new VWheel();
        wheel1.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        wheel1.getTransforms().add(new Translate(-width - 28, -22.3, 55));
        getChildren().add(wheel1);

        VWheel wheel2 = new VWheel();
        wheel2.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        wheel2.getTransforms().add(new Translate(-width - 28, -22.3, -53));
        getChildren().add(wheel2);

        VWheel wheel3 = new VWheel();
        wheel3.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        wheel3.getTransforms().add(new Translate(-width - 28, -120.3, 55));
        getChildren().add(wheel3);

        VWheel wheel4 = new VWheel();
        wheel4.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        wheel4.getTransforms().add(new Translate(-width - 28, -120.3, -53));
        getChildren().add(wheel4);
    }
}
