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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The renderables of the visualizer, drawn in {@link SceneLayer} order and, within a layer, in
 * the order they were added. The scene does not draw on its own: anything that changes what
 * should be seen calls {@link #requestRender()}, and whoever owns the frame loop listens for it.
 *
 * <p>Not thread safe. Use it from the JavaFX application thread only.
 */
public final class Scene {
    private final RenderContext context;
    private final List<Renderable> renderables = new ArrayList<>();
    private final List<Runnable> renderListeners = new CopyOnWriteArrayList<>();

    public Scene(RenderContext context) {
        this.context = context;
    }

    /**
     * The context the renderables upload their meshes through. Drawing through it is only
     * possible while {@link #render()} runs.
     */
    public RenderContext context() {
        return context;
    }

    public void add(Renderable renderable) {
        if (renderables.contains(renderable)) {
            return;
        }
        renderables.add(renderable);
        renderable.onAttached(this);
        requestRender();
    }

    public void remove(Renderable renderable) {
        if (renderables.remove(renderable)) {
            renderable.onDetached(this);
            requestRender();
        }
    }

    /**
     * Detaches every renderable, releasing what they hold.
     */
    public void clear() {
        List<Renderable> detached = new ArrayList<>(renderables);
        renderables.clear();
        detached.forEach(renderable -> renderable.onDetached(this));
        requestRender();
    }

    public List<Renderable> renderables() {
        return Collections.unmodifiableList(renderables);
    }

    public void addRenderListener(Runnable listener) {
        renderListeners.add(listener);
    }

    public void removeRenderListener(Runnable listener) {
        renderListeners.remove(listener);
    }

    /**
     * Asks for a new frame. Cheap to call repeatedly; the frame loop coalesces requests.
     */
    public void requestRender() {
        renderListeners.forEach(Runnable::run);
    }

    public void render() {
        for (SceneLayer layer : SceneLayer.values()) {
            boolean depthTestSet = false;
            for (Renderable renderable : renderables) {
                if (renderable.layer() != layer || !renderable.isVisible()) {
                    continue;
                }
                if (!depthTestSet) {
                    context.setDepthTest(layer.isDepthTested());
                    depthTestSet = true;
                }
                renderable.render(context);
            }
        }
    }

    /**
     * The union of the bounds of every visible renderable that has one.
     */
    public Optional<Bounds3> bounds() {
        return renderables.stream()
                .filter(Renderable::isVisible)
                .map(Renderable::bounds)
                .flatMap(Optional::stream)
                .reduce(Bounds3::union);
    }
}
