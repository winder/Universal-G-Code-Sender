package com.willwinder.universalgcodesender.fx.component.visualizer.models;

import com.willwinder.universalgcodesender.gcode.GcodeStats;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class Grid extends Model {

    // Grid settings
    private static final double GRID_STEP_COARSE_MM = 10.0;
    private static final double GRID_STEP_FINE_MM = 5.0;

    /**
     * When zoom is >= this value we switch to the 5mm grid.
     * Tune this if you want the fine grid to appear earlier/later.
     */
    private static final double FINE_GRID_ZOOM_THRESHOLD = 2.0;

    /** Visual thickness (diameter) at zoomFactor == 1.0, in world units (mm). */
    private static final double GRID_DIAMETER = 0.08;

    /** Slight Z lift to avoid z-fighting with the plane/model. */
    private static final double Z_OFFSET = 0.001;

    /** Remember the current grid extents + step so the Visualizer can draw a 2D overlay. */
    public record TickSpec(double x1, double y1, double x2, double y2, double step) {}

    private final BackendAPI backend;

    private final DoubleProperty minX = new SimpleDoubleProperty(0);
    private final DoubleProperty minY = new SimpleDoubleProperty(0);
    private final DoubleProperty maxX = new SimpleDoubleProperty(100);
    private final DoubleProperty maxY = new SimpleDoubleProperty(100);

    private final PhongMaterial material = new PhongMaterial(Color.GRAY);

    /** Each grid line is its own Cylinder node. */
    private final Group gridGroup = new Group();
    private final List<Cylinder> gridCylinders = new ArrayList<>();

    private double currentZoomFactor = 1.0;
    private double currentGridStepMm = GRID_STEP_COARSE_MM;

    public Grid() {
        backend.addUGSEventListener(this::onEvent);

        regenerateGrid();
        getChildren().add(gridGroup);
    }

    private double desiredGridStepForZoom(double zoomFactor) {
        return zoomFactor >= FINE_GRID_ZOOM_THRESHOLD ? GRID_STEP_FINE_MM : GRID_STEP_COARSE_MM;
    }

    private double cylinderRadiusForZoom(double zoomFactor) {
        return GRID_DIAMETER / zoomFactor;
    }

    private void clearGridNodes() {
        gridGroup.getChildren().clear();
        gridCylinders.clear();
    }

    private void regenerateGrid() {
        clearGridNodes();

        double step = currentGridStepMm;

        double x1 = Math.round((minX.get() - step) / step) * step;
        double y1 = Math.round((minY.get() - step) / step) * step;
        double x2 = Math.round((maxX.get() + step) / step) * step;
        double y2 = Math.round((maxY.get() + step) / step) * step;

        double radius = cylinderRadiusForZoom(currentZoomFactor);

        // Vertical lines (constant X, spanning Y)
        for (double x = x1; x <= x2; x += step) {
            Cylinder c = new Cylinder(radius, Math.max(0.0001, (y2 - y1)));
            c.setMaterial(material);

            c.setTranslateX(x);
            c.setTranslateY((y1 + y2) / 2.0);
            c.setTranslateZ(Z_OFFSET);

            // Default cylinder axis is Y in JavaFX, so no rotation needed.
            gridCylinders.add(c);
            gridGroup.getChildren().add(c);
        }

        // Horizontal lines (constant Y, spanning X)
        for (double y = y1; y <= y2; y += step) {
            Cylinder c = new Cylinder(radius, Math.max(0.0001, (x2 - x1)));
            c.setMaterial(material);

            c.setTranslateX((x1 + x2) / 2.0);
            c.setTranslateY(y);
            c.setTranslateZ(Z_OFFSET);

            // Rotate around Z so the cylinder runs along X instead of Y.
            c.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));

            gridCylinders.add(c);
            gridGroup.getChildren().add(c);
        }
    }

    private void updateCylinderRadiiOnly() {
        double radius = cylinderRadiusForZoom(currentZoomFactor);
        for (Cylinder c : gridCylinders) {
            c.setRadius(radius);
        }
    }

    private void onEvent(UGSEvent ugsEvent) {
        if (ugsEvent instanceof FileStateEvent fileStateEvent) {
            if (fileStateEvent.getFileState() == FileState.FILE_LOADED) {
                GcodeStats gcodeStats = backend.getGcodeStats();
                minX.set(gcodeStats.getMin().getX());
                minY.set(gcodeStats.getMin().getY());
                maxX.set(gcodeStats.getMax().getX());
                maxY.set(gcodeStats.getMax().getY());
                regenerateGrid();
            }
        }
    }

    @Override
    public void onZoomChange(double zoomFactor) {
        currentZoomFactor = zoomFactor;

        double desiredStep = desiredGridStepForZoom(zoomFactor);
        boolean stepChanged = desiredStep != currentGridStepMm;
        currentGridStepMm = desiredStep;

        if (stepChanged) {
            regenerateGrid();
        } else {
            // Only thickness needs changing (inverse scaled)
            updateCylinderRadiiOnly();
        }
    }

    @Override
    public boolean useLighting() {
        return false;
    }
}
