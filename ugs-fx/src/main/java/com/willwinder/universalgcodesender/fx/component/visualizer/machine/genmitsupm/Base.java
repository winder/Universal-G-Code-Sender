package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm;


import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.Extrusion4040;
import javafx.scene.Group;

public class Base extends Group {
    private final FrontPlate frontPlate;

    public Base(double width, double depth) {

        Group root = new Group();
        frontPlate = new FrontPlate(width);
        root.getChildren().add(frontPlate);

        FrontPlate backPlate = new FrontPlate(width);
        backPlate.setTranslateY(depth - backPlate.getDepth());
        root.getChildren().add(backPlate);

        Extrusion4040 leftBaseSide = new Extrusion4040(depth - backPlate.getDepth() - frontPlate.getDepth());
        leftBaseSide.setTranslateY(frontPlate.getDepth());
        root.getChildren().add(leftBaseSide);

        Extrusion4040 rightBaseSide = new Extrusion4040(depth - backPlate.getDepth() - frontPlate.getDepth());
        rightBaseSide.setTranslateY(frontPlate.getDepth());
        rightBaseSide.setTranslateX(width - rightBaseSide.getWidth());
        root.getChildren().add(rightBaseSide);
        root.setTranslateZ(40);

        getChildren().add(root);
    }

    public double getHeight() {
        return 40;
    }

    public double getFrontThickness() {
        return frontPlate.getDepth();
    }
}
