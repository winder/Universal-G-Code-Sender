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

import java.util.Optional;

/**
 * A ray in world coordinates, as returned by {@link Camera#unproject}.
 */
public record Ray(Point3D origin, Point3D direction) {

    /**
     * Where the ray crosses the horizontal plane at the given height, or empty when it runs
     * parallel to it or the plane lies behind the origin.
     */
    public Optional<Point3D> intersectPlaneZ(double z) {
        if (Math.abs(direction.getZ()) < 1e-12) {
            return Optional.empty();
        }
        double t = (z - origin.getZ()) / direction.getZ();
        if (t < 0) {
            return Optional.empty();
        }
        return Optional.of(pointAt(t));
    }

    public Point3D pointAt(double t) {
        return origin.add(direction.multiply(t));
    }
}
