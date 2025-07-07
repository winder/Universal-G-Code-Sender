package com.willwinder.universalgcodesender.fx.component.visualizer.machine.common;

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

public class Extrusion2060 extends Group {
    private static final Logger LOGGER = Logger.getLogger(Extrusion4040.class.getName());

    public Extrusion2060(double length) {
        try {
            URI uri = Objects.requireNonNull(getClass().getResource("/model/tslot-2060.svg")).toURI();
            CSG csg = SVGLoad.extrude(uri, length)
                    .get(0)
                    .rotx(90)
                    .move(0, 0, getHeight());

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(COLOR_ALUMINIUM);
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(1);

            MeshView meshView = csg.getMesh();
            meshView.setMaterial(material);
            getChildren().add(meshView);
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Could not load SVG model", e);
        }
    }

    public double getWidth() {
        return 20;
    }

    public double getHeight() {
        return 60;
    }
}
