package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_ALUMINIUM;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.svg.SVGLoad;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YRail extends Group {
    private static final Logger LOGGER = Logger.getLogger(YRail.class.getName());

    public YRail(double length) {
        try {
            URI uri = Objects.requireNonNull(getClass().getResource("/model/longmill/yrail.svg")).toURI();
            CSG csg = SVGLoad.extrude(uri, length)
                    .get(0)
                    .rotx(90)
                    .move(0, 0, 40);

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(COLOR_ALUMINIUM);
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(20);

            MeshView meshView = csg.getMesh();
            meshView.setMaterial(material);
            getChildren().add(meshView);
        } catch (IOException | URISyntaxException | NullPointerException e) {
            LOGGER.log(Level.INFO, "Could not load model", e);
        }
    }
}
