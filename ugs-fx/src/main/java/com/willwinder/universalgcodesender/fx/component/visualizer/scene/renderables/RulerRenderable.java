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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables;

import com.willwinder.universalgcodesender.fx.component.visualizer.overlay.OverlayPainter;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.LineMeshBuilder;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneLayer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Rulers along the X and Y axes of the workspace with millimeter or eighth inch ticks. The
 * ticks are lines in the scene; the numbers are drawn on the overlay canvas at the projected
 * tick positions and thinned out when they would overlap.
 */
public final class RulerRenderable implements Renderable, OverlayPainter {
    private static final double TICK_LENGTH_LARGE = 8.0;
    private static final double TICK_LENGTH_MEDIUM = 4.0;
    private static final double TICK_LENGTH_SMALL = 2.0;
    private static final double LABEL_GAP = 3.0;
    private static final float BASE_WIDTH_PX = 1;
    private static final float TICK_WIDTH_PX = 1.5f;
    private static final double Z = 0.05;
    private static final double MM_PER_INCH = 25.4;
    private static final double MIN_LABEL_SPACING_PX = 36;
    private static final Font LABEL_FONT = Font.font(11);

    /** Metric ruler: small=1mm, medium=5mm, large=10mm, label every 2 large = 2cm. */
    private static final double METRIC_SMALL_MM = 1.0;
    private static final int METRIC_MEDIUM_PER_SMALL = 5;
    private static final int METRIC_LARGE_PER_SMALL = 10;
    private static final int METRIC_LABEL_EVERY_LARGE = 2;
    /** Imperial ruler: small=1/8″, medium=1/4″, large=1″, label every 1″. */
    private static final double IMPERIAL_SMALL_MM = MM_PER_INCH / 8.0;
    private static final int IMPERIAL_MEDIUM_PER_SMALL = 2;
    private static final int IMPERIAL_LARGE_PER_SMALL = 8;
    private static final int IMPERIAL_LABEL_EVERY_LARGE = 1;
    private static final double DEFAULT_EXTENT = 100;

    private record Label(String text, Point3D position) {
    }

    private final UGSEventListener eventListener = this::onEvent;
    private final ChangeListener<Object> redrawListener = (observable, oldValue, newValue) -> requestRender();
    private final List<Label> xLabels = new ArrayList<>();
    private final List<Label> yLabels = new ArrayList<>();
    private Scene scene;
    private MeshHandle baseLines;
    private MeshHandle ticks;
    private double minX;
    private double minY;
    private double maxX = DEFAULT_EXTENT;
    private double maxY = DEFAULT_EXTENT;
    private UnitUtils.Units units = UnitUtils.Units.MM;

    @Override
    public SceneLayer layer() {
        return SceneLayer.RULER;
    }

    @Override
    public boolean isVisible() {
        return VisualizerSettings.getInstance().showRulerProperty().get();
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        units = backend.getSettings().getPreferredUnits();
        backend.addUGSEventListener(eventListener);
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.showRulerProperty().addListener(redrawListener);
        settings.colorRulerLinesProperty().addListener(redrawListener);
        settings.colorRulerTextProperty().addListener(redrawListener);
        updateBoundsFromWorkspace();
    }

    @Override
    public void onDetached(Scene scene) {
        LookupService.lookup(BackendAPI.class).removeUGSEventListener(eventListener);
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.showRulerProperty().removeListener(redrawListener);
        settings.colorRulerLinesProperty().removeListener(redrawListener);
        settings.colorRulerTextProperty().removeListener(redrawListener);
        releaseMeshes();
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        if (baseLines == null) {
            build(context);
        }
        float[] color = toRgba(Color.web(VisualizerSettings.getInstance().colorRulerLinesProperty().get()));
        context.drawLines(baseLines, null, color, BASE_WIDTH_PX);
        context.drawLines(ticks, null, color, TICK_WIDTH_PX);
    }

    @Override
    public void paint(GraphicsContext graphics, Camera camera, double width, double height) {
        if (!isVisible()) {
            return;
        }
        graphics.setFill(Color.web(VisualizerSettings.getInstance().colorRulerTextProperty().get()));
        graphics.setFont(LABEL_FONT);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        paintLabels(graphics, camera, xLabels);
        paintLabels(graphics, camera, yLabels);
    }

    /**
     * Draws every n:th label, with n chosen so neighbouring labels stay a readable distance
     * apart on screen at the current zoom.
     */
    private static void paintLabels(GraphicsContext graphics, Camera camera, List<Label> labels) {
        if (labels.size() < 2) {
            labels.forEach(label -> paintLabel(graphics, camera, label));
            return;
        }
        double spacing = camera.project(labels.get(0).position())
                .flatMap(first -> camera.project(labels.get(1).position()).map(first::distance))
                .orElse(MIN_LABEL_SPACING_PX);
        int stride = spacing < 1e-6 ? labels.size() : (int) Math.max(1, Math.ceil(MIN_LABEL_SPACING_PX / spacing));
        for (int i = 0; i < labels.size(); i += stride) {
            paintLabel(graphics, camera, labels.get(i));
        }
    }

    private static void paintLabel(GraphicsContext graphics, Camera camera, Label label) {
        camera.project(label.position()).ifPresent(point ->
                graphics.fillText(label.text(), point.getX(), point.getY()));
    }

    private void build(RenderContext context) {
        xLabels.clear();
        yLabels.clear();
        double large = largeStepMm();
        double xStart = Math.floor(minX / large) * large;
        double xEnd = Math.max(Math.ceil(maxX / large) * large, xStart + large);
        double yStart = Math.floor(minY / large) * large;
        double yEnd = Math.max(Math.ceil(maxY / large) * large, yStart + large);

        LineMeshBuilder base = new LineMeshBuilder(2)
                .add(xStart, 0, Z, xEnd, 0, Z, Color.BLACK)
                .add(0, yStart, Z, 0, yEnd, Z, Color.BLACK);
        LineMeshBuilder tickBuilder = new LineMeshBuilder();
        buildAxis(tickBuilder, xStart, xEnd, true);
        buildAxis(tickBuilder, yStart, yEnd, false);
        baseLines = context.upload(base.build(), VertexLayout.LINE);
        ticks = context.upload(tickBuilder.build(), VertexLayout.LINE);
    }

    private void buildAxis(LineMeshBuilder builder, double start, double end, boolean xAxis) {
        double small = smallStepMm();
        int medium = mediumPerSmall();
        int large = largePerSmall();
        int labelEvery = labelEveryLarge();
        int steps = (int) Math.round((end - start) / small);
        for (int step = 0; step <= steps; step++) {
            double position = start + step * small;
            double length = tickLength(step, medium, large);
            if (xAxis) {
                builder.add(position, 0, Z, position, -length, Z, Color.BLACK);
            } else {
                builder.add(0, position, Z, -length, position, Z, Color.BLACK);
            }
            // Labels sit on multiples of the label step counted from the origin, not from the
            // ruler's start, so they read 0, 20, 40 whatever the workspace extents are.
            long largeIndex = Math.round(position / (small * large));
            if (step % large == 0 && (largeIndex % labelEvery == 0 || step == 0 || step == steps)) {
                double offset = -(TICK_LENGTH_LARGE + LABEL_GAP);
                Point3D labelPosition = xAxis ? new Point3D(position, offset, Z) : new Point3D(offset, position, Z);
                (xAxis ? xLabels : yLabels).add(new Label(labelText(position), labelPosition));
            }
        }
    }

    private static double tickLength(int step, int mediumPerSmall, int largePerSmall) {
        if (step % largePerSmall == 0) {
            return TICK_LENGTH_LARGE;
        }
        if (step % mediumPerSmall == 0) {
            return TICK_LENGTH_MEDIUM;
        }
        return TICK_LENGTH_SMALL;
    }

    private double smallStepMm() {
        return units == UnitUtils.Units.INCH ? IMPERIAL_SMALL_MM : METRIC_SMALL_MM;
    }

    private int mediumPerSmall() {
        return units == UnitUtils.Units.INCH ? IMPERIAL_MEDIUM_PER_SMALL : METRIC_MEDIUM_PER_SMALL;
    }

    private int largePerSmall() {
        return units == UnitUtils.Units.INCH ? IMPERIAL_LARGE_PER_SMALL : METRIC_LARGE_PER_SMALL;
    }

    private int labelEveryLarge() {
        return units == UnitUtils.Units.INCH ? IMPERIAL_LABEL_EVERY_LARGE : METRIC_LABEL_EVERY_LARGE;
    }

    private double largeStepMm() {
        return smallStepMm() * largePerSmall();
    }

    private String labelText(double positionMm) {
        if (units == UnitUtils.Units.INCH) {
            return String.valueOf((int) Math.round(positionMm / MM_PER_INCH));
        }
        return String.valueOf((int) Math.round(positionMm));
    }

    private void updateBoundsFromWorkspace() {
        WorkspaceManager.getInstance().getActiveWorkspace()
                .flatMap(WorkspaceContext::getBounds)
                .ifPresent(bounds -> {
                    minX = Math.min(0, bounds.minX());
                    minY = Math.min(0, bounds.minY());
                    maxX = Math.max(0, bounds.maxX());
                    maxY = Math.max(0, bounds.maxY());
                    invalidate();
                });
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent
                && fileStateEvent.getFileState() == FileState.FILE_LOADED) {
            Platform.runLater(this::updateBoundsFromWorkspace);
        } else if (event instanceof SettingChangedEvent) {
            Platform.runLater(() -> {
                UnitUtils.Units preferred = LookupService.lookup(BackendAPI.class).getSettings().getPreferredUnits();
                if (preferred != units) {
                    units = preferred;
                    invalidate();
                }
            });
        }
    }

    private void invalidate() {
        releaseMeshes();
        requestRender();
    }

    private void releaseMeshes() {
        if (scene != null) {
            if (baseLines != null) {
                scene.context().release(baseLines);
            }
            if (ticks != null) {
                scene.context().release(ticks);
            }
        }
        baseLines = null;
        ticks = null;
    }

    private void requestRender() {
        if (scene != null) {
            scene.requestRender();
        }
    }

    private static float[] toRgba(Color color) {
        return new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()};
    }
}
