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

import java.util.Optional;

/**
 * Something the visualizer draws. Renderables are added to a {@link Scene}, which calls
 * {@link #render} once per frame in {@link SceneLayer} order. Anything a renderable changes
 * between frames has to be followed by {@link Scene#requestRender()} or it will not be seen
 * until the next frame is requested by something else.
 *
 * <p>All methods are called on the JavaFX application thread.
 */
public interface Renderable {

    SceneLayer layer();

    default boolean isVisible() {
        return true;
    }

    /**
     * The extent of what this renderable draws, used to frame the camera. Empty when it has no
     * natural extent, such as the grid or the tool marker.
     */
    default Optional<Bounds3> bounds() {
        return Optional.empty();
    }

    /**
     * Called when added to a scene. The place to register listeners and upload meshes that do
     * not depend on the frame.
     */
    default void onAttached(Scene scene) {
    }

    /**
     * Called when removed from a scene or when the scene is cleared. Everything acquired in
     * {@link #onAttached} is released here.
     */
    default void onDetached(Scene scene) {
    }

    void render(RenderContext context);
}
