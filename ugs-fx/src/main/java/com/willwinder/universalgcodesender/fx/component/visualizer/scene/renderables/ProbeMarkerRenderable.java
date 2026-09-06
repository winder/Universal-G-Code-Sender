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
import com.willwinder.universalgcodesender.fx.service.probe.ProbeService;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.beans.InvalidationListener;
import javafx.scene.paint.Color;

import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_ALUMINIUM;
import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;

/**
 * Shows where the touch plate is expected while probing Z: a puck the thickness of the plate
 * under the tool, with an arrow from the safe probing height down onto it. Follows the work
 * position so it moves with the machine.
 */
public final class ProbeMarkerRenderable implements Renderable {
    private static final double PUCK_RADIUS = 20;
    private static final double PUCK_RIM = 1;
    private static final double INNER_RADIUS = 15;
    private static final double ARROW_RADIUS = 0.3;
    private static final double ARROW_HEAD_RADIUS = 2;
    private static final double ARROW_HEAD_HEIGHT = 4;
    private static final int DIVISIONS = 32;

    private final ProbeService probeService;
    private final PositionAnimatorTimer positionAnimator = new PositionAnimatorTimer();
    private final UGSEventListener eventListener = this::onEvent;
    private final InvalidationListener positionListener = observable -> requestRender();
    private final InvalidationListener thicknessListener = observable -> invalidateMeshes();
    private final float[] puckColor = toRgba(COLOR_DARK_GREY, 1);
    private final float[] innerColor = toRgba(COLOR_ALUMINIUM, 1);
    private final float[] arrowColor = toRgba(COLOR_DARK_GREY, 0.6);
    private Scene scene;
    private MeshHandle puck;
    private MeshHandle inner;
    private MeshHandle arrowShaft;
    private MeshHandle arrowHead;
    private double meshSafeDistance;

    public ProbeMarkerRenderable(ProbeService probeService) {
        this.probeService = probeService;
    }

    @Override
    public SceneLayer layer() {
        return SceneLayer.MACHINE;
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        positionAnimator.posXProperty().addListener(positionListener);
        positionAnimator.posYProperty().addListener(positionListener);
        positionAnimator.posZProperty().addListener(positionListener);
        ProbeSettings.getInstance().zPlateThicknessProperty().addListener(thicknessListener);
        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(eventListener);
        moveTo(backend.getWorkPosition().getPositionIn(UnitUtils.Units.MM));
    }

    @Override
    public void onDetached(Scene scene) {
        LookupService.lookup(BackendAPI.class).removeUGSEventListener(eventListener);
        ProbeSettings.getInstance().zPlateThicknessProperty().removeListener(thicknessListener);
        positionAnimator.stop();
        positionAnimator.posXProperty().removeListener(positionListener);
        positionAnimator.posYProperty().removeListener(positionListener);
        positionAnimator.posZProperty().removeListener(positionListener);
        releaseMeshes();
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        double safeDistance = safeDistanceMm();
        if (puck == null || safeDistance != meshSafeDistance) {
            releaseMeshes();
            build(context, safeDistance);
        }
        float[] model = Mat4.translation(
                positionAnimator.posXProperty().get(),
                positionAnimator.posYProperty().get(),
                positionAnimator.posZProperty().get());
        context.drawTriangles(puck, model, puckColor, true);
        context.drawTriangles(inner, model, innerColor, true);
        context.drawTriangles(arrowHead, model, arrowColor, true);
        context.drawTriangles(arrowShaft, model, arrowColor, true);
    }

    /**
     * The plate's top face is the marker origin; the puck hangs below it and the arrow comes
     * down from the safe probing height above it.
     */
    private void build(RenderContext context, double safeDistance) {
        double thickness = ProbeSettings.getInstance().zPlateThicknessProperty().get().convertTo(Unit.MM).doubleValue();
        meshSafeDistance = safeDistance;
        puck = context.upload(translate(SceneMeshes.cylinder(PUCK_RADIUS, thickness, DIVISIONS), -thickness - PUCK_RIM),
                VertexLayout.MESH);
        inner = context.upload(translate(SceneMeshes.cylinder(INNER_RADIUS, thickness, DIVISIONS), -thickness),
                VertexLayout.MESH);
        arrowHead = context.upload(SceneMeshes.cone(ARROW_HEAD_RADIUS, ARROW_HEAD_HEIGHT, 16), VertexLayout.MESH);
        double shaftLength = Math.max(Math.abs(safeDistance) - ARROW_HEAD_HEIGHT, 0.1);
        arrowShaft = context.upload(translate(SceneMeshes.cylinder(ARROW_RADIUS, shaftLength, 10), ARROW_HEAD_HEIGHT),
                VertexLayout.MESH);
    }

    private static float[] translate(float[] meshVertices, double z) {
        for (int i = 2; i < meshVertices.length; i += SceneMeshes.FLOATS_PER_VERTEX) {
            meshVertices[i] += (float) z;
        }
        return meshVertices;
    }

    private double safeDistanceMm() {
        return probeService.getSafeProbeZDistance().convertTo(Unit.MM).doubleValue();
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStatusEvent controllerStatusEvent) {
            moveTo(controllerStatusEvent.getStatus().getWorkCoord().getPositionIn(UnitUtils.Units.MM));
        }
    }

    /**
     * The plate is expected where the probe move ends. The safe probing distance is a downward
     * move, so it is negative and lands below the tool.
     */
    private void moveTo(Position workPosition) {
        Position target = new Position(workPosition);
        target.setZ(workPosition.getZ() + safeDistanceMm());
        positionAnimator.setTarget(target);
        positionAnimator.start();
    }

    private void invalidateMeshes() {
        releaseMeshes();
        requestRender();
    }

    private void releaseMeshes() {
        if (scene != null) {
            for (MeshHandle mesh : new MeshHandle[]{puck, inner, arrowShaft, arrowHead}) {
                if (mesh != null) {
                    scene.context().release(mesh);
                }
            }
        }
        puck = null;
        inner = null;
        arrowShaft = null;
        arrowHead = null;
    }

    private void requestRender() {
        if (scene != null) {
            scene.requestRender();
        }
    }

    private static float[] toRgba(Color color, double alpha) {
        return new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) alpha};
    }
}
