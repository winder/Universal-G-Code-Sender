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
package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings.ModifierKey;
import javafx.scene.input.MouseButton;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Pans, rotates and zooms the camera with the mouse buttons and modifiers the user configured
 * in the visualizer settings. Sits last in the handler stack, so anything an editing tool does
 * not claim ends up moving the view.
 */
public final class CameraNavigationHandler implements InputHandler {
    static final double ROTATE_DEGREES_PER_PIXEL = 0.5;

    private enum Gesture {
        PAN, ROTATE
    }

    private final Camera camera;
    private final Supplier<MouseMapping> panMapping;
    private final Supplier<MouseMapping> rotateMapping;
    private final BooleanSupplier invertZoom;
    private final BooleanSupplier invertRotation;
    private Gesture gesture;
    private double lastX;
    private double lastY;

    public CameraNavigationHandler(Camera camera, Supplier<MouseMapping> panMapping,
                                   Supplier<MouseMapping> rotateMapping, BooleanSupplier invertZoom,
                                   BooleanSupplier invertRotation) {
        this.camera = camera;
        this.panMapping = panMapping;
        this.rotateMapping = rotateMapping;
        this.invertZoom = invertZoom;
        this.invertRotation = invertRotation;
    }

    /**
     * A handler that reads the current visualizer settings on every event, so changes in the
     * settings pane apply at once.
     */
    public static CameraNavigationHandler fromSettings(Camera camera) {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        return new CameraNavigationHandler(camera,
                () -> MouseMapping.parse(settings.panMouseButtonProperty().get(),
                        settings.panModifierKeyProperty().get(), MouseButton.SECONDARY, ModifierKey.NONE),
                () -> MouseMapping.parse(settings.rotateMouseButtonProperty().get(),
                        settings.rotateModifierKeyProperty().get(), MouseButton.SECONDARY, ModifierKey.SHIFT),
                () -> settings.invertZoomProperty().get(),
                () -> settings.invertRotationProperty().get());
    }

    @Override
    public boolean onPressed(PointerEvent event) {
        if (rotateMapping.get().matchesPress(event.mouse())) {
            gesture = Gesture.ROTATE;
        } else if (panMapping.get().matchesPress(event.mouse())) {
            gesture = Gesture.PAN;
        } else {
            return false;
        }
        lastX = event.x();
        lastY = event.y();
        return true;
    }

    @Override
    public void onDragged(PointerEvent event, PointerEvent pressed) {
        double deltaX = event.x() - lastX;
        double deltaY = event.y() - lastY;
        lastX = event.x();
        lastY = event.y();
        if (gesture == Gesture.PAN) {
            camera.pan(deltaX, deltaY);
        } else if (gesture == Gesture.ROTATE) {
            // Dragging right turns the scene right, as if grabbing and turning it, unless inverted.
            double direction = invertRotation.getAsBoolean() ? 1 : -1;
            camera.orbit(direction * deltaX * ROTATE_DEGREES_PER_PIXEL, direction * -deltaY * ROTATE_DEGREES_PER_PIXEL);
        }
    }

    @Override
    public void onReleased(PointerEvent event, PointerEvent pressed) {
        gesture = null;
    }

    @Override
    public boolean onScroll(ScrollInput scroll) {
        double delta = invertZoom.getAsBoolean() ? -scroll.deltaY() : scroll.deltaY();
        camera.zoomAt(delta, scroll.x(), scroll.y());
        return true;
    }
}
