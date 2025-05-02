package com.willwinder.universalgcodesender.fx.component.visualizer;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Axes extends Group {

    public static final double RADIUS = 0.2;

    public Axes() {
        Cylinder axisX = new Cylinder(RADIUS, 200);
        axisX.getTransforms().addAll(new Rotate(90, Rotate.Z_AXIS), new Translate(0, -100, 0));
        axisX.setMaterial(new PhongMaterial(Color.RED));

        Cylinder axisY = new Cylinder(RADIUS, 200);
        axisY.getTransforms().add(new Translate(0, 100, 0));
        axisY.setMaterial(new PhongMaterial(Color.GREEN));

        Cylinder axisZ = new Cylinder(RADIUS, 200);
        axisZ.setMaterial(new PhongMaterial(Color.BLUE));
        axisZ.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Translate(0, 100, 0));

        getChildren().addAll(axisX, axisY, axisZ);
    }
}
