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

import com.willwinder.universalgcodesender.fx.component.visualizer.PositionAnimatorTimer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Mat4;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneLayer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneMeshes;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;

/**
 * A cone with its tip at the current work position, animated between status reports.
 */
public final class ToolMarkerRenderable implements Renderable {
    private static final double RADIUS = 4;
    private static final double HEIGHT = 10;
    private static final int DIVISIONS = 24;
    private static final float[] COLOR = {1.0f, 0.65f, 0.0f, 1.0f};

    private final PositionAnimatorTimer positionAnimator = new PositionAnimatorTimer();
    private final UGSEventListener eventListener = this::onEvent;
    private final InvalidationListener positionListener = observable -> requestRender();
    private final ChangeListener<Boolean> visibilityListener = (observable, oldValue, newValue) -> requestRender();
    private Scene scene;
    private MeshHandle mesh;

    @Override
    public SceneLayer layer() {
        return SceneLayer.MACHINE;
    }

    @Override
    public boolean isVisible() {
        return VisualizerSettings.getInstance().showToolProperty().get();
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        mesh = scene.context().upload(SceneMeshes.cone(RADIUS, HEIGHT, DIVISIONS), VertexLayout.MESH);
        positionAnimator.posXProperty().addListener(positionListener);
        positionAnimator.posYProperty().addListener(positionListener);
        positionAnimator.posZProperty().addListener(positionListener);
        VisualizerSettings.getInstance().showToolProperty().addListener(visibilityListener);
        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(eventListener);
        positionAnimator.setTarget(backend.getWorkPosition().getPositionIn(UnitUtils.Units.MM));
        positionAnimator.start();
    }

    @Override
    public void onDetached(Scene scene) {
        LookupService.lookup(BackendAPI.class).removeUGSEventListener(eventListener);
        VisualizerSettings.getInstance().showToolProperty().removeListener(visibilityListener);
        positionAnimator.stop();
        positionAnimator.posXProperty().removeListener(positionListener);
        positionAnimator.posYProperty().removeListener(positionListener);
        positionAnimator.posZProperty().removeListener(positionListener);
        if (mesh != null) {
            scene.context().release(mesh);
            mesh = null;
        }
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        float[] model = Mat4.translation(
                positionAnimator.posXProperty().get(),
                positionAnimator.posYProperty().get(),
                positionAnimator.posZProperty().get());
        context.drawTriangles(mesh, model, COLOR, true);
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStatusEvent controllerStatusEvent) {
            positionAnimator.setTarget(controllerStatusEvent.getStatus()
                    .getWorkCoord()
                    .getPositionIn(UnitUtils.Units.MM));
            positionAnimator.start();
        }
    }

    private void requestRender() {
        if (scene != null) {
            scene.requestRender();
        }
    }
}
