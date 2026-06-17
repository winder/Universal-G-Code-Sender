/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.fx.component.visualizer.models;

import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.List;

public class Ruler extends Model {

    private static final double TICK_LENGTH_CM = 8.0;
    private static final double TICK_LENGTH_HALF_CM = 4.0;
    private static final double TICK_LENGTH_MM = 2.0;

    private static final double TICK_DIAMETER = 0.15;
    private static final double BASE_DIAMETER = 0.05;

    /**
     * Slight Z lift to avoid z-fighting with grid/plane.
     */
    private static final double Z_OFFSET = 0.002;

    /**
     * Target height of label glyphs in world units (mm).
     */
    private static final double LABEL_SIZE_MM = 4.0;

    /**
     * Font size used when rasterizing labels. Higher = sharper texture.
     */
    private static final double LABEL_FONT_SIZE = 32.0;

    /**
     * Extra DPI multiplier applied when snapshotting the text node.
     * */
    private static final double LABEL_SNAPSHOT_SCALE = 2.0;
    private static final double LABEL_GAP = 1.0;

    private static final double MM_PER_INCH = 25.4;

    /** Metric ruler: small=1mm, medium=5mm (5×small), large=10mm (10×small), label every 2 large = 2cm. */
    private static final double METRIC_SMALL_MM = 1.0;
    private static final int METRIC_MEDIUM_PER_SMALL = 5;
    private static final int METRIC_LARGE_PER_SMALL = 10;
    private static final int METRIC_LABEL_EVERY_LARGE = 2;

    /** Imperial ruler: small=1/8″, medium=1/4″ (2×small), large=1″ (8×small), label every 1″. */
    private static final double IMPERIAL_SMALL_MM = MM_PER_INCH / 8.0;
    private static final int IMPERIAL_MEDIUM_PER_SMALL = 2;
    private static final int IMPERIAL_LARGE_PER_SMALL = 8;
    private static final int IMPERIAL_LABEL_EVERY_LARGE = 1;

    private static final double DEFAULT_MIN = 0;
    private static final double DEFAULT_MAX = 100;

    private final BackendAPI backend;

    private double minX = DEFAULT_MIN;
    private double minY = DEFAULT_MIN;
    private double maxX = DEFAULT_MAX;
    private double maxY = DEFAULT_MAX;

    private final PhongMaterial material = new PhongMaterial(Color.BLACK);
    private final Group rulerGroup = new Group();
    private final List<Cylinder> cylinders = new ArrayList<>();

    private Color textColor = Color.BLACK;
    private double currentZoomFactor = 1.0;
    private UnitUtils.Units activeUnits = UnitUtils.Units.MM;

    public Ruler() {
        setDepthTest(DepthTest.DISABLE);

        this.backend = LookupService.lookup(BackendAPI.class);
        activeUnits = backend.getSettings().getPreferredUnits();
        backend.addUGSEventListener(this::onEvent);

        VisualizerSettings settings = VisualizerSettings.getInstance();
        applyLineColor(settings.colorRulerLinesProperty().getValue());
        applyTextColor(settings.colorRulerTextProperty().getValue());
        settings.colorRulerLinesProperty().addListener((obs, oldVal, newVal) -> applyLineColor(newVal));
        settings.colorRulerTextProperty().addListener((obs, oldVal, newVal) -> {
            applyTextColor(newVal);
            regenerate();
        });
        visibleProperty().bind(settings.showRulerProperty());

        regenerate();
        getChildren().add(rulerGroup);
    }

    private void applyLineColor(String webColor) {
        material.setDiffuseColor(Color.web(webColor));
    }

    private void applyTextColor(String webColor) {
        textColor = Color.web(webColor);
    }

    private void clear() {
        rulerGroup.getChildren().clear();
        cylinders.clear();
    }

    private void regenerate() {
        clear();

        double large = largeStepMm();
        double xStart = Math.floor(minX / large) * large;
        double xEnd = Math.ceil(maxX / large) * large;
        double yStart = Math.floor(minY / large) * large;
        double yEnd = Math.ceil(maxY / large) * large;

        if (xEnd <= xStart) xEnd = xStart + large;
        if (yEnd <= yStart) yEnd = yStart + large;

        buildXAxis(xStart, xEnd);
        buildYAxis(yStart, yEnd);
    }

    private void buildXAxis(double start, double end) {
        double baseLength = end - start;
        Cylinder base = new Cylinder(baseRadius(), baseLength);
        base.setMaterial(material);
        base.setTranslateX((start + end) / 2.0);
        base.setTranslateZ(Z_OFFSET);
        // Default cylinder runs along Y; rotate 90° about Z to run along X.
        base.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        cylinders.add(base);
        rulerGroup.getChildren().add(base);

        double small = smallStepMm();
        int medium = mediumPerSmall();
        int large = largePerSmall();
        int labelEvery = labelEveryLarge();
        int steps = (int) Math.round(baseLength / small);

        for (int s = 0; s <= steps; s++) {
            double position = start + s * small;
            double tickLength = tickLengthFor(s, medium, large);

            Cylinder tick = new Cylinder(tickRadius(), tickLength);
            tick.setMaterial(material);
            tick.setTranslateX(position);
            tick.setTranslateY(-tickLength / 2.0);
            tick.setTranslateZ(Z_OFFSET);
            cylinders.add(tick);
            rulerGroup.getChildren().add(tick);

            if (shouldLabel(s, steps, large, labelEvery)) {
                rulerGroup.getChildren().add(buildLabel(
                        labelText(position),
                        position,
                        -TICK_LENGTH_CM - LABEL_GAP - LABEL_SIZE_MM / 2.0));
            }
        }
    }

    private void buildYAxis(double start, double end) {
        double baseLength = end - start;
        Cylinder base = new Cylinder(baseRadius(), baseLength);
        base.setMaterial(material);
        base.setTranslateY((start + end) / 2.0);
        base.setTranslateZ(Z_OFFSET);
        cylinders.add(base);
        rulerGroup.getChildren().add(base);

        double small = smallStepMm();
        int medium = mediumPerSmall();
        int large = largePerSmall();
        int labelEvery = labelEveryLarge();
        int steps = (int) Math.round(baseLength / small);

        for (int s = 0; s <= steps; s++) {
            double position = start + s * small;
            double tickLength = tickLengthFor(s, medium, large);

            Cylinder tick = new Cylinder(tickRadius(), tickLength);
            tick.setMaterial(material);
            tick.setTranslateX(-tickLength / 2.0);
            tick.setTranslateY(position);
            tick.setTranslateZ(Z_OFFSET);
            tick.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
            cylinders.add(tick);
            rulerGroup.getChildren().add(tick);

            if (shouldLabel(s, steps, large, labelEvery)) {
                rulerGroup.getChildren().add(buildLabel(
                        labelText(position),
                        -TICK_LENGTH_CM - LABEL_GAP - LABEL_SIZE_MM,
                        position));
            }
        }
    }

    private static double tickLengthFor(int stepIndex, int mediumPerSmall, int largePerSmall) {
        if (stepIndex % largePerSmall == 0) return TICK_LENGTH_CM;
        if (stepIndex % mediumPerSmall == 0) return TICK_LENGTH_HALF_CM;
        return TICK_LENGTH_MM;
    }

    private static boolean shouldLabel(int stepIndex, int totalSteps, int largePerSmall, int labelEveryLarge) {
        if (stepIndex % largePerSmall != 0) return false;
        int largeIndex = stepIndex / largePerSmall;
        return largeIndex % labelEveryLarge == 0 || stepIndex == 0 || stepIndex == totalSteps;
    }

    private double smallStepMm() {
        return activeUnits == UnitUtils.Units.INCH ? IMPERIAL_SMALL_MM : METRIC_SMALL_MM;
    }

    private int mediumPerSmall() {
        return activeUnits == UnitUtils.Units.INCH ? IMPERIAL_MEDIUM_PER_SMALL : METRIC_MEDIUM_PER_SMALL;
    }

    private int largePerSmall() {
        return activeUnits == UnitUtils.Units.INCH ? IMPERIAL_LARGE_PER_SMALL : METRIC_LARGE_PER_SMALL;
    }

    private int labelEveryLarge() {
        return activeUnits == UnitUtils.Units.INCH ? IMPERIAL_LABEL_EVERY_LARGE : METRIC_LABEL_EVERY_LARGE;
    }

    private double largeStepMm() {
        return smallStepMm() * largePerSmall();
    }

    private String labelText(double positionMm) {
        if (activeUnits == UnitUtils.Units.INCH) {
            return String.valueOf((int) Math.round(positionMm / MM_PER_INCH));
        }
        return String.valueOf((int) Math.round(positionMm));
    }

    private ImageView buildLabel(String value, double x, double y) {
        Text text = new Text(value);
        text.setFont(Font.font(LABEL_FONT_SIZE));
        text.setFill(textColor);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setTransform(Transform.scale(LABEL_SNAPSHOT_SCALE, LABEL_SNAPSHOT_SCALE));
        WritableImage image = text.snapshot(params, null);

        ImageView view = new ImageView(image);
        view.setSmooth(true);
        view.setPreserveRatio(true);
        view.setFitHeight(LABEL_SIZE_MM);

        // The world is rotated 180° about Y then 180° about Z (net: 180° about X),
        // which would render the texture upside-down. Counter-rotate so the
        // glyph reads correctly from the default top-down view.
        view.getTransforms().add(new Rotate(180, Rotate.X_AXIS));

        // ImageView occupies local [0, fitWidth] × [0, fitHeight]. After the X-axis
        // flip the visible Y range becomes [-fitHeight, 0], so to center on (x, y)
        // we translate the (rotated) origin to (x - w/2, y + h/2).
        double aspect = image.getWidth() / image.getHeight();
        double fitWidth = LABEL_SIZE_MM * aspect;
        view.setTranslateX(x - fitWidth / 2.0);
        view.setTranslateY(y + LABEL_SIZE_MM / 2.0);
        view.setTranslateZ(Z_OFFSET);
        return view;
    }

    private double baseRadius() {
        return BASE_DIAMETER / currentZoomFactor;
    }

    private double tickRadius() {
        return TICK_DIAMETER / currentZoomFactor;
    }

    /**
     * Sizes the ruler to the active workspace. The bounds come from the {@link WorkspaceContext} so
     * the ruler no longer needs to know how the size is determined (gcode bounds, design drawing,
     * ...). When the workspace cannot report its size the ruler keeps its current extents.
     */
    private void updateBoundsFromWorkspace() {
        WorkspaceManager.getInstance().getActiveWorkspace()
                .flatMap(WorkspaceContext::getBounds)
                .ifPresent(bounds -> {
                    // Always include the origin, then extend toward the workspace content. This
                    // covers content in the negative quadrants (negative bound to zero) as well as
                    // the positive quadrants (zero to positive bound).
                    minX = Math.min(0, bounds.minX());
                    minY = Math.min(0, bounds.minY());
                    maxX = Math.max(0, bounds.maxX());
                    maxY = Math.max(0, bounds.maxY());
                    regenerate();
                });
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent
                && fileStateEvent.getFileState() == FileState.FILE_LOADED) {
            updateBoundsFromWorkspace();
        } else if (event instanceof SettingChangedEvent) {
            UnitUtils.Units preferred = backend.getSettings().getPreferredUnits();
            if (preferred != activeUnits) {
                activeUnits = preferred;
                regenerate();
            }
        }
    }

    @Override
    public void onZoomChange(double zoomFactor) {
        currentZoomFactor = zoomFactor;
        double base = baseRadius();
        double tick = tickRadius();
        for (Cylinder c : cylinders) {
            c.setRadius(c.getHeight() > TICK_LENGTH_CM ? base : tick);
        }
    }

    @Override
    public boolean useLighting() {
        return false;
    }
}
