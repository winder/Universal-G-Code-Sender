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
 * The order the scene is drawn in. Layers that are not depth tested are drawn on top of
 * everything before them, which is what handles and previews need so they never sink into the
 * work plane.
 *
 * <p>The toolpath comes before the design so that a translucent design fill blends over the
 * cuts below the work plane instead of hiding them through the depth buffer; moves above the
 * plane still pass the depth test and stay in front of the fill.
 */
public enum SceneLayer {
    GRID(true),
    RULER(true),
    GCODE(true),
    DESIGN_FILL(true),
    DESIGN_OUTLINE(true),
    MACHINE(true),
    HANDLES(false),
    OVERLAY(false);

    private final boolean depthTested;

    SceneLayer(boolean depthTested) {
        this.depthTested = depthTested;
    }

    public boolean isDepthTested() {
        return depthTested;
    }
}
