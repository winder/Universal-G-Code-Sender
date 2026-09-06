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

/**
 * An axis aligned box in world coordinates (millimeters, Z up).
 */
public record Bounds3(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

    public static Bounds3 ofPoint(double x, double y, double z) {
        return new Bounds3(x, y, z, x, y, z);
    }

    public double centerX() {
        return (minX + maxX) / 2;
    }

    public double centerY() {
        return (minY + maxY) / 2;
    }

    public double centerZ() {
        return (minZ + maxZ) / 2;
    }

    public double width() {
        return maxX - minX;
    }

    public double height() {
        return maxY - minY;
    }

    public double depth() {
        return maxZ - minZ;
    }

    /**
     * The largest side, which is what a camera needs to fit the whole box in view.
     */
    public double size() {
        return Math.max(Math.max(width(), height()), depth());
    }

    public Bounds3 union(Bounds3 other) {
        return new Bounds3(
                Math.min(minX, other.minX), Math.min(minY, other.minY), Math.min(minZ, other.minZ),
                Math.max(maxX, other.maxX), Math.max(maxY, other.maxY), Math.max(maxZ, other.maxZ));
    }

    public Bounds3 include(double x, double y, double z) {
        return union(ofPoint(x, y, z));
    }
}
