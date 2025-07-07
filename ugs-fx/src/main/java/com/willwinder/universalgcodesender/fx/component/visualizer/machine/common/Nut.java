package com.willwinder.universalgcodesender.fx.component.visualizer.machine.common;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;
import eu.mihosoft.vrl.v3d.Hexagon;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class Nut extends Group {
    public Nut() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(COLOR_DARK_GREY);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(1);

        MeshView meshView = new Hexagon(16, 12).toCSG().getMesh();
        meshView.setMaterial(material);
        getChildren().add(meshView);
    }
}
