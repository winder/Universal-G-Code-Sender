package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;
import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.common.STLModel.readSTL;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YGantryPlate extends Group {
    private static final Logger LOGGER = Logger.getLogger(YGantryPlate.class.getName());

    public YGantryPlate() {
        try {
            CSG csg = readSTL("/model/longmill/ygantry-plate.stl")
                    .scale(1000);
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(COLOR_DARK_GREY);
            material.setSpecularColor(Color.LIGHTGRAY);
            material.setSpecularPower(1);

            MeshView meshView = csg.getMesh();
            meshView.setMaterial(material);
            getChildren().add(meshView);
        } catch (IOException | URISyntaxException | NullPointerException e) {
            LOGGER.log(Level.INFO, "Could not load model", e);
        }
    }
}
