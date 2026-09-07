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
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Ray;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

/**
 * A mouse event together with where it points in the world: the ray through the cursor and, when
 * the ray hits it, the point on the work plane (Z = 0). Handlers get both so camera navigation
 * can stay in pixels while editing tools work in millimeters.
 *
 * @param mouse          the JavaFX event, with the position in logical pixels of the visualizer
 * @param ray            the ray through the cursor, in world coordinates
 * @param workPlanePoint where the ray crosses the work plane, if it does
 */
public record PointerEvent(MouseEvent mouse, Ray ray, Optional<Point3D> workPlanePoint) {

    public static PointerEvent of(MouseEvent mouse, Camera camera) {
        Ray ray = camera.unproject(mouse.getX(), mouse.getY());
        return new PointerEvent(mouse, ray, ray.intersectPlaneZ(0));
    }

    public double x() {
        return mouse.getX();
    }

    public double y() {
        return mouse.getY();
    }

    public Point2D screen() {
        return new Point2D(mouse.getX(), mouse.getY());
    }

    public MouseButton button() {
        return mouse.getButton();
    }

    public int clickCount() {
        return mouse.getClickCount();
    }

    public boolean isShiftDown() {
        return mouse.isShiftDown();
    }

    public boolean isControlDown() {
        return mouse.isControlDown();
    }

    public boolean isAltDown() {
        return mouse.isAltDown();
    }

    public boolean isMetaDown() {
        return mouse.isMetaDown();
    }
}
