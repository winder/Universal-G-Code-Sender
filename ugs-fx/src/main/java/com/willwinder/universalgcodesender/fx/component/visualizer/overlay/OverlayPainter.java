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
package com.willwinder.universalgcodesender.fx.component.visualizer.overlay;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import javafx.scene.canvas.GraphicsContext;

/**
 * Draws 2D content over the rendered frame on a JavaFX canvas: text and markers that Vulkan
 * has no font for. Painters project world positions through the camera and are repainted
 * after every frame, so they always line up with what was rendered.
 */
public interface OverlayPainter {

    /**
     * @param graphics the canvas to draw on, already cleared, in logical pixels
     * @param camera   the camera the frame was rendered with
     * @param width    the width of the canvas in logical pixels
     * @param height   the height of the canvas in logical pixels
     */
    void paint(GraphicsContext graphics, Camera camera, double width, double height);
}
