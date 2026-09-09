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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.StockSpec;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;


/**
 * Builds the {@link StockSpec} for a program from the visualizer settings.
 */
public final class StockSpecs {
    private StockSpecs() {
    }

    /**
     * Reads JavaFX properties, so call it on the JavaFX thread or right where the settings were
     * changed.
     */
    public static StockSpec fromSettings() {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        StockSpec.Mode mode = parseMode(settings.stockModeProperty().get());
        if (mode == StockSpec.Mode.MANUAL) {
            double minX = settings.stockMinXProperty().get();
            double minY = settings.stockMinYProperty().get();
            double top = settings.stockTopProperty().get();
            return StockSpec.manual(new Bounds3(minX, minY, top - settings.stockThicknessProperty().get(),
                    minX + settings.stockWidthProperty().get(), minY + settings.stockLengthProperty().get(), top));
        }
        return StockSpec.AUTOMATIC;
    }

    public static StockSpec.Mode parseMode(String name) {
        try {
            return name == null ? StockSpec.Mode.AUTOMATIC : StockSpec.Mode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return StockSpec.Mode.AUTOMATIC;
        }
    }

}
