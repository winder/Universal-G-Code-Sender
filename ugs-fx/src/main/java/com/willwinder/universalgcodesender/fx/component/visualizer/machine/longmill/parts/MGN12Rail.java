package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_STEEL;
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

public class MGN12Rail extends Group {
    private static final Logger LOGGER = Logger.getLogger(MGN12Rail.class.getName());

    public MGN12Rail(double length) {
        try {
            URI uri = Objects.requireNonNull(getClass().getResource("/model/longmill/mgn12.svg")).toURI();
            CSG csg = SVGLoad.extrude(uri, length)
                    .get(0);

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(COLOR_STEEL);
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(60);

            MeshView meshView = csg.getMesh();
            meshView.setMaterial(material);
            getChildren().add(meshView);
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Could not load SVG model", e);
        }
    }
}
