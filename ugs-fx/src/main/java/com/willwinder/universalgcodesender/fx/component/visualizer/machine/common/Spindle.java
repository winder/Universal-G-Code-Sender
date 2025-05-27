package com.willwinder.universalgcodesender.fx.component.visualizer.machine.common;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;
import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_STEEL;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class Spindle extends Group {
    public Spindle() {
        int slices = 128;
        CSG spindleBody = new Cylinder(25, 80, slices).toCSG().move(0, 0, 50);
        CSG axis = new Cylinder(6, 30, slices).toCSG().move(0,0,20);
        CSG tool = new Cylinder(1.5, 0.1, 20, slices).toCSG().rotx(180).move(0,0, 20);

        MeshView meshView = spindleBody
                .dumbUnion(axis)
                .dumbUnion(tool)
                .getMesh();

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(COLOR_STEEL);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(60);
        meshView.setMaterial(material);
        getChildren().add(meshView);

        CSG spindleFan = new Cylinder(28, 30, slices).toCSG().move(0, 0, 120);
        material = new PhongMaterial();
        material.setDiffuseColor(COLOR_DARK_GREY);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(60);
        meshView = spindleFan.getMesh();
        meshView.setMaterial(material);
        getChildren().add(meshView);


        Nut nut = new Nut();
        nut.setTranslateZ(21);
        getChildren().add(nut);
    }

    public double getWidth() {
        return 50;
    }
}
