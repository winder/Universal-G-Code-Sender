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
package com.willwinder.universalgcodesender.fx.component.visualizer.models;

import javafx.scene.Group;

/**
 * A visualizer model that can be registerd through the {@link com.willwinder.universalgcodesender.fx.service.VisualizerService#addModel(Model)}.
 *
 */
public abstract class Model extends Group {
    /**
     * When the zoom factor is changed
     *
     * @param zoomFactor the current zoom factor
     */
    public abstract void onZoomChange(double zoomFactor);

    /**
     * Returns true if lighting should be applied to the model
     *
     * @return true if lighting should be used
     */
    public abstract boolean useLighting();
}
