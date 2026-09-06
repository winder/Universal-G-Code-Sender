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
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

/**
 * The X, Y and Z axes from the work origin, in the usual red, green and blue.
 */
public final class AxesRenderable implements Renderable {
    private static final double EXTENT = 220;
    private static final double Z = 0.06;
    private static final float WIDTH_PX = 2;
    private static final Color X_COLOR = Color.color(0.85, 0.20, 0.20);
    private static final Color Y_COLOR = Color.color(0.20, 0.70, 0.25);
    private static final Color Z_COLOR = Color.color(0.25, 0.45, 0.90);

    private final ChangeListener<Boolean> visibilityListener = (observable, oldValue, newValue) -> requestRender();
    private Scene scene;
    private MeshHandle mesh;

    @Override
    public SceneLayer layer() {
        return SceneLayer.GRID;
    }

    @Override
    public boolean isVisible() {
        return VisualizerSettings.getInstance().showAxesProperty().get();
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        float[] vertices = new LineMeshBuilder(3)
                .add(0, 0, Z, EXTENT, 0, Z, X_COLOR)
                .add(0, 0, Z, 0, EXTENT, Z, Y_COLOR)
                .add(0, 0, Z, 0, 0, EXTENT, Z_COLOR)
                .build();
        mesh = scene.context().upload(vertices, VertexLayout.LINE);
        VisualizerSettings.getInstance().showAxesProperty().addListener(visibilityListener);
    }

    @Override
    public void onDetached(Scene scene) {
        VisualizerSettings.getInstance().showAxesProperty().removeListener(visibilityListener);
        if (mesh != null) {
            scene.context().release(mesh);
            mesh = null;
        }
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        context.drawColoredLines(mesh, null, WIDTH_PX);
    }

    private void requestRender() {
        if (scene != null) {
            scene.requestRender();
        }
    }
}
