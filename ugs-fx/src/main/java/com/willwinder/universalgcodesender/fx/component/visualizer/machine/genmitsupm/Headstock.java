package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.Extrusion2060;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.Rail;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.Spindle;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;

public class Headstock extends Group {

    private final Spindle spindle;

    public Headstock() {
        HeadstockTop headstockTop = new HeadstockTop();
        headstockTop.setTranslateZ(133);
        getChildren().add(headstockTop);

        HeadstockTop headstockBottom = new HeadstockTop();
        getChildren().add(headstockBottom);

        Extrusion2060 base = new Extrusion2060(133 - headstockBottom.getThickness());
        base.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        base.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        base.setTranslateZ(headstockBottom.getThickness());
        base.setTranslateX(8);
        base.setTranslateY(-base.getWidth());
        getChildren().add(base);

        Rail leftRail = new Rail(5, 133);
        leftRail.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        leftRail.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        leftRail.setTranslateZ(133/2d + headstockBottom.getThickness() / 2);
        leftRail.setTranslateX(10);
        leftRail.setTranslateY(-base.getHeight() + 10);
        getChildren().add(leftRail);


        Rail rightRail = new Rail(5, 133);
        rightRail.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        rightRail.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        rightRail.setTranslateZ(133/2d + headstockBottom.getThickness() /2);
        rightRail.setTranslateX(headstockTop.getWidth() - 10);
        rightRail.setTranslateY(-base.getHeight() + 10);
        getChildren().add(rightRail);

        Group spindleGroup = new Group();
        spindle = new Spindle();
        spindleGroup.getChildren().add(spindle);
        spindleGroup.setTranslateX(headstockTop.getWidth() / 2);
        spindleGroup.setTranslateY(-headstockTop.getWidth() / 3 -headstockTop.getHeight());
        spindleGroup.setTranslateZ(-80);
        getChildren().add(spindleGroup);
    }

    public Spindle getSpindle() {
        return spindle;
    }
}
