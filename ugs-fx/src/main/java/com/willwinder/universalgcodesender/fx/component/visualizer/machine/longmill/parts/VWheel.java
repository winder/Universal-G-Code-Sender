package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.STLModel;
import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VWheel extends Group {
    private static final Logger LOGGER = Logger.getLogger(VWheel.class.getName());

    public VWheel() {
        try {
            CSG csg = STLModel.readSTL("/model/longmill/vwheel.stl")
                    .scale(1000);
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(COLOR_DARK_GREY);
            material.setSpecularColor(Color.LIGHTGRAY);
            material.setSpecularPower(1);

            MeshView meshView = csg.getMesh();
            meshView.setMaterial(material);
            getChildren().add(meshView);

            Cylinder cylinder = new Cylinder(7, 10);
            cylinder.setMaterial(material);
            cylinder.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
            getChildren().add(cylinder);

            Cylinder bolt = new Cylinder(3, 10);
            bolt.setMaterial(material);
            bolt.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
            bolt.getTransforms().add(new Translate(0, -10, 0));
            getChildren().add(bolt);
        } catch (IOException | URISyntaxException | NullPointerException e) {
            LOGGER.log(Level.INFO, "Could not load model", e);
        }
    }
}
