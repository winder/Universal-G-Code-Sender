package com.willwinder.universalgcodesender.fx.component.visualizer.machine.common;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_STEEL;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

public class Rail extends Group {
    public Rail(double radius, double length) {
        Cylinder cylinder = new Cylinder(radius, length, 32);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(COLOR_STEEL);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(1);
        cylinder.setMaterial(material);

        getChildren().add(cylinder);
    }
}
