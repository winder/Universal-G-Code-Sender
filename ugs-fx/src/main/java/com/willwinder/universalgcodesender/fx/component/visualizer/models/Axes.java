package com.willwinder.universalgcodesender.fx.component.visualizer.models;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Axes extends Model {

    public static final double RADIUS = 0.14;

    private final Cylinder axisX;
    private final Cylinder axisY;
    private final Cylinder axisZ;

    public Axes() {
        axisX = new Cylinder(RADIUS, 200);
        axisX.getTransforms().addAll(new Rotate(90, Rotate.Z_AXIS), new Translate(0, -100, 0));
        axisX.setMaterial(new PhongMaterial(Color.RED));

        axisY = new Cylinder(RADIUS, 200);
        axisY.getTransforms().add(new Translate(0, 100, 0));
        axisY.setMaterial(new PhongMaterial(Color.GREEN));

        axisZ = new Cylinder(RADIUS, 200);
        axisZ.setMaterial(new PhongMaterial(Color.BLUE));
        axisZ.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Translate(0, 100, 0));

        getChildren().addAll(axisX, axisY, axisZ);
    }

    /**
     * Scales the cylinder thickness (radius) relative to {@link #RADIUS}.
     * Pass 1.0 for default thickness.
     */
    public void onZoomChange(double scale) {
        double s = clamp(scale, 0.05, 50.0);
        axisX.setRadius(RADIUS / s);
        axisY.setRadius(RADIUS / s);
        axisZ.setRadius(RADIUS / s);
    }

    @Override
    public boolean useLighting() {
        return false;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
