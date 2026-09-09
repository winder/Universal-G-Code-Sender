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
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.Optional;

/**
 * Publishes the block of material the stock simulation is currently showing, so the rest of the
 * user interface can describe it. Empty while the stock is hidden or no program is loaded.
 */
public final class StockService {
    private static StockService instance;
    private final ReadOnlyObjectWrapper<Optional<Bounds3>> bounds = new ReadOnlyObjectWrapper<>(this, "bounds", Optional.empty());

    private StockService() {
    }

    public static synchronized StockService getInstance() {
        if (instance == null) {
            instance = new StockService();
        }
        return instance;
    }

    /**
     * The bounds of the simulated block, updated on the JavaFX thread.
     */
    public ReadOnlyObjectProperty<Optional<Bounds3>> boundsProperty() {
        return bounds.getReadOnlyProperty();
    }

    public Optional<Bounds3> getBounds() {
        return bounds.get();
    }

    public void setBounds(Optional<Bounds3> value) {
        if (Platform.isFxApplicationThread()) {
            bounds.set(value);
        } else {
            Platform.runLater(() -> bounds.set(value));
        }
    }
}
