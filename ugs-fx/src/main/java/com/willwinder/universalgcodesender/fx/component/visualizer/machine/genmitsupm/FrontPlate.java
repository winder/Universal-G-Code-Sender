package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.RoundedCube;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class FrontPlate extends Group {
    private final double depth = 10;
    private final double height = 40;
    private final double width;

    public FrontPlate(double width) {
        this.width = width;
        CSG csg = new RoundedCube(width, height, depth)
                .cornerRadius(1)
                .toCSG()
                .rotx(90)
                .move(width / 2, depth / 2, -(height / 2));

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(COLOR_DARK_GREY);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(60);

        MeshView meshView = csg.getMesh();
        meshView.setMaterial(material);
        getChildren().add(meshView);
    }

    public double getDepth() {
        return depth;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }
}
