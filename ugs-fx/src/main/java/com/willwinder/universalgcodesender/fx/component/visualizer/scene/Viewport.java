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
 * The size of the rendered image in physical pixels, and the scale from the logical JavaFX
 * pixels that mouse events and line widths are expressed in.
 */
public record Viewport(int width, int height, double outputScale) {

    public static final Viewport EMPTY = new Viewport(1, 1, 1);

    public Viewport {
        width = Math.max(width, 1);
        height = Math.max(height, 1);
        outputScale = outputScale > 0 ? outputScale : 1;
    }

    public double aspect() {
        return (double) width / height;
    }

    public double toPhysical(double logicalPixels) {
        return logicalPixels * outputScale;
    }

    public double toLogical(double physicalPixels) {
        return physicalPixels / outputScale;
    }
}
