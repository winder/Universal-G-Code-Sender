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

import javafx.geometry.Point3D;

/**
 * The six axis aligned views of the work area, each named after the side of the machine the
 * camera looks at. {@code normal} points from the work area towards the camera.
 */
public enum ViewOrientation {
    TOP(0, 90, new Point3D(0, 0, 1)),
    BOTTOM(0, -90, new Point3D(0, 0, -1)),
    FRONT(0, 0, new Point3D(0, -1, 0)),
    BACK(180, 0, new Point3D(0, 1, 0)),
    RIGHT(90, 0, new Point3D(1, 0, 0)),
    LEFT(-90, 0, new Point3D(-1, 0, 0));

    private final double yawDegrees;
    private final double pitchDegrees;
    private final Point3D normal;

    ViewOrientation(double yawDegrees, double pitchDegrees, Point3D normal) {
        this.yawDegrees = yawDegrees;
        this.pitchDegrees = pitchDegrees;
        this.normal = normal;
    }

    public double yawDegrees() {
        return yawDegrees;
    }

    public double pitchDegrees() {
        return pitchDegrees;
    }

    public Point3D normal() {
        return normal;
    }

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
