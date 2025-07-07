package com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;
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

public class HeadstockTop extends Group {
    private static final Logger LOGGER = Logger.getLogger(GantrySide.class.getName());
    private final double thickness = 10;

    public HeadstockTop() {
        try {
            URI uri = Objects.requireNonNull(getClass().getResource("/model/genmitsu-3020-promax-headstock-top.svg")).toURI();
            CSG csg = SVGLoad.extrude(uri, thickness)
                    .get(0)
                    .move(0, -getHeight(), 0);

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(COLOR_DARK_GREY);
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(60);

            MeshView meshView = csg.getMesh();
            meshView.setMaterial(material);
            getChildren().add(meshView);
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Could not load SVG model", e);
        }
    }

    public double getWidth() {
        return 76;
    }

    public double getHeight() {
        return 60;
    }

    public double getThickness() {
        return thickness;
    }
}
