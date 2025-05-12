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

public class GantrySide extends Group {
    private static final Logger LOGGER = Logger.getLogger(GantrySide.class.getName());

    public GantrySide() {
        try {
            URI uri = Objects.requireNonNull(getClass().getResource("/model/genmitsu-3020-promax-gantry-side.svg")).toURI();
            CSG csg = SVGLoad.extrude(uri, getThickness())
                    .get(0)
                    .rotz(90)
                    .roty(90)
                    .move(10, 80, 0);

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

    public double getThickness() {
        return 10;
    }

    public double getWidth() {
        return 80;
    }

    public double getHeight() {
        return 254;
    }
}
