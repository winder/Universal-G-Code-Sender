package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_ALUMINIUM;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.RoundedCube;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class BasePlate extends Group {
    public BasePlate(double width, double height, double thickness) {
        CSG csg = new RoundedCube(width, height, thickness)
                .cornerRadius(0.5)
                .toCSG()
                .move(0, height, 0);

        MeshView meshView = csg
                .move(width / 2, height / 2, thickness / 2 - 0.2)

                .getMesh();

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(COLOR_ALUMINIUM);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(60);
        meshView.setMaterial(material);
        getChildren().add(meshView);
    }
}
