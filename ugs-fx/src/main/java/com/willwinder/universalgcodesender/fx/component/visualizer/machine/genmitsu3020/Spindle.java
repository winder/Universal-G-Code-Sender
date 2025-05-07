package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsu3020;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class Spindle extends Group {
    public Spindle() {
        int slices = 64;
        CSG spindleBody = new Cylinder(25, 100, slices).toCSG().move(0, 0, 50);
        CSG axis = new Cylinder(8, 30, slices).toCSG().move(0,0,20);
        CSG tool = new Cylinder(1.5, 0.1, 20, slices).toCSG().rotx(180).move(0,0, 20);

        MeshView meshView = spindleBody
                .dumbUnion(axis)
                .dumbUnion(tool)
                .getMesh();

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Genmitsu3020.COLOR_TITANIUM);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(60);
        meshView.setMaterial(material);
        getChildren().add(meshView);
    }

    public double getWidth() {
        return 50;
    }
}
