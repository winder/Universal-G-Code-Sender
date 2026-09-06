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
import javafx.scene.paint.Color;

/**
 * A grid on the work plane sized to the active workspace, always including the origin. The
 * step follows the preferred units and switches to a finer one when zoomed in far enough for
 * it to be readable.
 */
public final class GridRenderable implements Renderable {
    private static final double MM_PER_INCH = 25.4;
    private static final double METRIC_COARSE_MM = 10.0;
    private static final double METRIC_FINE_MM = 5.0;
    private static final double IMPERIAL_COARSE_MM = MM_PER_INCH;
    private static final double IMPERIAL_FINE_MM = MM_PER_INCH / 4.0;
    /** The fine grid appears once its lines are at least this far apart on screen. */
    private static final double FINE_GRID_MIN_PIXELS = 15;
    private static final double Z = 0.05;
    private static final Color COLOR = Color.color(0.55, 0.55, 0.55);
    private static final float[] RGBA = {0.55f, 0.55f, 0.55f, 1.0f};
    private static final double DEFAULT_EXTENT = 100;

    private final UGSEventListener eventListener = this::onEvent;
    private final ChangeListener<Boolean> visibilityListener = (observable, oldValue, newValue) -> requestRender();
    private Scene scene;
    private MeshHandle mesh;
    private double meshStep;
    private double minX;
    private double minY;
    private double maxX = DEFAULT_EXTENT;
    private double maxY = DEFAULT_EXTENT;
    private UnitUtils.Units units = UnitUtils.Units.MM;

    @Override
    public SceneLayer layer() {
        return SceneLayer.GRID;
    }

    @Override
    public boolean isVisible() {
        return VisualizerSettings.getInstance().showGridProperty().get();
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        units = backend.getSettings().getPreferredUnits();
        backend.addUGSEventListener(eventListener);
        VisualizerSettings.getInstance().showGridProperty().addListener(visibilityListener);
        updateBoundsFromWorkspace();
    }

    @Override
    public void onDetached(Scene scene) {
        LookupService.lookup(BackendAPI.class).removeUGSEventListener(eventListener);
        VisualizerSettings.getInstance().showGridProperty().removeListener(visibilityListener);
        releaseMesh();
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        double step = stepFor(context.camera().worldUnitsPerPixel());
        if (mesh == null || step != meshStep) {
            releaseMesh();
            meshStep = step;
            mesh = context.upload(buildGrid(step), VertexLayout.LINE);
        }
        context.drawLines(mesh, null, RGBA, 1);
    }

    private double stepFor(double worldUnitsPerPixel) {
        double fine = units == UnitUtils.Units.INCH ? IMPERIAL_FINE_MM : METRIC_FINE_MM;
        double coarse = units == UnitUtils.Units.INCH ? IMPERIAL_COARSE_MM : METRIC_COARSE_MM;
        return fine / worldUnitsPerPixel >= FINE_GRID_MIN_PIXELS ? fine : coarse;
    }

    private float[] buildGrid(double step) {
        return LineMeshBuilder.grid(minX - step, minY - step, maxX + step, maxY + step, step, Z, COLOR).build();
    }

    /**
     * Sizes the grid to the active workspace. The bounds come from the {@link WorkspaceContext}
     * so the grid does not need to know how the size is determined. When the workspace cannot
     * report its size the grid keeps its current extents.
     */
    private void updateBoundsFromWorkspace() {
        WorkspaceManager.getInstance().getActiveWorkspace()
                .flatMap(WorkspaceContext::getBounds)
                .ifPresent(bounds -> {
                    minX = Math.min(0, bounds.minX());
                    minY = Math.min(0, bounds.minY());
                    maxX = Math.max(0, bounds.maxX());
                    maxY = Math.max(0, bounds.maxY());
                    invalidateMesh();
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
                    invalidateMesh();
                }
            });
        }
    }

    private void invalidateMesh() {
        releaseMesh();
        requestRender();
    }

    private void releaseMesh() {
        if (mesh != null && scene != null) {
            scene.context().release(mesh);
        }
        mesh = null;
    }

    private void requestRender() {
        if (scene != null) {
            scene.requestRender();
        }
    }
}
