package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.Extrusion2040;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.Rail;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;

public class Gantry extends Group {
    private final GantrySide leftGantrySide;
    private final Headstock headstock;

    public Gantry(double width) {
        Group root = new Group();

        leftGantrySide = new GantrySide();
        root.getChildren().add(leftGantrySide);

        GantrySide rightGantrySide = new GantrySide();
        rightGantrySide.setTranslateX(width - rightGantrySide.getThickness());
        root.getChildren().add(rightGantrySide);

        double length = width - rightGantrySide.getThickness() - leftGantrySide.getThickness();
        Extrusion2040 top = new Extrusion2040(length);
        top.setRotationAxis(Rotate.Z_AXIS);
        top.setRotate(90);
        top.setTranslateY(-length / 2 - top.getWidth() / 2 + leftGantrySide.getWidth());
        top.setTranslateX(length / 2);
        top.setTranslateZ(leftGantrySide.getHeight() - top.getHeight());
        root.getChildren().add(top);

        Extrusion2040 bottom = new Extrusion2040(width - rightGantrySide.getThickness() - leftGantrySide.getThickness());
        bottom.setRotationAxis(Rotate.Z_AXIS);
        bottom.setRotate(90);
        bottom.setTranslateY(-length / 2 - bottom.getWidth() / 2 + leftGantrySide.getWidth());
        bottom.setTranslateX(length / 2);
        bottom.setTranslateZ(leftGantrySide.getHeight() - bottom.getHeight() - bottom.getHeight() - 17);
        root.getChildren().add(bottom);

        double railLength = width - leftGantrySide.getThickness() * 2;
        Rail topRail = new Rail(5, railLength);
        topRail.setRotationAxis(Rotate.Z_AXIS);
        topRail.setRotate(90);
        topRail.setTranslateX(railLength / 2 + leftGantrySide.getThickness());
        topRail.setTranslateY(leftGantrySide.getWidth() / 2);
        topRail.setTranslateZ(leftGantrySide.getHeight() - 10);
        root.getChildren().add(topRail);

        Rail bottomRail = new Rail(5, railLength);
        bottomRail.setRotationAxis(Rotate.Z_AXIS);
        bottomRail.setRotate(90);
        bottomRail.setTranslateX(railLength / 2 + leftGantrySide.getThickness());
        bottomRail.setTranslateY(leftGantrySide.getWidth() / 2);
        bottomRail.setTranslateZ(leftGantrySide.getHeight() - bottom.getHeight() - bottom.getHeight() - 7);
        root.getChildren().add(bottomRail);


        headstock = new Headstock();
        headstock.setTranslateZ(130);
        headstock.setTranslateX(10);
        headstock.setTranslateY(30);
        root.getChildren().add(headstock);

        getChildren().add(root);
    }

    public Headstock getHeadstock() {
        return headstock;
    }

    public double getSideDepth() {
        return leftGantrySide.getThickness();
    }

    public double getSideWidth() {
        return leftGantrySide.getWidth();
    }
}
