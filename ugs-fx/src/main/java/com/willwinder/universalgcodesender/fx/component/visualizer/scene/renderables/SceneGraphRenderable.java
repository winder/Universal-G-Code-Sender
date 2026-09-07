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

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshConverter;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneGraphWalker;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneLayer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import javafx.scene.Node;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Draws a JavaFX 3D scene graph that was never attached to a JavaFX scene. The tree is walked on
 * every frame, so its transforms and bindings take effect without anything notifying the
 * renderer; vertices are uploaded the first time a shape is seen and then reused.
 *
 * <p>This is how the machine models are drawn: their part classes, nested groups and position
 * bindings keep working unchanged, only the drawing is taken over.
 */
public final class SceneGraphRenderable implements Renderable {
    private final Node root;
    private final SceneLayer layer;
    private final Map<Object, MeshHandle> meshes = new IdentityHashMap<>();
    private Scene scene;

    public SceneGraphRenderable(Node root) {
        this(root, SceneLayer.MACHINE);
    }

    public SceneGraphRenderable(Node root, SceneLayer layer) {
        this.root = root;
        this.layer = layer;
    }

    @Override
    public SceneLayer layer() {
        return layer;
    }

    @Override
    public boolean isVisible() {
        return root.isVisible();
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void onDetached(Scene scene) {
        meshes.values().forEach(scene.context()::release);
        meshes.clear();
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        for (SceneGraphWalker.Draw item : SceneGraphWalker.walk(root)) {
            MeshHandle mesh = meshes.computeIfAbsent(item.key(),
                    key -> context.upload(MeshConverter.toVertices(item.shape()), VertexLayout.MESH));
            context.drawTriangles(mesh, item.modelMatrix(), item.color(), true);
        }
    }

    /**
     * Forgets the uploaded meshes so that changed geometry is re-read on the next frame.
     */
    public void invalidateMeshes() {
        if (scene != null) {
            meshes.values().forEach(scene.context()::release);
            scene.requestRender();
        }
        meshes.clear();
    }
}
